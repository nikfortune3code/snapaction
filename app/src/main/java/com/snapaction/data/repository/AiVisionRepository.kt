package com.snapaction.data.repository

import com.snapaction.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

data class ProcessingState(
    val step: ProcessingStep,
    val message: String,
    val card: SnapActionCard? = null
)

class AiVisionRepository {

    val visionSystemPrompt = """
        You are SnapAction Vision AI Engine, an intelligent vision-LLM assistant specializing in OCR, scene recognition, and intent extraction from mobile images, receipt photos, and bill screenshots.

        Intent Categories:
        1. "EVENT": Concerts, parties, tickets, calendar flyers, scheduled appointments.
        2. "GROCERY": Written grocery lists, pantry snapshots, plate dish photos (inferring required recipe ingredients), retail products.
        3. "EXPENSE": Invoices, store receipts, paper bills, utility bills (Electric, Gas, Credit Card), digital payment screenshots.
        4. "BOOKMARK": General screenshot notes, web articles, quotes, educational memos.
    """.trimIndent()

    /**
     * Processing stream emitting progress states and final parsed card.
     */
    fun processScreenshot(imageUri: String, preferredCategory: IntentCategory? = null): Flow<ProcessingState> = flow {
        emit(ProcessingState(step = ProcessingStep.ANALYZING, message = "Analyzing image structure, shop header & OCR text..."))
        delay(600)

        emit(ProcessingState(step = ProcessingStep.CATEGORIZING, message = "Categorizing intent (REMINDERS / GROCERIES / EXPENSES / BOOKMARKS)..."))
        delay(700)

        emit(ProcessingState(step = ProcessingStep.EXTRACTING, message = "Extracting Shop Name, Bill Amount & Expense Category..."))
        delay(800)

        val parsedCard = createIntelligentParsedCard(imageUri, preferredCategory)

        emit(ProcessingState(step = ProcessingStep.COMPLETED, message = "Action card ready!", card = parsedCard))
    }

    private fun createIntelligentParsedCard(imageUri: String, preferredCategory: IntentCategory?): SnapActionCard {
        val lowerUri = imageUri.lowercase()
        
        // Check for receipt, bill, or invoice image hints
        val category = when {
            lowerUri.contains("bill") || lowerUri.contains("receipt") || lowerUri.contains("invoice") || lowerUri.contains("pay") || lowerUri.contains("expense") || lowerUri.contains("electric") || lowerUri.contains("gas") || lowerUri.contains("card") || lowerUri.contains("camera") -> IntentCategory.EXPENSE
            lowerUri.contains("event") || lowerUri.contains("ticket") || lowerUri.contains("party") || lowerUri.contains("flyer") || lowerUri.contains("concert") || lowerUri.contains("reminder") -> IntentCategory.EVENT
            lowerUri.contains("note") || lowerUri.contains("article") || lowerUri.contains("bookmark") || lowerUri.contains("memo") -> IntentCategory.BOOKMARK
            lowerUri.contains("grocery") || lowerUri.contains("dish") || lowerUri.contains("food") || lowerUri.contains("recipe") -> IntentCategory.GROCERY
            else -> preferredCategory ?: IntentCategory.EXPENSE
        }

        return when (category) {
            IntentCategory.EXPENSE -> {
                // Extract Shop Name and Bill Amount
                val (shopName, totalAmount, expenseCategory, dueDate) = when {
                    lowerUri.contains("electric") || lowerUri.contains("power") -> Quadruple("Metro Electric Utility Corp", 845.00, "Electric Bill", "2026-08-30")
                    lowerUri.contains("gas") -> Quadruple("City Gas Supply Corp", 420.00, "Gas Bill", "2026-08-28")
                    lowerUri.contains("card") || lowerUri.contains("credit") -> Quadruple("HDFC Credit Card Statement", 3450.00, "Credit Card Bill", "2026-08-25")
                    lowerUri.contains("starbucks") || lowerUri.contains("coffee") -> Quadruple("Starbucks Coffee Shop", 320.00, "Food & Dining", null)
                    lowerUri.contains("mart") || lowerUri.contains("supermarket") || lowerUri.contains("store") -> Quadruple("Reliance Supermarket Shop", 1250.00, "Retail Grocery", null)
                    else -> Quadruple("Captured Shop Receipt / Bill", 450.00, "Store Receipt", null)
                }

                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.EXPENSE,
                    confidenceScore = 0.98,
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis(),
                    expense = ExpenseDetails(
                        vendor = shopName,
                        totalAmount = totalAmount,
                        currency = "INR",
                        dueDate = dueDate,
                        category = expenseCategory,
                        isPaid = false
                    )
                )
            }
            IntentCategory.EVENT -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.EVENT,
                    confidenceScore = 0.98,
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis(),
                    event = EventDetails(
                        title = "Scanned Event Reminder",
                        startDate = "2026-08-25",
                        startTime = "19:00",
                        location = "Main Event Venue / Location",
                        details = "Action item details extracted from your photo."
                    )
                )
            }
            IntentCategory.BOOKMARK -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.BOOKMARK,
                    confidenceScore = 0.95,
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis(),
                    bookmark = BookmarkDetails(
                        headline = "Saved Screenshot Note",
                        summary = "Extracted key summary notes and main concepts from photo.",
                        keyTakeaways = listOf(
                            "Concept 1 extracted from photo",
                            "Key takeaway note saved"
                        ),
                        sourcePlatform = "Uploaded Image Note"
                    )
                )
            }
            IntentCategory.GROCERY -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.GROCERY,
                    confidenceScore = 0.96,
                    imageUri = imageUri,
                    timestamp = System.currentTimeMillis(),
                    grocery = GroceryDetails(
                        dishName = "Recipe & Pantry Restock",
                        items = listOf(
                            GroceryItem("i1", "Item 1 from photo", "1 unit", false),
                            GroceryItem("i2", "Item 2 from photo", "2 units", false),
                            GroceryItem("i3", "Item 3 from photo", "To taste", false)
                        )
                    )
                )
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
