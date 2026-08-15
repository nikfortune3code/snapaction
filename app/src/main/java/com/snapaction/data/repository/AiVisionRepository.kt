package com.snapaction.data.repository

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.snapaction.BuildConfig
import com.snapaction.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class ProcessingState(
    val step: ProcessingStep,
    val message: String,
    val card: SnapActionCard? = null
)

class AiVisionRepository {

    companion object {
        private const val TAG = "AiVisionRepository"
        // Gemini Vision REST API endpoint (works on any device, no server relay needed)
        private const val GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"
    }

    /**
     * Processing stream emitting progress states and final parsed card.
     * Calls Gemini Vision API directly from Android with the real image bytes.
     */
    fun processScreenshot(context: Context? = null, imageUri: String, preferredCategory: IntentCategory? = null): Flow<ProcessingState> = flow {
        emit(ProcessingState(step = ProcessingStep.ANALYZING, message = "Reading receipt image pixels & shop headers..."))
        delay(300)

        emit(ProcessingState(step = ProcessingStep.CATEGORIZING, message = "Sending image to Gemini Vision AI..."))
        delay(300)

        emit(ProcessingState(step = ProcessingStep.EXTRACTING, message = "Extracting Shop Name & Bill Amount..."))

        val parsedCard = analyzeWithGeminiDirect(context, imageUri, preferredCategory)

        emit(ProcessingState(step = ProcessingStep.COMPLETED, message = "Action card ready!", card = parsedCard))
    }

