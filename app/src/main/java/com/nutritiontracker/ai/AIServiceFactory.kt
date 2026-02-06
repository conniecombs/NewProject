package com.nutritiontracker.ai

import com.nutritiontracker.data.model.AIProvider

object AIServiceFactory {
    fun create(provider: AIProvider, apiKey: String): AIService {
        return when (provider) {
            AIProvider.CLAUDE -> ClaudeAIService(apiKey)
            AIProvider.GEMINI -> GeminiAIService(apiKey)
            AIProvider.GROK -> GrokAIService(apiKey)
        }
    }
}
