package com.snapaction.data.model

import kotlinx.serialization.SerialName
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
    val endDate: String? = null,
    val endTime: String? = null,
    val location: String = "",
    val details: String = ""
)

@Serializable
data class GroceryItem(
    val id: String,
    val name: String,
    val quantity: String = "1",
    val isChecked: Boolean = false
)

@Serializable
data class GroceryDetails(
    val dishName: String = "Grocery List",
    val items: List<GroceryItem> = emptyList()
)

@Serializable
data class ExpenseDetails(
    val vendor: String,
    val totalAmount: Double,
    val currency: String = "USD",
    val dueDate: String = "",
    val category: String = "General",
    val isPaid: Boolean = false
)

@Serializable
data class BookmarkDetails(
    val headline: String,
    val summary: String,
    val keyTakeaways: List<String> = emptyList(),
    val sourcePlatform: String = "Web / Note"
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
