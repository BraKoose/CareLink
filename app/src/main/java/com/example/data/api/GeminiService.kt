package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    const val SYSTEM_PROMPT = """You are the CareLink AI Health Educator, an empathetic, factual, and strictly non-judgmental conversational assistant designed specifically for a Ghanaian audience (including People Living with HIV (PLHIV), their HIV-negative partners, and families). 

Your purpose is to provide clear, medically accurate, and accessible sexual health education, HIV medication adherence tips, signs/symptoms, and general Q&A about living positively or preventing HIV.

STRICT PROTOCOLS:
1. MEDICAL ACCURACY & SCOPE: You are restricted STRICTLY to HIV/AIDS, STI, and sexual health topics. If asked about unrelated topics (e.g., sports, technology, general recipes), politely decline: "I'm CareLink's dedicated sexual health assistant. I can only provide guidance on HIV/AIDS education, STI prevention, and sexual health. For other topics, please consult appropriate medical resources."
2. NO DIAGNOSIS: You do not provide definitive diagnoses. Always advise the user to seek an at-home test kit or visit a healthcare center (such as Korle Bu Teaching Hospital, 37 Military Hospital, or local clinics in Ghana) for confirmation.
3. ADHERENCE & ADVICE: For ARVs, PEP, and PrEP, emphasize strict adherence (taking it daily, same time) and clinical pickups.
4. STIGMA REDUCTION: Use uplifting, encouraging language. HIV is manageable. Treat status with absolute dignity. Do not use alarmist, stigmatizing, or judgmental phrasing.
5. PRIVACY: Respect total privacy. Do not ask for or repeat personal identifiers (like real names, phone numbers, locations).
"""

    suspend fun getChatResponse(userPrompt: String, chatHistory: List<Pair<String, String>>): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API Key Configuration Error: Please ensure you have added a valid GEMINI_API_KEY in the AI Studio Secrets Panel. Currently using placeholder."
        }

        try {
            val url = "$BASE_URL?key=$apiKey"
            
            // Build contents array including history
            val contentsArray = JSONArray()
            
            // 1. History
            chatHistory.forEach { (sender, text) ->
                val role = if (sender == "user") "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", role)
                val partsArray = JSONArray()
                val partObj = JSONObject()
                partObj.put("text", text)
                partsArray.put(partObj)
                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
            }
            
            // 2. Current User Message
            val currentMessageObj = JSONObject()
            currentMessageObj.put("role", "user")
            val currentParts = JSONArray()
            val currentPartObj = JSONObject()
            currentPartObj.put("text", userPrompt)
            currentParts.put(currentPartObj)
            currentMessageObj.put("parts", currentParts)
            contentsArray.put(currentMessageObj)

            // 3. Request root object
            val requestBodyJson = JSONObject()
            requestBodyJson.put("contents", contentsArray)

            // 4. System instruction
            val systemInstructionObj = JSONObject()
            val systemPartsArray = JSONArray()
            val systemPartObj = JSONObject()
            systemPartObj.put("text", SYSTEM_PROMPT)
            systemPartsArray.put(systemPartObj)
            systemInstructionObj.put("parts", systemPartsArray)
            requestBodyJson.put("systemInstruction", systemInstructionObj)

            // 5. Config
            val configObj = JSONObject()
            configObj.put("temperature", 0.4) // Slightly lower for medically accurate & factual responses
            requestBodyJson.put("generationConfig", configObj)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = requestBodyJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "API call failed: Code ${response.code}, Body: $errBody")
                return@withContext "Response Error (Code ${response.code}): Unable to connect to the medical knowledge base at the moment."
            }

            val responseBody = response.body?.string() ?: ""
            val jsonResponse = JSONObject(responseBody)
            val candidates = jsonResponse.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                if (content != null) {
                    val parts = content.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text")
                    }
                }
            }
            return@withContext "Empty response: No medically relevant information could be generated. Please try rephrasing."
        } catch (e: Exception) {
            Log.e(TAG, "Exception during API call", e)
            return@withContext "Connection Error: ${e.localizedMessage ?: "Please check your internet connection."}"
        }
    }
}
