package com.nutritiontracker.ai

import com.nutritiontracker.data.model.NutritionInfo

interface AIService {
    suspend fun analyzeFood(imageBase64: String): FoodAnalysisResult
}

sealed class FoodAnalysisResult {
    data class Success(
        val foodName: String,
        val nutritionInfo: NutritionInfo,
        val description: String
    ) : FoodAnalysisResult()

    data class Error(val message: String) : FoodAnalysisResult()
}
