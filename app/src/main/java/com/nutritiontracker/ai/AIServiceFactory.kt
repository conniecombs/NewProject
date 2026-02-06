package com.nutritiontracker.ai

import com.nutritiontracker.data.model.AIProvider

object AIServiceFactory {

    fun create(
        provider: AIProvider,
        apiKey: String,
        withRetry: Boolean = false
    ): AIService {
        val baseService: AIService = when (provider) {
            AIProvider.CLAUDE -> ClaudeAIService(apiKey)
            AIProvider.GEMINI -> GeminiAIService(apiKey)
            AIProvider.GROK -> GrokAIService(apiKey)
        }

        return if (withRetry) {
            RetryingAIService(baseService)
        } else {
            baseService
        }
    }

    fun createMock(): AIService = MockAIService()
}
