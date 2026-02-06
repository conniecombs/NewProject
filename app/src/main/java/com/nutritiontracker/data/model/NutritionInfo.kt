package com.nutritiontracker.data.model

data class NutritionInfo(
    val calories: Int = 0,
    val protein: Double = 0.0,
    val carbohydrates: Double = 0.0,
    val fat: Double = 0.0,
    val fiber: Double = 0.0,
    val sugar: Double = 0.0,
    val sodium: Double = 0.0,
    val servingSize: String = "",
    val confidence: Float = 0f
)
