package com.snapaction.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit
import com.snapaction.BuildConfig

/**
 * Response from analyze-cart-item.
 * productName is always populated; brandName is null if no brand was detected.
 */
data class CartAnalysisResult(
    val productName: String,
    val brandName: String?
)

/**
 * HTTP client that calls Google Gemini Vision API directly from Android
 * to analyze a product image and return productName + brandName.
 * This ensures the app works on any network (cellular data or other Wi-Fi)
 * and on any device without relying on a local server relay.
 */
object CartApiService {

    private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads [imageFile] to Gemini Vision API directly and
     * returns a [CartAnalysisResult]. Throws on failure.
     * Must be called from a coroutine.
     */
    suspend fun analyzeCartImage(imageFile: File): CartAnalysisResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY.trim()

        if (apiKey.isBlank() || apiKey == "your_gemini_api_key_here") {
            throw Exception("Gemini API key is not configured in BuildConfig. Ensure your API key is correctly set in local.properties or your system environment.")
        }

        val imageBytes = imageFile.readBytes()
        if (imageBytes.isEmpty()) {
            throw Exception("Could not read captured product image file.")
        }

        val base64Image = android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        val mimeType = when (imageFile.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "webp"        -> "image/webp"
            else          -> "image/jpeg"
        }

        // Build Gemini Vision API request body
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        // Image part
                        put(JSONObject().apply {
                            put("inline_data", JSONObject().apply {
                                put("mime_type", mimeType)
                                put("data", base64Image)
                            })
                        })
                        // Text prompt part
                        put(JSONObject().apply {
                            put("text", """
                                You are a product recognition assistant. Analyze the image carefully.
                                
                                Identify the primary object/product shown (e.g., watch, keyboard, whiskey bottle, sneakers, headphones, laptop).
                                If a brand name, logo, or text is visible, extract the brand name exactly (e.g., "Apple", "Nike", "Casio", "Sony").
                                If no brand can be identified from the image, return null for brandName — never guess or invent a brand.
                                
                                Return ONLY a valid JSON object in exactly this format:
                                {
                                  "productName": "Concise specific product name",
                                  "brandName": "Brand name or null"
                                }
                            """.trimIndent())
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1)
                put("maxOutputTokens", 512)
            })
        }

        val requestJson = requestBody.toString()
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBodyOk = requestJson.toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$GEMINI_API_URL?key=$apiKey")
            .post(requestBodyOk)
            .build()

        val response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw Exception("Gemini API error ${response.code}: $errorBody")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from Gemini API")

        val json = JSONObject(responseBody)
        val textContent = json
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
            .trim()

        // Strip markdown fences if present
        val cleanJson = textContent
            .replace(Regex("^```json\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("^```\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
            .trim()

        val parsed = JSONObject(cleanJson)
        val productName = parsed.optString("productName", "Unknown Product").ifBlank { "Unknown Product" }
        val brandName = if (parsed.isNull("brandName")) null
                        else parsed.optString("brandName", null)?.ifBlank { null }

        CartAnalysisResult(productName = productName, brandName = brandName)
    }
}
