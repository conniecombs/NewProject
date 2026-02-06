package com.nutritiontracker.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GeminiAIService(private val apiKey: String) : AIService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    override suspend fun analyzeFood(imageBase64: String): FoodAnalysisResult {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = buildRequestBody(imageBase64)
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey")
                    .addHeader("content-type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext FoodAnalysisResult.Error(
                        "Gemini API error (${response.code}): $responseBody"
                    )
                }

                val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                val candidates = jsonResponse.getAsJsonArray("candidates")
                val textContent = candidates
                    ?.firstOrNull()?.asJsonObject
                    ?.getAsJsonObject("content")
                    ?.getAsJsonArray("parts")
                    ?.firstOrNull()?.asJsonObject
                    ?.get("text")?.asString ?: ""

                NutritionResponseParser.parseResponse(textContent)
            } catch (e: Exception) {
                FoodAnalysisResult.Error("Gemini API error: ${e.message}")
            }
        }
    }

    private fun buildRequestBody(imageBase64: String): String {
        val body = JsonObject().apply {
            add("contents", gson.toJsonTree(listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf(
                            "inline_data" to mapOf(
                                "mime_type" to "image/jpeg",
                                "data" to imageBase64
                            )
                        ),
                        mapOf(
                            "text" to NutritionResponseParser.ANALYSIS_PROMPT
                        )
                    )
                )
            )))
        }
        return gson.toJson(body)
    }
}
