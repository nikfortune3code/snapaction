package com.snapaction.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class IntentCategory {
    GROCERY,
    EXPENSE,
    EVENT,
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
    val vendor: String, // Heading: e.g. "Paid to Lucky Traders"
    val totalAmount: Double,
    val currency: String = "INR",
    val dueDate: String? = null, // Optional: Set only if applicable (e.g. Electric, Gas, Credit Card bills)
    val category: String = "UPI Payment",
    val isPaid: Boolean = false,
    val isTransactionSms: Boolean = false,
    val rawSmsText: String? = null
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
) {
    fun getMonthYearString(): String {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        return sdf.format(Date(timestamp))
    }
}

/**
 * Represents a single cart item AI-detected from an image.
 * productName and brandName are auto-filled by Vision AI;
 * quantity is entered manually by the user.
 */
@Serializable
data class CartItem(
    val id: String,
    val imageUri: String,                // The source image URI
    val productName: String,             // Auto-filled from AI
    val brandName: String? = null,       // Null if brand not detected
    val quantity: Int = 1,               // User-editable
    val timestamp: Long = System.currentTimeMillis(),
    val isPurchased: Boolean = false,    // Ticked manually as purchased
    val purchaseDate: String? = null     // YYYY-MM-DD format date of purchase
)
