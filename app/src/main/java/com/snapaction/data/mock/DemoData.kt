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
                endDate = "2026-08-24",
                endTime = "23:30",
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
                    GroceryItem("g1", "Boneless Chicken Breasts", "2 large lbs", isChecked = true),
                    GroceryItem("g2", "Heavy Whip Cream", "1 cup", isChecked = false),
                    GroceryItem("g3", "Sun-dried Tomatoes in oil", "1/2 cup chopped", isChecked = false),
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
            id = "demo-4",
            category = IntentCategory.BOOKMARK,
            confidenceScore = 0.93,
            imageUri = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis() - 3600000 * 24,
            bookmark = BookmarkDetails(
                headline = "10-Minute Morning Spine Mobility Routine",
                summary = "Quick full-body dynamic routine designed to unlock tight hip flexors and relieve lower back tension after sleeping.",
                keyTakeaways = listOf(
                    "90/90 Hip Switches - 10 smooth reps per side",
                    "Cat-Cow with Thread The Needle - 8 slow breathing cycles",
                    "World's Greatest Stretch - 5 deep holds per leg"
                ),
                sourcePlatform = "Instagram"
            )
        )
    )
}
