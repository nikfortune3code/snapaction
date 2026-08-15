package com.snapaction.data.mock

import com.snapaction.data.model.*

object DemoData {
    val initialCards = listOf(
        SnapActionCard(
            id = "demo-1",
            category = IntentCategory.EVENT,
            confidenceScore = 0.98,
            imageUri = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis() - 3600000 * 2,
            event = EventDetails(
                title = "Neon Summer Music Festival 2026",
                startDate = "2026-08-24",
                startTime = "18:00",
                location = "Sunset Amphitheater, Austin TX",
                details = "Live electronic music festival featuring international DJs. Gates open at 5:00 PM."
            )
        ),
        SnapActionCard(
            id = "demo-2",
            category = IntentCategory.GROCERY,
            confidenceScore = 0.95,
            imageUri = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis() - 3600000 * 5,
            grocery = GroceryDetails(
                dishName = "Creamy Tuscan Garlic Chicken",
                items = listOf(
                    GroceryItem("g1", "Boneless Chicken Breasts", "2 lbs", isChecked = true),
                    GroceryItem("g2", "Heavy Whip Cream", "1 cup", isChecked = false),
                    GroceryItem("g3", "Sun-dried Tomatoes in oil", "1/2 cup", isChecked = false),
                    GroceryItem("g4", "Fresh Baby Spinach", "2 cups", isChecked = true),
                    GroceryItem("g5", "Garlic Cloves", "4 minced", isChecked = false),
                    GroceryItem("g6", "Grated Parmesan Cheese", "1/2 cup", isChecked = false)
                )
            )
        ),
        SnapActionCard(
            id = "demo-3",
            category = IntentCategory.EXPENSE,
            confidenceScore = 0.99,
            imageUri = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis() - 3600000 * 12,
            expense = ExpenseDetails(
                vendor = "Metro Electric Utility Corp",
                totalAmount = 84.50,
                currency = "USD",
                dueDate = "2026-08-15",
                category = "Utilities",
                isPaid = false
            )
        ),
        SnapActionCard(
            id = "demo-past-expense-1",
            category = IntentCategory.EXPENSE,
            confidenceScore = 0.99,
            imageUri = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis() - 3600000L * 24 * 35, // July 2026
            expense = ExpenseDetails(
                vendor = "Paid to City Water Dept",
                totalAmount = 52.00,
                currency = "USD",
                dueDate = null,
                category = "Utilities",
                isPaid = true,
                isTransactionSms = true
            )
        ),
        SnapActionCard(
            id = "demo-4",
            category = IntentCategory.BOOKMARK,
            confidenceScore = 0.94,
            imageUri = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis() - 3600000 * 18,
            bookmark = BookmarkDetails(
                headline = "10-Minute Morning Spine Mobility Routine",
                summary = "Key stretching and mobility exercises to improve posture and reduce lower back tightness.",
                keyTakeaways = listOf(
                    "Perform cat-cow stretches for 60s every morning",
                    "Hold thoracic extension over foam roller",
                    "Hydrate immediately after waking up"
                ),
                sourcePlatform = "Saved Screenshot Notes"
            )
        )
    )
}
