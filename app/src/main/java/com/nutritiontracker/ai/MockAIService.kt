package com.nutritiontracker.ai

import com.nutritiontracker.data.model.NutritionInfo
import kotlinx.coroutines.delay

class MockAIService : AIService {

    private val mockFoods = listOf(
        MockFood(
            "Grilled Chicken Salad",
            "A fresh garden salad with grilled chicken breast, mixed greens, cherry tomatoes, and light vinaigrette",
            NutritionInfo(
                calories = 350, protein = 32.0, carbohydrates = 15.0, fat = 18.0,
                fiber = 4.0, sugar = 5.0, sodium = 480.0,
                servingSize = "1 large bowl (~350g)", confidence = 0.88f
            )
        ),
        MockFood(
            "Pepperoni Pizza Slice",
            "A single slice of pepperoni pizza with mozzarella cheese on a thin crust",
            NutritionInfo(
                calories = 298, protein = 12.0, carbohydrates = 34.0, fat = 13.0,
                fiber = 2.0, sugar = 4.0, sodium = 640.0,
                servingSize = "1 slice (~107g)", confidence = 0.92f
            )
        ),
        MockFood(
            "Banana Smoothie",
            "A creamy banana smoothie blended with milk, yogurt, and a hint of honey",
            NutritionInfo(
                calories = 220, protein = 8.0, carbohydrates = 42.0, fat = 3.5,
                fiber = 3.0, sugar = 28.0, sodium = 85.0,
                servingSize = "1 glass (~350ml)", confidence = 0.85f
            )
        ),
        MockFood(
            "Cheeseburger",
            "A beef cheeseburger with lettuce, tomato, onion, and special sauce on a sesame bun",
            NutritionInfo(
                calories = 540, protein = 28.0, carbohydrates = 40.0, fat = 30.0,
                fiber = 2.0, sugar = 8.0, sodium = 920.0,
                servingSize = "1 burger (~220g)", confidence = 0.90f
            )
        ),
        MockFood(
            "Sushi Roll (California)",
            "California roll with crab, avocado, and cucumber wrapped in rice and seaweed",
            NutritionInfo(
                calories = 255, protein = 9.0, carbohydrates = 38.0, fat = 7.0,
                fiber = 3.0, sugar = 6.0, sodium = 530.0,
                servingSize = "8 pieces (~180g)", confidence = 0.87f
            )
        ),
        MockFood(
            "Latte",
            "A medium-sized caffè latte made with steamed whole milk",
            NutritionInfo(
                calories = 190, protein = 10.0, carbohydrates = 18.0, fat = 7.0,
                fiber = 0.0, sugar = 17.0, sodium = 150.0,
                servingSize = "1 medium (~400ml)", confidence = 0.93f
            )
        )
    )

    override suspend fun analyzeFood(imageBase64: String): FoodAnalysisResult {
        // Simulate network delay
        delay(1500)

        val food = mockFoods.random()
        return FoodAnalysisResult.Success(
            foodName = food.name,
            nutritionInfo = food.nutritionInfo,
            description = food.description
        )
    }
}

private data class MockFood(
    val name: String,
    val description: String,
    val nutritionInfo: NutritionInfo
)
