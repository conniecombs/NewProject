package com.nutritiontracker.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nutritiontracker.BuildConfig
import com.nutritiontracker.ai.AIServiceFactory
import com.nutritiontracker.ai.FoodAnalysisResult
import com.nutritiontracker.data.db.AppDatabase
import com.nutritiontracker.data.model.AIProvider
import com.nutritiontracker.data.model.FoodEntry
import com.nutritiontracker.data.model.NutritionInfo
import com.nutritiontracker.data.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FoodRepository
    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).foodEntryDao()
        repository = FoodRepository(dao)
    }

    fun setSelectedProvider(provider: AIProvider) {
        _uiState.value = _uiState.value.copy(selectedProvider = provider)
    }

    fun setApiKey(key: String) {
        _uiState.value = _uiState.value.copy(apiKey = key)
    }

    fun setMealType(mealType: String) {
        _uiState.value = _uiState.value.copy(mealType = mealType)
    }

    fun analyzeImage(imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                error = null,
                analysisResult = null,
                capturedImageUri = imageUri
            )

            try {
                val base64Image = uriToBase64(imageUri)
                if (base64Image == null) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = "Failed to read image"
                    )
                    return@launch
                }

                val state = _uiState.value
                val apiKey = getApiKey(state.selectedProvider, state.apiKey)

                if (apiKey.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = "Please enter an API key for ${state.selectedProvider.displayName}"
                    )
                    return@launch
                }

                val service = AIServiceFactory.create(state.selectedProvider, apiKey)
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
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun analyzeImageFromFile(file: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isAnalyzing = true,
                error = null,
                analysisResult = null,
                capturedImageUri = Uri.fromFile(file)
            )

            try {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                val base64Image = bitmapToBase64(bitmap)

                val state = _uiState.value
                val apiKey = getApiKey(state.selectedProvider, state.apiKey)

                if (apiKey.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isAnalyzing = false,
                        error = "Please enter an API key for ${state.selectedProvider.displayName}"
                    )
                    return@launch
                }

                val service = AIServiceFactory.create(state.selectedProvider, apiKey)
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
                        _uiState.value = _uiState.value.copy(
                            isAnalyzing = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isAnalyzing = false,
                    error = "Error: ${e.message}"
                )
            }
        }
    }

    fun saveEntry() {
        val state = _uiState.value
        val result = state.analysisResult ?: return

        viewModelScope.launch {
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
                imagePath = state.capturedImageUri?.toString(),
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
            capturedImageUri = null,
            isAnalyzing = false
        )
    }

    private fun getApiKey(provider: AIProvider, userKey: String): String {
        if (userKey.isNotBlank()) return userKey
        return when (provider) {
            AIProvider.CLAUDE -> BuildConfig.CLAUDE_API_KEY
            AIProvider.GEMINI -> BuildConfig.GEMINI_API_KEY
            AIProvider.GROK -> BuildConfig.GROK_API_KEY
        }
    }

    private fun uriToBase64(uri: Uri): String? {
        return try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bitmapToBase64(bitmap)
        } catch (e: Exception) {
            null
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val maxDimension = 1024
        val scaledBitmap = if (bitmap.width > maxDimension || bitmap.height > maxDimension) {
            val scale = maxDimension.toFloat() / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * scale).toInt(),
                (bitmap.height * scale).toInt(),
                true
            )
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}

data class CameraUiState(
    val selectedProvider: AIProvider = AIProvider.CLAUDE,
    val apiKey: String = "",
    val mealType: String = "Snack",
    val isAnalyzing: Boolean = false,
    val analysisResult: AnalysisResult? = null,
    val error: String? = null,
    val capturedImageUri: Uri? = null,
    val saved: Boolean = false
)

data class AnalysisResult(
    val foodName: String,
    val description: String,
    val nutritionInfo: NutritionInfo
)