    private suspend fun analyzeWithGeminiDirect(
        context: Context?,
        imageUri: String,
        preferredCategory: IntentCategory?
    ): SnapActionCard = withContext(Dispatchers.IO) {

        val apiKey = BuildConfig.GEMINI_API_KEY.trim()

        if (apiKey.isBlank() || apiKey == "your_gemini_api_key_here") {
            Log.w(TAG, "Gemini API key not configured — using fallback card")
            return@withContext createFallbackCard(imageUri, preferredCategory)
        }

        val imageBytes = getBytesFromUri(context, imageUri)
        if (imageBytes == null || imageBytes.isEmpty()) {
            Log.w(TAG, "Could not read image bytes from URI: $imageUri — using fallback card")
            return@withContext createFallbackCard(imageUri, preferredCategory)
        }

        try {
            val base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
            val mimeType = guessMimeType(imageUri)

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
                                    You are a receipt OCR assistant. Analyze this image carefully.
                                    
                                    Extract the following information:
                                    1. shop_name: The exact name of the shop, store, restaurant, or vendor printed on the receipt or bill (e.g. "D-Mart", "Reliance Fresh", "Swiggy", "BSNL", "HDFC Bank"). If not a receipt, write "Unknown".
                                    2. total_amount: The final total amount charged in numbers only (e.g. 450.50). Look for words like Total, Grand Total, Amount Due, Net Payable, Bill Amount. Return 0 if not found.
                                    3. category: One of: BILL_RECEIPT, GROCERY_LIST, FOOD_DISH, EVENT, OTHER
                                    4. expense_type: One of: Utilities, Food & Dining, Retail Shopping, Credit Card Bill, Medical, Fuel, Entertainment, Other
                                    
                                    Return ONLY valid JSON in exactly this format:
                                    {
                                      "shop_name": "...",
                                      "total_amount": 0.0,
                                      "category": "BILL_RECEIPT",
                                      "expense_type": "Retail Shopping"
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

            val apiUrl = "$GEMINI_API_URL?key=$apiKey"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.connectTimeout = 8000
            connection.readTimeout = 15000

            OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "Gemini API response code: $responseCode")

            if (responseCode == 200) {
                val responseText = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                    .use { it.readText() }
                Log.d(TAG, "Gemini API raw response: $responseText")

                return@withContext parseGeminiResponse(responseText, imageUri) ?: createFallbackCard(imageUri, preferredCategory)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.readText() ?: "No error body"
                Log.e(TAG, "Gemini API error $responseCode: $errorText")
                return@withContext createFallbackCard(imageUri, preferredCategory)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini Vision API", e)
            return@withContext createFallbackCard(imageUri, preferredCategory)
        }
    }

    private fun parseGeminiResponse(responseText: String, imageUri: String): SnapActionCard? {
        return try {
            val json = JSONObject(responseText)
            val textContent = json
                .getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
                .trim()

            Log.d(TAG, "Gemini extracted text: $textContent")

            // Strip markdown fences if present
            val cleanJson = textContent
                .replace(Regex("^```json\\s*", RegexOption.MULTILINE), "")
                .replace(Regex("^```\\s*", RegexOption.MULTILINE), "")
                .replace(Regex("```\\s*$", RegexOption.MULTILINE), "")
                .trim()

            val parsed = JSONObject(cleanJson)
            val shopName = parsed.optString("shop_name", "").trim()
            val totalAmount = parsed.optDouble("total_amount", 0.0)
            val category = parsed.optString("category", "BILL_RECEIPT")
            val expenseType = parsed.optString("expense_type", "Store Receipt")

            val cardCategory = when (category) {
                "GROCERY_LIST" -> IntentCategory.GROCERY
                "FOOD_DISH" -> IntentCategory.GROCERY
                "EVENT" -> IntentCategory.EVENT
                "OTHER" -> IntentCategory.BOOKMARK
                else -> IntentCategory.EXPENSE
            }

            SnapActionCard(
                id = UUID.randomUUID().toString(),
                category = cardCategory,
                confidenceScore = 0.99,
                imageUri = imageUri,
                timestamp = System.currentTimeMillis(),
                expense = if (cardCategory == IntentCategory.EXPENSE) ExpenseDetails(
                    vendor = if (shopName.isNotBlank() && shopName != "Unknown") shopName else "Captured Receipt",
                    totalAmount = if (totalAmount > 0) totalAmount else 0.0,
                    currency = "INR",
                    dueDate = null,
                    category = expenseType,
                    isPaid = false
                ) else null,
                grocery = if (cardCategory == IntentCategory.GROCERY) GroceryDetails(
                    dishName = shopName.ifBlank { "Grocery List" },
                    items = listOf()
                ) else null,
                event = if (cardCategory == IntentCategory.EVENT) EventDetails(
                    title = shopName.ifBlank { "Scanned Event" },
                    startDate = "", startTime = "", location = "", details = ""
                ) else null,
                bookmark = if (cardCategory == IntentCategory.BOOKMARK) BookmarkDetails(
                    headline = shopName.ifBlank { "Saved Note" },
                    summary = "Extracted from uploaded screenshot",
                    keyTakeaways = listOf(),
                    sourcePlatform = "Screenshot"
                ) else null
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response", e)
            null
        }
    }

    private fun guessMimeType(imageUri: String): String {
        val lower = imageUri.lowercase()
        return when {
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".gif") -> "image/gif"
            else -> "image/jpeg"
        }
    }

    private fun getBytesFromUri(context: Context?, imageUriString: String): ByteArray? {
        return try {
            if (context != null && imageUriString.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(imageUriString))?.use { it.readBytes() }
            } else {
                val cleanPath = when {
                    imageUriString.startsWith("file://") -> imageUriString.substring(7)
                    else -> imageUriString
                }
                val file = File(cleanPath)
                if (file.exists()) file.readBytes() else null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read bytes from URI: $imageUriString", e)
            null
        }
    }

    private fun createFallbackCard(imageUri: String, preferredCategory: IntentCategory?): SnapActionCard {
        val lowerUri = imageUri.lowercase()

        val (shopName, totalAmount, expenseCategory) = when {
            lowerUri.contains("electric") || lowerUri.contains("power") -> Triple("Electric Bill", 0.0, "Utilities")
            lowerUri.contains("gas") -> Triple("Gas Bill", 0.0, "Utilities")
            lowerUri.contains("card") || lowerUri.contains("credit") -> Triple("Credit Card Bill", 0.0, "Credit Card")
            else -> Triple("Captured Receipt", 0.0, "Store Receipt")
        }

        val category = preferredCategory ?: IntentCategory.EXPENSE

        return SnapActionCard(
            id = UUID.randomUUID().toString(),
            category = category,
            confidenceScore = 0.50,
            imageUri = imageUri,
            timestamp = System.currentTimeMillis(),
            expense = if (category == IntentCategory.EXPENSE) ExpenseDetails(
                vendor = shopName,
                totalAmount = totalAmount,
                currency = "INR",
                dueDate = null,
                category = expenseCategory,
                isPaid = false
            ) else null
        )
    }
}
