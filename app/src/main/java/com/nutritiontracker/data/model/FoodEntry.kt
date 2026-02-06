package com.nutritiontracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val protein: Double,
    val carbohydrates: Double,
    val fat: Double,
    val fiber: Double,
    val sugar: Double,
    val sodium: Double,
    val servingSize: String,
    val imagePath: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val mealType: String = "Snack" // Breakfast, Lunch, Dinner, Snack
)
