package com.example.data

import android.util.Log
import com.example.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val mediaTypeJson = "application/json; charset=utf-8".toMediaType()

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        responseMimeType: String? = null,
        modelName: String = "gemini-3.5-flash"
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default placeholder!")
            return ""
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val requestJson = JSONObject()
        
        // Contents
        val contentsArr = JSONArray()
        val contentObj = JSONObject()
        val partsArr = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArr.put(partObj)
        contentObj.put("parts", partsArr)
        contentsArr.put(contentObj)
        requestJson.put("contents", contentsArr)

        // System Instruction
        if (systemInstruction != null) {
            val sysInstObj = JSONObject()
            val sysPartsArr = JSONArray()
            val sysPartObj = JSONObject()
            sysPartObj.put("text", systemInstruction)
            sysPartsArr.put(sysPartObj)
            sysInstObj.put("parts", sysPartsArr)
            requestJson.put("systemInstruction", sysInstObj)
        }

        // Generation Config
        if (responseMimeType != null) {
            val genConfig = JSONObject()
            genConfig.put("responseMimeType", responseMimeType)
            requestJson.put("generationConfig", genConfig)
        }

        val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code: ${response.code}, body: $responseBody")
                    return ""
                }
                
                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return parts.getJSONObject(0).optString("text", "")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during generateContent: ${e.message}", e)
        }
        return ""
    }

    suspend fun generateImage(prompt: String): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return null
        }

        // We can use gemini-2.5-flash-image for standard image generation as described in skill guidelines
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-image:generateContent?key=$apiKey"

        val requestJson = JSONObject()
        val contentsArr = JSONArray()
        val contentObj = JSONObject()
        val partsArr = JSONArray()
        val partObj = JSONObject()
        partObj.put("text", prompt)
        partsArr.put(partObj)
        contentObj.put("parts", partsArr)
        contentsArr.put(contentObj)
        requestJson.put("contents", contentsArr)

        val genConfig = JSONObject()
        genConfig.put("responseModalities", JSONArray(listOf("TEXT", "IMAGE")))
        val imageConfig = JSONObject()
        imageConfig.put("aspectRatio", "1:1")
        imageConfig.put("imageSize", "1K")
        genConfig.put("imageConfig", imageConfig)
        requestJson.put("generationConfig", genConfig)

        val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Image generation request failed: ${response.code}, body: $responseBody")
                    return null
                }

                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            for (i in 0 until parts.length()) {
                                val part = parts.getJSONObject(i)
                                val inlineData = part.optJSONObject("inlineData")
                                if (inlineData != null) {
                                    val mimeType = inlineData.optString("mimeType", "")
                                    val data = inlineData.optString("data", "")
                                    if (data.isNotEmpty()) {
                                        return "data:$mimeType;base64,$data"
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating image: ${e.message}", e)
        }
        return null
    }

    suspend fun chatConversation(
        chatHistory: List<JSONObject>,
        customSystemInstruction: String
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return ""
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val requestJson = JSONObject()
        requestJson.put("contents", JSONArray(chatHistory))

        val sysInstObj = JSONObject()
        val sysPartsArr = JSONArray()
        val sysPartObj = JSONObject()
        sysPartObj.put("text", customSystemInstruction)
        sysPartsArr.put(sysPartObj)
        sysInstObj.put("parts", sysPartsArr)
        requestJson.put("systemInstruction", sysInstObj)

        val requestBody = requestJson.toString().toRequestBody(mediaTypeJson)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    Log.e(TAG, "Chat failed with code: ${response.code}")
                    return ""
                }

                val responseJson = JSONObject(responseBody)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return parts.getJSONObject(0).optString("text", "")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during chatConversation: ${e.message}", e)
        }
        return ""
    }
}
