package com.nutritiontracker.data.model

enum class AIProvider(val displayName: String) {
    CLAUDE("Claude (Anthropic)"),
    GEMINI("Gemini (Google)"),
    GROK("Grok (xAI)")
}
