package com.nutritiontracker.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nutritiontracker.ai.AIServiceFactory
import com.nutritiontracker.ai.FoodAnalysisResult
import com.nutritiontracker.data.SecureKeyStore
import com.nutritiontracker.data.db.AppDatabase
import com.nutritiontracker.data.model.AIProvider
import com.nutritiontracker.data.model.FoodEntry
import com.nutritiontracker.data.model.NutritionInfo
import com.nutritiontracker.data.model.PendingAnalysis
import com.nutritiontracker.data.repository.FoodRepository
import com.nutritiontracker.util.ImageProcessor
import com.nutritiontracker.worker.OfflineAnalysisWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val db = AppDatabase.getDatabase(application)
    private val keyStore = SecureKeyStore(application)
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        val dao = db.foodEntryDao()
        repository = FoodRepository(dao)
        // Load persisted provider preference
        _uiState.value = _uiState.value.copy(
            selectedProvider = keyStore.getSelectedProvider()
        )
    }

    fun setSelectedProvider(provider: AIProvider) {
        _uiState.value = _uiState.value.copy(selectedProvider = provider)
        keyStore.saveSelectedProvider(provider)
    }

    fun setMealType(mealType: String) {
        _uiState.value = _uiState.value.copy(mealType = mealType)
    }

    fun analyzeImage(imageUri: Uri) {
        val context = getApplication<Application>()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                error = null,
                analysisResult = null,
                capturedImageUri = imageUri,
                canQueueOffline = false
            )

            try {
                val base64Image = ImageProcessor.processImageFromUri(context, imageUri)
                if (base64Image == null) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = "Failed to read image. Please try another photo."
                    )
                    return@launch
                }

                analyzeBase64(base64Image)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    fun analyzeImageFromFile(file: File) {
        viewModelScope.launch {
            val uri = Uri.fromFile(file)
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                error = null,
                analysisResult = null,
                capturedImageUri = uri,
                canQueueOffline = false
            )

            try {
                val base64Image = ImageProcessor.processImageFromFile(file)
                if (base64Image == null) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = "Failed to process image. Please try again."
                    )
                    return@launch
                }

                analyzeBase64(base64Image)
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private suspend fun analyzeBase64(base64Image: String) {
        val state = _uiState.value
        val provider = state.selectedProvider
        val apiKey = keyStore.getApiKey(provider)

        // Use mock mode if no key is configured
        val service = if (apiKey.isBlank()) {
            _uiState.value = _uiState.value.copy(usingDemoMode = true)
            AIServiceFactory.createMock()
        } else {
            _uiState.value = _uiState.value.copy(usingDemoMode = false)
            AIServiceFactory.create(provider, apiKey, withRetry = true)
        }

        val result = service.analyzeFood(base64Image)

        when (result) {
            is FoodAnalysisResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    analysisResult = AnalysisResult(
                        foodName = result.foodName,
                        description = result.description,
                        nutritionInfo = result.nutritionInfo
                    )
                )
            }
            is FoodAnalysisResult.Error -> {
                val isNetworkError = result.message.contains("UnknownHost") ||
                    result.message.contains("timeout") ||
                    result.message.contains("network", ignoreCase = true) ||
                    result.message.contains("SocketException")

                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = friendlyErrorMessage(result.message),
                    canQueueOffline = isNetworkError
                )
            }
        }
    }

    fun queueForLater() {
        val state = _uiState.value
        val uri = state.capturedImageUri ?: return
        val context = getApplication<Application>()

        viewModelScope.launch {
            val cleanFile = ImageProcessor.saveCleanImage(context, uri)
            if (cleanFile != null) {
                db.pendingAnalysisDao().insert(
                    PendingAnalysis(
                        imagePath = cleanFile.absolutePath,
                        mealType = state.mealType,
                        provider = state.selectedProvider.name
                    )
                )
                OfflineAnalysisWorker.enqueue(context)

                _uiState.value = _uiState.value.copy(
                    error = null,
                    queued = true,
                    isAnalyzing = false,
                    canQueueOffline = false
                )
            }
        }
    }

    fun saveEntry() {
        val state = _uiState.value
        val result = state.analysisResult ?: return

        viewModelScope.launch {
            val context = getApplication<Application>()
            val cleanFile = state.capturedImageUri?.let {
                ImageProcessor.saveCleanImage(context, it)
            }

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
                imagePath = cleanFile?.absolutePath,
                mealType = state.mealType
            )
            repository.insert(entry)
            _uiState.value = _uiState.value.copy(
                saved = true,
                analysisResult = null
            )
        }
    }

    fun resetState() {
        _uiState.value = _uiState.value.copy(
            analysisResult = null,
            error = null,
            saved = false,
            queued = false,
            capturedImageUri = null,
            isAnalyzing = false,
            usingDemoMode = false,
            canQueueOffline = false
        )
    }

    private fun handleError(e: Exception) {
        val isNetworkError = e is java.net.UnknownHostException ||
            e is java.net.SocketTimeoutException ||
            e is java.io.IOException

        _uiState.value = _uiState.value.copy(
            isAnalyzing = false,
            error = if (isNetworkError) {
                "No internet connection. You can save this image to analyze later."
            } else {
                friendlyErrorMessage(e.message ?: "Unknown error")
            },
            canQueueOffline = isNetworkError
        )
    }

    private fun friendlyErrorMessage(rawMessage: String): String {
        return when {
            rawMessage.contains("401") || rawMessage.contains("Unauthorized") ->
                "Invalid API key. Please check your key in Settings."
            rawMessage.contains("403") || rawMessage.contains("Forbidden") ->
                "Access denied. Your API key may lack required permissions."
            rawMessage.contains("429") || rawMessage.contains("rate") ->
                "Too many requests. Please wait a moment and try again."
            rawMessage.contains("500") || rawMessage.contains("502") || rawMessage.contains("503") ->
                "The AI service is temporarily unavailable. Try again shortly or switch providers."
            rawMessage.contains("timeout") || rawMessage.contains("timed out") ->
                "Request timed out. Check your connection and try again."
            rawMessage.contains("UnknownHost") || rawMessage.contains("network") ->
                "No internet connection. You can queue this image for later analysis."
            rawMessage.contains("parse") ->
                "The AI response was unexpected. Try again or switch to a different provider."
            else -> "Something went wrong: $rawMessage"
        }
    }
}

data class CameraUiState(
    val selectedProvider: AIProvider = AIProvider.CLAUDE,
    val mealType: String = "Snack",
    val isAnalyzing: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val error: String? = null,
    val capturedImageUri: Uri? = null,
    val saved: Boolean = false,
    val queued: Boolean = false,
    val canQueueOffline: Boolean = false,
    val usingDemoMode: Boolean = false
)

data class AnalysisResult(
    val foodName: String,
    val description: String,
    val nutritionInfo: NutritionInfo
)
