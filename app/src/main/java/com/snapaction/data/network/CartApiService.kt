package com.snapaction.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Response from /api/analyze-cart-item.
 * productName is always populated; brandName is null if no brand was detected.
 */
data class CartAnalysisResult(
    val productName: String,
    val brandName: String?
)

/**
 * HTTP client that calls the SnapAction Node.js Vision backend
 * to analyze a product image and return productName + brandName.
 *
 * BASE_URL options:
 *  - Emulator  : http://10.0.2.2:3001
 *  - Physical  : http://<YOUR_PC_LAN_IP>:3001
 */
object CartApiService {

    private const val BASE_URL = "http://10.0.2.2:3001"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads [imageFile] to the backend as multipart/form-data and
     * returns a [CartAnalysisResult]. Throws on failure.
     * Must be called from a coroutine.
     */
    suspend fun analyzeCartImage(imageFile: File): CartAnalysisResult = withContext(Dispatchers.IO) {
        val mimeType = when (imageFile.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png"         -> "image/png"
            "webp"        -> "image/webp"
            else          -> "image/jpeg"
        }

        val requestBody = imageFile.readBytes().toRequestBody(mimeType.toMediaTypeOrNull())

        val multipart = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", imageFile.name, requestBody)
            .build()

        val request = Request.Builder()
            .url("$BASE_URL/api/analyze-cart-item")
            .post(multipart)
            .build()

        val response = client.newCall(request).execute()

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            throw Exception("Backend error ${response.code}: $errorBody")
        }

        val responseBody = response.body?.string()
            ?: throw Exception("Empty response from backend")

        parseResponse(responseBody)
    }

    private fun parseResponse(json: String): CartAnalysisResult {
        val root = JSONObject(json)
        val data = root.getJSONObject("data")
        val productName = data.optString("productName", "Unknown Product")
            .ifBlank { "Unknown Product" }
        val brandName = if (data.isNull("brandName")) null
                        else data.optString("brandName", null)?.ifBlank { null }
        return CartAnalysisResult(productName = productName, brandName = brandName)
    }
}
