package com.nutritiontracker.data.model

data class DailySummary(
    val date: String,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbohydrates: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val totalSugar: Double,
    val totalSodium: Double,
    val entryCount: Int
)
