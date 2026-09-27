package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AiRecommendationInsight
import com.example.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiRecommendationService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun getPersonalizedRecommendations(
        userInterests: List<String>,
        availablePosts: List<Post>,
        customGoalPrompt: String? = null
    ): AiRecommendationInsight = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY" || availablePosts.isEmpty()) {
            return@withContext generateLocalFallbackInsight(userInterests, availablePosts)
        }

        try {
            val postsSummary = availablePosts.take(15).map { post ->
                JSONObject().apply {
                    put("id", post.id)
                    put("author", post.authorName)
                    put("content", post.content.take(100))
                    put("tags", JSONArray(post.tags))
                }
            }

            val promptText = buildString {
                append("You are SYNQ's intelligent social recommendation algorithm. ")
                append("The user is interested in: ${if (userInterests.isEmpty()) "general trending topics" else userInterests.joinToString(", ")}. ")
                if (!customGoalPrompt.isNullOrBlank()) {
                    append("User's current exploration goal: \"$customGoalPrompt\". ")
                }
                append("Here are the candidate posts in JSON: ${JSONArray(postsSummary)}. ")
                append("Return a valid JSON object with the following fields: ")
                append("\"summary\": a friendly 1-2 sentence briefing of why their feed is tailored for them today, ")
                append("\"recommendedTopics\": an array of 3-5 trending topic strings (e.g. [\"#AI\", \"#Design\"]), ")
                append("\"postReasons\": a JSON map where keys are post IDs and values are short 3-6 word badges explaining why it was recommended (e.g. \"Matches your #tech interest\"). ")
                append("Return ONLY the JSON object, no Markdown backticks or commentary.")
            }

            val requestBodyJson = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().apply { put("text", promptText) })
                }
                val contents = JSONArray().apply {
                    put(JSONObject().apply { put("parts", parts) })
                }
                put("contents", contents)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("responseMimeType", "application/json")
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                Log.w("GeminiRec", "API request failed with code: ${response.code}")
                return@withContext generateLocalFallbackInsight(userInterests, availablePosts)
            }

            val responseBody = response.body?.string() ?: return@withContext generateLocalFallbackInsight(userInterests, availablePosts)
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val partsArray = content?.optJSONArray("parts")
            val text = partsArray?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return@withContext generateLocalFallbackInsight(userInterests, availablePosts)
            }

            parseRecommendationResponse(text, userInterests, availablePosts)
        } catch (e: Exception) {
            Log.e("GeminiRec", "Error fetching Gemini recommendations", e)
            generateLocalFallbackInsight(userInterests, availablePosts)
        }
    }

    private fun parseRecommendationResponse(
        rawText: String,
        userInterests: List<String>,
        availablePosts: List<Post>
    ): AiRecommendationInsight {
        return try {
            val cleaned = rawText.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
            val obj = JSONObject(cleaned)
            val summary = obj.optString("summary", "Personalized curation based on your top interests.")
            val topicsJson = obj.optJSONArray("recommendedTopics")
            val topics = mutableListOf<String>()
            if (topicsJson != null) {
                for (i in 0 until topicsJson.length()) {
                    topics.add(topicsJson.getString(i))
                }
            }

            val reasonsObj = obj.optJSONObject("postReasons")
            val reasons = mutableMapOf<String, String>()
            if (reasonsObj != null) {
                val keys = reasonsObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    reasons[key] = reasonsObj.getString(key)
                }
            }

            AiRecommendationInsight(
                summary = summary,
                recommendedTopics = if (topics.isEmpty()) listOf("#Tech", "#AI", "#Creative", "#SYNQ") else topics,
                postReasons = reasons
            )
        } catch (e: Exception) {
            Log.e("GeminiRec", "Failed to parse JSON response: $rawText", e)
            generateLocalFallbackInsight(userInterests, availablePosts)
        }
    }

    private fun generateLocalFallbackInsight(
        userInterests: List<String>,
        availablePosts: List<Post>
    ): AiRecommendationInsight {
        val interestSet = userInterests.map { it.lowercase() }.toSet()
        val postReasons = mutableMapOf<String, String>()

        availablePosts.forEach { post ->
            val matchingTag = post.tags.firstOrNull { interestSet.contains(it.lowercase()) }
            if (matchingTag != null) {
                postReasons[post.id] = "Matches your #$matchingTag interest"
            } else if (post.likesCount > 5) {
                postReasons[post.id] = "Trending across SYNQ"
            } else {
                postReasons[post.id] = "Suggested discovery"
            }
        }

        val primaryTopic = userInterests.firstOrNull() ?: "Discovery"
        return AiRecommendationInsight(
            summary = "Feed curated around your interest in $primaryTopic and trending community moments.",
            recommendedTopics = if (userInterests.isNotEmpty()) {
                userInterests.map { if (it.startsWith("#")) it else "#$it" }
            } else {
                listOf("#Tech", "#AI", "#Innovation", "#Design", "#SYNQ")
            },
            postReasons = postReasons
        )
    }
}
