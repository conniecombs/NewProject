package com.nutritiontracker.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nutritiontracker.data.model.NutritionInfo

object NutritionResponseParser {

    const val ANALYSIS_PROMPT = """Analyze this food/beverage image and provide nutritional information.

Respond ONLY with a valid JSON object in this exact format, no other text:
{
    "food_name": "Name of the food or beverage",
    "description": "Brief description of what you see",
    "serving_size": "Estimated serving size (e.g., '1 cup', '250ml', '1 plate')",
    "calories": 0,
    "protein_g": 0.0,
    "carbohydrates_g": 0.0,
    "fat_g": 0.0,
    "fiber_g": 0.0,
    "sugar_g": 0.0,
    "sodium_mg": 0.0,
    "confidence": 0.85
}

If you cannot identify the food, set confidence to 0.0 and use "Unknown food item" as the name.
Provide your best estimate for nutritional values based on the apparent portion size."""

    fun parseResponse(responseText: String): FoodAnalysisResult {
        return try {
            val jsonStr = extractJson(responseText)
            val gson = Gson()
            val json = gson.fromJson(jsonStr, JsonObject::class.java)

            val foodName = json.get("food_name")?.asString ?: "Unknown Food"
            val description = json.get("description")?.asString ?: ""

            val nutritionInfo = NutritionInfo(
                calories = json.get("calories")?.asInt ?: 0,
                protein = json.get("protein_g")?.asDouble ?: 0.0,
                carbohydrates = json.get("carbohydrates_g")?.asDouble ?: 0.0,
                fat = json.get("fat_g")?.asDouble ?: 0.0,
                fiber = json.get("fiber_g")?.asDouble ?: 0.0,
                sugar = json.get("sugar_g")?.asDouble ?: 0.0,
                sodium = json.get("sodium_mg")?.asDouble ?: 0.0,
                servingSize = json.get("serving_size")?.asString ?: "",
                confidence = json.get("confidence")?.asFloat ?: 0f
            )

            FoodAnalysisResult.Success(foodName, nutritionInfo, description)
        } catch (e: Exception) {
            FoodAnalysisResult.Error("Failed to parse AI response: ${e.message}")
        }
    }

    private fun extractJson(text: String): String {
        val trimmed = text.trim()
        // Try to find JSON object in the response
        val startIndex = trimmed.indexOf('{')
        val endIndex = trimmed.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return trimmed.substring(startIndex, endIndex + 1)
        }
        return trimmed
    }
}
