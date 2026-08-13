package com.snapaction.data.model

import kotlinx.serialization.Serializable

enum class IntentCategory {
    EVENT,
    GROCERY,
    EXPENSE,
    BOOKMARK
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
data class EventDetails(
    val title: String,
    val startDate: String,
    val startTime: String,
    val endDate: String = "",
    val endTime: String = "",
    val location: String = "",
    val details: String = ""
)

@Serializable
data class GroceryItem(
    val id: String,
    val name: String,
    val quantity: String = "",
    val isChecked: Boolean = false
)

@Serializable
data class GroceryDetails(
    val dishName: String,
    val items: List<GroceryItem>
)

@Serializable
data class ExpenseDetails(
    val vendor: String,
    val totalAmount: Double,
    val currency: String = "USD",
    val dueDate: String,
    val category: String = "Utilities",
    val isPaid: Boolean = false
)

@Serializable
data class BookmarkDetails(
    val headline: String,
    val summary: String,
    val keyTakeaways: List<String>,
    val sourcePlatform: String = "Instagram"
)

@Serializable
data class SnapActionCard(
    val id: String,
    val category: IntentCategory,
    val confidenceScore: Double = 0.95,
    val imageUri: String,
    val timestamp: Long = System.currentTimeMillis(),
    val event: EventDetails? = null,
    val grocery: GroceryDetails? = null,
    val expense: ExpenseDetails? = null,
    val bookmark: BookmarkDetails? = null
)
