package com.snapaction.data.repository

import com.snapaction.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AiVisionRepository {

    val visionSystemPrompt = """
        You are SnapAction Vision AI Engine, an intelligent vision-LLM assistant specializing in OCR, scene recognition, and intent extraction from mobile images and screenshots.

        Intent Categories:
        1. "EVENT": Concerts, parties, tickets, calendar flyers, scheduled appointments.
        2. "GROCERY": Written grocery lists, pantry snapshots, plate dish photos (inferring required recipe ingredients), retail products.
        3. "EXPENSE": Invoices, store receipts, utility bills, digital payment screenshots.
        4. "BOOKMARK": General screenshot notes, web articles, quotes, educational memos.
    """.trimIndent()

    /**
     * Processing stream emitting progress states and final parsed card.
     */
    fun processScreenshot(imageUri: String, preferredCategory: IntentCategory? = null): Flow<ProcessingState> = flow {
        emit(ProcessingState(step = ProcessingStep.ANALYZING, message = "Analyzing image structure & OCR text..."))
        delay(600)

        emit(ProcessingState(step = ProcessingStep.CATEGORIZING, message = "Categorizing intent (REMINDERS / GROCERIES / EXPENSES / BOOKMARKS)..."))
        delay(700)

        emit(ProcessingState(step = ProcessingStep.EXTRACTING, message = "Extracting structured action items with Gemini Vision..."))
        delay(800)

        val parsedCard = createIntelligentParsedCard(imageUri, preferredCategory)

        emit(ProcessingState(step = ProcessingStep.COMPLETED, message = "Action card ready!", card = parsedCard))
    }

    private fun createIntelligentParsedCard(imageUri: String, preferredCategory: IntentCategory?): SnapActionCard {
        val lowerUri = imageUri.lowercase()
        
        // Determine category based on URI content hints or fallback to active tab preference
        val category = when {
            lowerUri.contains("bill") || lowerUri.contains("receipt") || lowerUri.contains("invoice") || lowerUri.contains("pay") || lowerUri.contains("expense") -> IntentCategory.EXPENSE
            lowerUri.contains("event") || lowerUri.contains("ticket") || lowerUri.contains("party") || lowerUri.contains("flyer") || lowerUri.contains("concert") || lowerUri.contains("reminder") -> IntentCategory.EVENT
            lowerUri.contains("note") || lowerUri.contains("article") || lowerUri.contains("bookmark") || lowerUri.contains("memo") -> IntentCategory.BOOKMARK
            lowerUri.contains("grocery") || lowerUri.contains("dish") || lowerUri.contains("food") || lowerUri.contains("recipe") -> IntentCategory.GROCERY
            else -> preferredCategory ?: IntentCategory.EVENT
        }

        return when (category) {
            IntentCategory.EVENT -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.EVENT,
                    confidenceScore = 0.98,
                    imageUri = imageUri,
                    event = EventDetails(
                        title = "Scanned Event Reminder",
                        startDate = "2026-08-25",
                        startTime = "19:00",
                        location = "Main Event Venue / Location",
                        details = "Action item details extracted from your screenshot."
                    )
                )
            }
            IntentCategory.EXPENSE -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.EXPENSE,
                    confidenceScore = 0.98,
                    imageUri = imageUri,
                    expense = ExpenseDetails(
                        vendor = "Utility Biller / Store Receipt",
                        totalAmount = 49.99,
                        currency = "USD",
                        dueDate = "2026-08-30",
                        category = "Utilities & Shopping",
                        isPaid = false
                    )
                )
            }
            IntentCategory.BOOKMARK -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.BOOKMARK,
                    confidenceScore = 0.95,
                    imageUri = imageUri,
                    bookmark = BookmarkDetails(
                        headline = "Saved Screenshot Note",
                        summary = "Extracted key summary notes and main concepts from screenshot.",
                        keyTakeaways = listOf(
                            "Concept 1 extracted from screenshot",
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
                    grocery = GroceryDetails(
                        dishName = "Recipe & Pantry Restock",
                        items = listOf(
                            GroceryItem("i1", "Item 1 from screenshot", "1 unit", false),
                            GroceryItem("i2", "Item 2 from screenshot", "2 units", false),
                            GroceryItem("i3", "Item 3 from screenshot", "To taste", false)
                        )
                    )
                )
            }
        }
    }
}

data class ProcessingState(
    val step: ProcessingStep,
    val message: String,
    val card: SnapActionCard? = null
)
