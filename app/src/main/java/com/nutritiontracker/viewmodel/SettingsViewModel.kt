package com.nutritiontracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nutritiontracker.ai.AIServiceFactory
import com.nutritiontracker.ai.FoodAnalysisResult
import com.nutritiontracker.data.SecureKeyStore
import com.nutritiontracker.data.model.AIProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val keyStore = SecureKeyStore(application)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _uiState.value = SettingsUiState(
            claudeKey = keyStore.getApiKey(AIProvider.CLAUDE),
            geminiKey = keyStore.getApiKey(AIProvider.GEMINI),
            grokKey = keyStore.getApiKey(AIProvider.GROK),
            selectedProvider = keyStore.getSelectedProvider(),
            claudeKeyValid = null,
            geminiKeyValid = null,
            grokKeyValid = null
        )
    }

    fun updateClaudeKey(key: String) {
        _uiState.value = _uiState.value.copy(claudeKey = key, claudeKeyValid = null)
    }

    fun updateGeminiKey(key: String) {
        _uiState.value = _uiState.value.copy(geminiKey = key, geminiKeyValid = null)
    }

    fun updateGrokKey(key: String) {
        _uiState.value = _uiState.value.copy(grokKey = key, grokKeyValid = null)
    }

    fun setSelectedProvider(provider: AIProvider) {
        _uiState.value = _uiState.value.copy(selectedProvider = provider)
        keyStore.saveSelectedProvider(provider)
    }

    fun saveKey(provider: AIProvider) {
        val key = when (provider) {
            AIProvider.CLAUDE -> _uiState.value.claudeKey
            AIProvider.GEMINI -> _uiState.value.geminiKey
            AIProvider.GROK -> _uiState.value.grokKey
        }
        keyStore.saveApiKey(provider, key)
        _uiState.value = _uiState.value.copy(
            saveMessage = "${provider.displayName} key saved securely"
        )
    }

    fun validateKey(provider: AIProvider) {
        val key = when (provider) {
            AIProvider.CLAUDE -> _uiState.value.claudeKey
            AIProvider.GEMINI -> _uiState.value.geminiKey
            AIProvider.GROK -> _uiState.value.grokKey
        }

        if (key.isBlank()) {
            setValidation(provider, false, "API key is empty")
            return
        }

        _uiState.value = _uiState.value.copy(isValidating = true, saveMessage = null)

        viewModelScope.launch {
            try {
                // Quick validation: check key format
                val formatValid = when (provider) {
                    AIProvider.CLAUDE -> key.startsWith("sk-ant-")
                    AIProvider.GEMINI -> key.length >= 20
                    AIProvider.GROK -> key.length >= 20
                }

                if (!formatValid) {
                    setValidation(provider, false, "Invalid key format")
                    return@launch
                }

                // Try a lightweight API call to validate
                val service = AIServiceFactory.create(provider, key)
                // Use a minimal test - send a tiny base64 image
                // A 1x1 white JPEG pixel
                val testImage = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////2wBDAf//////////////////////////////////////////////////////////////////////////////////////wAARCAABAAEDASIAAhEBAxEB/8QAFAABAAAAAAAAAAAAAAAAAAAACf/EABQQAQAAAAAAAAAAAAAAAAAAAAD/xAAUAQEAAAAAAAAAAAAAAAAAAAAA/8QAFBEBAAAAAAAAAAAAAAAAAAAAAP/aAAwDAQACEQMRAD8AKwA//9k="
                val result = service.analyzeFood(testImage)

                when (result) {
                    is FoodAnalysisResult.Success -> {
                        setValidation(provider, true, "Key validated successfully")
                        keyStore.saveApiKey(provider, key)
                    }
                    is FoodAnalysisResult.Error -> {
                        // If the error is about the image being invalid but we got a response,
                        // the key is still valid
                        if (result.message.contains("400") || result.message.contains("parse")) {
                            setValidation(provider, true, "Key is valid (connected to API)")
                            keyStore.saveApiKey(provider, key)
                        } else {
                            setValidation(provider, false, result.message)
                        }
                    }
                }
            } catch (e: Exception) {
                setValidation(provider, false, "Validation failed: ${e.message}")
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }

    private fun setValidation(provider: AIProvider, valid: Boolean, message: String) {
        _uiState.value = when (provider) {
            AIProvider.CLAUDE -> _uiState.value.copy(
                claudeKeyValid = valid, isValidating = false, saveMessage = message
            )
            AIProvider.GEMINI -> _uiState.value.copy(
                geminiKeyValid = valid, isValidating = false, saveMessage = message
            )
            AIProvider.GROK -> _uiState.value.copy(
                grokKeyValid = valid, isValidating = false, saveMessage = message
            )
        }
    }
}

data class SettingsUiState(
    val claudeKey: String = "",
    val geminiKey: String = "",
    val grokKey: String = "",
    val selectedProvider: AIProvider = AIProvider.CLAUDE,
    val claudeKeyValid: Boolean? = null,
    val geminiKeyValid: Boolean? = null,
    val grokKeyValid: Boolean? = null,
    val isValidating: Boolean = false,
    val saveMessage: String? = null
)
