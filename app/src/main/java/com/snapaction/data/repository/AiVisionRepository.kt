package com.snapaction.data.repository

import com.snapaction.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

class AiVisionRepository {

    val visionSystemPrompt = """
        You are SnapAction Vision AI Engine, an intelligent vision-LLM assistant specializing in OCR, scene recognition, and intent extraction from mobile images and screenshots.

        Classification Categories:
        1. "BILL_RECEIPT": Scanned invoices, restaurant/store receipts, digital payment screenshots.
        2. "GROCERY_LIST": Written grocery lists, pantry snapshots, retail grocery items.
        3. "FOOD_DISH": Cooked meals, plate photos, or food dishes (needs recipe/ingredient inference).
        4. "PACKAGED_ITEM": Packaged food/beverage/household products with labels.
        5. "OTHER": Fallback for bookmarks, memos, or general notes.

        JSON Schema (Strict Output):
        Ensure the LLM returns ONLY valid JSON matching this exact structure:
        {
          "detected_category": "BILL_RECEIPT" | "GROCERY_LIST" | "FOOD_DISH" | "PACKAGED_ITEM" | "OTHER",
          "confidence_score": 0.95,
          "summary_title": "string",
          "expense_details": {
            "is_expense": true,
            "merchant": "string or null",
            "total_amount": 0.0,
            "currency": "USD",
            "date": "YYYY-MM-DD or null"
          },
          "extracted_items": [
            {
              "name": "string",
              "quantity": "string or null",
              "estimated_price": 0.0
            }
          ],
          "recipe_details": {
            "dish_name": "string or null",
            "estimated_ingredients_required": ["string"],
            "notes": "string or null"
          }
        }
    """.trimIndent()

    /**
     * Processing stream emitting progress states and final parsed card.
     */
    fun processScreenshot(imageUri: String): Flow<ProcessingState> = flow {
        emit(ProcessingState(step = ProcessingStep.ANALYZING, message = "Analyzing image structure & OCR text..."))
        delay(600)

        emit(ProcessingState(step = ProcessingStep.CATEGORIZING, message = "Categorizing intent (BILL_RECEIPT / GROCERY_LIST / FOOD_DISH / PACKAGED_ITEM / OTHER)..."))
        delay(700)

        emit(ProcessingState(step = ProcessingStep.EXTRACTING, message = "Extracting structured JSON action items..."))
        delay(800)

        val parsedCard = createIntelligentParsedCard(imageUri)

        emit(ProcessingState(step = ProcessingStep.COMPLETED, message = "Action card ready!", card = parsedCard))
    }

    private fun createIntelligentParsedCard(imageUri: String): SnapActionCard {
        val lowerUri = imageUri.lowercase()
        return when {
            lowerUri.contains("bill") || lowerUri.contains("receipt") || lowerUri.contains("invoice") || lowerUri.contains("pay") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = ClassificationCategory.BILL_RECEIPT,
                    confidenceScore = 0.98,
                    imageUri = imageUri,
                    summaryTitle = "Grocery Market Receipt",
                    expenseDetails = ExpenseDetails(
                        isExpense = true,
                        merchant = "Fresh Mart Supermarket",
                        totalAmount = 84.50,
                        currency = "USD",
                        date = "2026-08-15"
                    ),
                    extractedItems = listOf(
                        ExtractedItem(name = "Organic Whole Milk 1 Gal", quantity = "1", estimatedPrice = 4.29),
                        ExtractedItem(name = "Fresh Hass Avocados 4-pack", quantity = "1 pkg", estimatedPrice = 5.99),
                        ExtractedItem(name = "Boneless Chicken Breast 2lb", quantity = "1", estimatedPrice = 12.50)
                    )
                )
            }
            lowerUri.contains("dish") || lowerUri.contains("meal") || lowerUri.contains("food") || lowerUri.contains("cook") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = ClassificationCategory.FOOD_DISH,
                    confidenceScore = 0.96,
                    imageUri = imageUri,
                    summaryTitle = "Creamy Tuscan Garlic Chicken",
                    recipeDetails = RecipeDetails(
                        dishName = "Creamy Tuscan Garlic Chicken",
                        estimatedIngredientsRequired = listOf(
                            "Boneless Chicken Breasts",
                            "Heavy Cream",
                            "Sun-dried Tomatoes",
                            "Baby Spinach",
                            "Garlic Cloves",
                            "Parmesan Cheese"
                        ),
                        notes = "Inferred recipe ingredients from plate photo."
                    )
                )
            }
            lowerUri.contains("packaged") || lowerUri.contains("item") || lowerUri.contains("product") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = ClassificationCategory.PACKAGED_ITEM,
                    confidenceScore = 0.94,
                    imageUri = imageUri,
                    summaryTitle = "Organic Cold-Pressed Almond Milk",
                    extractedItems = listOf(
                        ExtractedItem(name = "Organic Almond Milk Unsweetened 64oz", quantity = "1 bottle", estimatedPrice = 4.99)
                    )
                )
            }
            lowerUri.contains("grocery") || lowerUri.contains("list") || lowerUri.contains("shopping") -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = ClassificationCategory.GROCERY_LIST,
                    confidenceScore = 0.97,
                    imageUri = imageUri,
                    summaryTitle = "Weekly Pantry & Produce List",
                    extractedItems = listOf(
                        ExtractedItem(name = "Bananas", quantity = "1 bunch", estimatedPrice = 1.99),
                        ExtractedItem(name = "Greek Yogurt Vanilla 32oz", quantity = "1 tub", estimatedPrice = 5.49),
                        ExtractedItem(name = "Rolled Oats 42oz", quantity = "1 container", estimatedPrice = 4.79)
                    )
                )
            }
            else -> {
                SnapActionCard(
                    id = UUID.randomUUID().toString(),
                    category = ClassificationCategory.OTHER,
                    confidenceScore = 0.91,
                    imageUri = imageUri,
                    summaryTitle = "Saved Note & Bookmarked Snippet",
                    recipeDetails = RecipeDetails(
                        notes = "Extracted general text snippet and key concepts."
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
