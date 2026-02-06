package com.nutritiontracker.ai

import kotlinx.coroutines.delay
import java.io.IOException

class RetryingAIService(
    private val delegate: AIService,
    private val maxRetries: Int = 3,
    private val initialDelayMs: Long = 2000
) : AIService {

    override suspend fun analyzeFood(imageBase64: String): FoodAnalysisResult {
        var lastError: FoodAnalysisResult.Error? = null

        for (attempt in 0..maxRetries) {
            val result = delegate.analyzeFood(imageBase64)

            when (result) {
                is FoodAnalysisResult.Success -> return result
                is FoodAnalysisResult.Error -> {
                    lastError = result

                    // Only retry on network/server errors, not client errors
                    if (!isRetryable(result.message)) {
                        return result
                    }

                    if (attempt < maxRetries) {
                        val delayMs = initialDelayMs * (1L shl attempt) // exponential backoff
                        delay(delayMs)
                    }
                }
            }
        }

        return lastError ?: FoodAnalysisResult.Error("Unknown error after $maxRetries retries")
    }

    private fun isRetryable(errorMessage: String): Boolean {
        val retryablePatterns = listOf(
            "timeout", "timed out",
            "connection", "network",
            "503", "502", "500", "429",
            "IOException", "SocketException",
            "UnknownHostException"
        )
        val lowerMessage = errorMessage.lowercase()
        return retryablePatterns.any { lowerMessage.contains(it) }
    }
}
