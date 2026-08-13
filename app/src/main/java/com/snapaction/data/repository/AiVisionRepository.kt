package com.snapaction.data.repository

import com.snapaction.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AiVisionRepository {

    val visionSystemPrompt = """
        You are SnapAction Vision AI Engine, an intelligent mobile assistant specializing in OCR, scene analysis, and intent classification from mobile screenshots.

        Analyze the input screenshot image carefully. First, identify the primary intent category of the content from one of the following 4 categories:
        1. "EVENT": Flyers, concert posters, invitation cards, calendar screenshots, webinars, party invites.
        2. "GROCERY": Recipes, cooking ingredients, shopping lists, pantry items, Instagram food posts.
        3. "EXPENSE": Electricity/utility bills, receipts, invoices, subscription charges, payment reminders.
        4. "BOOKMARK": Social media posts (Instagram, X/Twitter, WhatsApp, Reddit), text notes, articles, book highlights, gym routines.

        Return ONLY a single valid JSON object adhering strictly to the schema below:
        {
          "category": "EVENT" | "GROCERY" | "EXPENSE" | "BOOKMARK",
          "confidenceScore": 0.95,
          "event": { "title": "", "startDate": "YYYY-MM-DD", "startTime": "HH:MM", "location": "", "details": "" },
          "grocery": { "dishName": "", "items": [{ "id": "1", "name": "", "quantity": "", "checked": false }] },
          "expense": { "vendor": "", "totalAmount": 0.0, "currency": "USD", "dueDate": "YYYY-MM-DD", "category": "Utilities", "isPaid": false },
          "bookmark": { "headline": "", "summary": "", "keyTakeaways": [""], "sourcePlatform": "Instagram" }
        }
    """.trimIndent()

    /**
     * Simulates live multi-step processing stream emitting progress states and final parsed card.
     */
    fun processScreenshot(imageUri: String): Flow<ProcessingState> = flow {
        emit(ProcessingState(step = ProcessingStep.ANALYZING, message = "Analyzing image structure & OCR text..."))
        delay(700)

        emit(ProcessingState(step = ProcessingStep.CATEGORIZING, message = "Categorizing intent (Event / Grocery / Expense / Bookmark)..."))
        delay(800)

        emit(ProcessingState(step = ProcessingStep.EXTRACTING, message = "Extracting structured JSON action items..."))
        delay(900)

        // Generate intelligent parsed card based on imageUri / filename heuristics or fallback simulation
        val parsedCard = createIntelligentParsedCard(imageUri)

        emit(ProcessingState(step = ProcessingStep.COMPLETED, message = "Action card ready!", card = parsedCard))
    }

    private fun createIntelligentParsedCard(imageUri: String): SnapActionCard {
        val lowerUri = imageUri.lowercase()
        return when {
            lowerUri.contains("bill") || lowerUri.contains("receipt") || lowerUri.contains("invoice") || lowerUri.contains("payment") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.EXPENSE,
                    confidenceScore = 0.97,
                    imageUri = imageUri,
                    expense = ExpenseDetails(
                        vendor = "Utility Provider Inc",
                        totalAmount = 142.75,
                        currency = "USD",
                        dueDate = "2026-09-01",
                        category = "Utilities",
                        isPaid = false
                    )
                )
            }
            lowerUri.contains("recipe") || lowerUri.contains("food") || lowerUri.contains("ingredient") || lowerUri.contains("cook") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.GROCERY,
                    confidenceScore = 0.94,
                    imageUri = imageUri,
                    grocery = GroceryDetails(
                        dishName = "Avocado Toast & Poached Eggs",
                        items = listOf(
                            GroceryItem("i1", "Sourdough Bread Slices", "2 thick slices"),
                            GroceryItem("i2", "Ripe Hass Avocados", "2 medium"),
                            GroceryItem("i3", "Organic Eggs", "2 large"),
                            GroceryItem("i4", "Chili Flakes & Everything Seasoning", "1 pinch")
                        )
                    )
                )
            }
            lowerUri.contains("event") || lowerUri.contains("ticket") || lowerUri.contains("party") || lowerUri.contains("concert") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.EVENT,
                    confidenceScore = 0.99,
                    imageUri = imageUri,
                    event = EventDetails(
                        title = "Tech & AI Product Summit 2026",
                        startDate = "2026-09-10",
                        startTime = "09:00",
                        endDate = "2026-09-10",
                        endTime = "17:00",
                        location = "Convention Center Hall B",
                        details = "Keynotes on Gemini Vision API and Mobile AI Design Systems."
                    )
                )
            }
            else -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = IntentCategory.BOOKMARK,
                    confidenceScore = 0.91,
                    imageUri = imageUri,
                    bookmark = BookmarkDetails(
                        headline = "Screenshot Note Capture",
                        summary = "Captured text snippet and key details from social post.",
                        keyTakeaways = listOf(
                            "Review target schedule",
                            "Verify team checklist",
                            "Follow up before end of week"
                        ),
                        sourcePlatform = "Mobile Screenshot"
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
