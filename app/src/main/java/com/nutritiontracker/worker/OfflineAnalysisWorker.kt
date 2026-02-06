package com.nutritiontracker.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.nutritiontracker.ai.AIServiceFactory
import com.nutritiontracker.ai.FoodAnalysisResult
import com.nutritiontracker.data.SecureKeyStore
import com.nutritiontracker.data.db.AppDatabase
import com.nutritiontracker.data.model.AIProvider
import com.nutritiontracker.data.model.FoodEntry
import com.nutritiontracker.data.model.PendingAnalysis
import com.nutritiontracker.util.ImageProcessor
import java.io.File
import java.util.concurrent.TimeUnit

class OfflineAnalysisWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getDatabase(applicationContext)
        val pendingDao = db.pendingAnalysisDao()
        val foodDao = db.foodEntryDao()
        val keyStore = SecureKeyStore(applicationContext)

        val pending = pendingDao.getPendingAnalyses()
        if (pending.isEmpty()) return Result.success()

        var allSucceeded = true

        for (item in pending) {
            pendingDao.updateStatus(item.id, PendingAnalysis.STATUS_PROCESSING)

            try {
                val provider = try {
                    AIProvider.valueOf(item.provider)
                } catch (e: IllegalArgumentException) {
                    AIProvider.CLAUDE
                }

                val apiKey = keyStore.getApiKey(provider)
                if (apiKey.isBlank()) {
                    pendingDao.updateStatus(item.id, PendingAnalysis.STATUS_PENDING)
                    allSucceeded = false
                    continue
                }

                val imageFile = File(item.imagePath)
                if (!imageFile.exists()) {
                    pendingDao.deleteById(item.id)
                    continue
                }

                val base64 = ImageProcessor.processImageFromFile(imageFile)
                if (base64 == null) {
                    pendingDao.deleteById(item.id)
                    continue
                }

                val service = AIServiceFactory.create(provider, apiKey, withRetry = true)
                val result = service.analyzeFood(base64)

                when (result) {
                    is FoodAnalysisResult.Success -> {
                        val entry = FoodEntry(
                            name = result.foodName,
                            calories = result.nutritionInfo.calories,
                            protein = result.nutritionInfo.protein,
                            carbohydrates = result.nutritionInfo.carbohydrates,
                            fat = result.nutritionInfo.fat,
                            fiber = result.nutritionInfo.fiber,
                            sugar = result.nutritionInfo.sugar,
                            sodium = result.nutritionInfo.sodium,
                            servingSize = result.nutritionInfo.servingSize,
                            imagePath = item.imagePath,
                            mealType = item.mealType,
                            timestamp = item.timestamp
                        )
                        foodDao.insert(entry)
                        pendingDao.deleteById(item.id)
                    }
                    is FoodAnalysisResult.Error -> {
                        val newRetry = item.retryCount + 1
                        if (newRetry >= MAX_RETRIES) {
                            pendingDao.updateStatus(item.id, PendingAnalysis.STATUS_FAILED)
                        } else {
                            pendingDao.update(item.copy(
                                retryCount = newRetry,
                                status = PendingAnalysis.STATUS_PENDING
                            ))
                        }
                        allSucceeded = false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing pending analysis ${item.id}", e)
                pendingDao.updateStatus(item.id, PendingAnalysis.STATUS_PENDING)
                allSucceeded = false
            }
        }

        return if (allSucceeded) Result.success() else Result.retry()
    }

    companion object {
        private const val TAG = "OfflineAnalysisWorker"
        private const val MAX_RETRIES = 5
        private const val WORK_NAME = "offline_analysis_sync"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<OfflineAnalysisWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    2, TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}
