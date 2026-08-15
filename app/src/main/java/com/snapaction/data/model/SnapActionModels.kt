package com.snapaction.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class ClassificationCategory {
    BILL_RECEIPT,
    GROCERY_LIST,
    FOOD_DISH,
    PACKAGED_ITEM,
    OTHER
}

enum class ProcessingStep {
    IDLE,
    ANALYZING,
    CATEGORIZING,
    EXTRACTING,
    COMPLETED,
    ERROR
}

@Serializable
data class ExpenseDetails(
    @SerialName("is_expense") val isExpense: Boolean = false,
    val merchant: String? = null,
    @SerialName("total_amount") val totalAmount: Double? = null,
    val currency: String? = null,
    val date: String? = null
)

@Serializable
data class ExtractedItem(
    val name: String,
    val quantity: String? = null,
    @SerialName("estimated_price") val estimatedPrice: Double? = null
)

@Serializable
data class RecipeDetails(
    @SerialName("dish_name") val dishName: String? = null,
    @SerialName("estimated_ingredients_required") val estimatedIngredientsRequired: List<String> = emptyList(),
    val notes: String? = null
)

@Serializable
data class SnapActionAnalysisResponse(
    @SerialName("detected_category") val detectedCategory: ClassificationCategory,
    @SerialName("confidence_score") val confidenceScore: Double,
    @SerialName("summary_title") val summaryTitle: String,
    @SerialName("expense_details") val expenseDetails: ExpenseDetails? = null,
    @SerialName("extracted_items") val extractedItems: List<ExtractedItem> = emptyList(),
    @SerialName("recipe_details") val recipeDetails: RecipeDetails? = null
)

@Serializable
data class SnapActionCard(
    val id: String,
    val category: ClassificationCategory,
    val confidenceScore: Double = 0.95,
    val imageUri: String,
    val summaryTitle: String,
    val timestamp: Long = System.currentTimeMillis(),
    val expenseDetails: ExpenseDetails? = null,
    val extractedItems: List<ExtractedItem> = emptyList(),
    val recipeDetails: RecipeDetails? = null
)
