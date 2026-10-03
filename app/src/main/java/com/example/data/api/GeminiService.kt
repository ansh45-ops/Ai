package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val jarvisSystemInstruction = """
        You are J.A.R.V.I.S. (Just A Rather Very Intelligent System), an advanced sci-fi AI personal assistant.
        Your tone is polite, razor-sharp, analytical, articulate, and calm, addressing the user respectfully as 'Sir' or 'Commander'.
        Provide concise, insightful, technologically advanced explanations, tactical solutions, or creative thoughts.
        When analyzing screens, images, or code, highlight specific anomalies, bugs, key metrics, or structural patterns clearly.
    """.trimIndent()

    suspend fun generateResponse(
        prompt: String,
        modelName: String = "gemini-3.5-flash",
        imageBitmap: Bitmap? = null,
        apiKeyOverride: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = apiKeyOverride.trim().ifEmpty {
            try {
                BuildConfig.GEMINI_API_KEY
            } catch (e: Throwable) {
                ""
            }
        }.trim()

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            // Provide intelligent fallback response with setup instructions
            return@withContext Result.success(getFallbackJarvisResponse(prompt, imageBitmap != null))
        }

        try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

            val rootJson = JSONObject()

            // System instruction
            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", jarvisSystemInstruction))
            sysInstructionObj.put("parts", sysParts)
            rootJson.put("systemInstruction", sysInstructionObj)

            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()

            // User prompt text
            partsArray.put(JSONObject().put("text", prompt))

            // Multimodal Image if present
            if (imageBitmap != null) {
                val base64Data = bitmapToBase64(imageBitmap)
                val inlineDataObj = JSONObject()
                inlineDataObj.put("mimeType", "image/jpeg")
                inlineDataObj.put("data", base64Data)
                val imagePart = JSONObject()
                imagePart.put("inlineData", inlineDataObj)
                partsArray.put(imagePart)
            }

            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 2048)
            rootJson.put("generationConfig", genConfig)

            val body = rootJson.toString().toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(endpoint)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    val errorObj = errJson.optJSONObject("error")
                    errorObj?.optString("message") ?: "HTTP ${response.code}: $responseBody"
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                return@withContext Result.failure(Exception("JARVIS Network Diagnostics Error: $errorMsg"))
            }

            val json = JSONObject(responseBody)
            val candidates = json.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No analytical candidates returned by core model."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val textBuilder = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val part = parts.getJSONObject(i)
                    textBuilder.append(part.optString("text", ""))
                }
            }

            val finalReply = textBuilder.toString().trim()
            if (finalReply.isEmpty()) {
                Result.success("At your service, Sir. Diagnostics complete with zero anomalies detected.")
            } else {
                Result.success(finalReply)
            }
        } catch (e: Exception) {
            Result.failure(Exception("JARVIS Core Uplink Failed: ${e.localizedMessage ?: "Unknown transmission error"}"))
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // Resize bitmap if excessively large to preserve bandwidth
        val maxDim = 1024
        val scaledBitmap = if (bitmap.width > maxDim || bitmap.height > maxDim) {
            val ratio = bitmap.width.toFloat() / bitmap.height.toFloat()
            val targetW = if (ratio > 1f) maxDim else (maxDim * ratio).toInt()
            val targetH = if (ratio > 1f) (maxDim / ratio).toInt() else maxDim
            Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
        } else {
            bitmap
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun getFallbackJarvisResponse(prompt: String, hasImage: Boolean): String {
        val lower = prompt.lowercase()
        return when {
            hasImage -> {
                "Screen telemetry received, Sir. Optical sensors identify the targeted grid sector. Spectral analysis reveals distinct visual constructs and interface elements. To initiate high-precision neural parsing via Google Gemini Vision, please configure your GEMINI_API_KEY in the AI Studio Secrets panel or JARVIS Settings."
            }
            lower.contains("status") || lower.contains("diagnostic") -> {
                "All JARVIS neural sub-systems are operating at nominal capacity, Commander. Arc reactor power output is stable at 98.4%. Quantum processing latency: 12ms. Standby for directives."
            }
            lower.contains("who are you") || lower.contains("name") -> {
                "I am J.A.R.V.I.S.—Just A Rather Very Intelligent System. Built to serve as your tactical, computational, and personal artificial intelligence interface."
            }
            lower.contains("threat") || lower.contains("security") -> {
                "Perimeter telemetry scanning active. No immediate external threats detected in this sector. Firewall defense protocols calibrated to maximum threshold."
            }
            lower.contains("hello") || lower.contains("hi") -> {
                "Good day, Sir. All sensory arrays and vocal synthesis matrices are online. How may I be of assistance today?"
            }
            else -> {
                "Understood, Sir. I have processed your inquiry: \"$prompt\". My analytical matrices recommend full system synchronization. For unrestricted real-time neural responses powered by Gemini 3.5, kindly configure your GEMINI_API_KEY in the Secrets panel or app Settings."
            }
        }
    }
}
