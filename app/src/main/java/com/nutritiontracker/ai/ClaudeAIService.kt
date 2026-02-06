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

class ClaudeAIService(private val apiKey: String) : AIService {

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
                    .url("https://api.anthropic.com/v1/messages")
                    .addHeader("x-api-key", apiKey)
                    .addHeader("anthropic-version", "2023-06-01")
                    .addHeader("content-type", "application/json")
                    .post(requestBody.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    return@withContext FoodAnalysisResult.Error(
                        "Claude API error (${response.code}): $responseBody"
                    )
                }

                val jsonResponse = gson.fromJson(responseBody, JsonObject::class.java)
                val content = jsonResponse.getAsJsonArray("content")
                val textContent = content?.firstOrNull()?.asJsonObject?.get("text")?.asString ?: ""

                NutritionResponseParser.parseResponse(textContent)
            } catch (e: Exception) {
                FoodAnalysisResult.Error("Claude API error: ${e.message}")
            }
        }
    }

    private fun buildRequestBody(imageBase64: String): String {
        val body = JsonObject().apply {
            addProperty("model", "claude-sonnet-4-20250514")
            addProperty("max_tokens", 1024)
            add("messages", gson.toJsonTree(listOf(
                mapOf(
                    "role" to "user",
                    "content" to listOf(
                        mapOf(
                            "type" to "image",
                            "source" to mapOf(
                                "type" to "base64",
                                "media_type" to "image/jpeg",
                                "data" to imageBase64
                            )
                        ),
                        mapOf(
                            "type" to "text",
                            "text" to NutritionResponseParser.ANALYSIS_PROMPT
                        )
                    )
                )
            )))
        }
        return gson.toJson(body)
    }
}
