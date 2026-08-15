package com.snapaction.data.mock

import com.snapaction.data.model.*

object DemoData {
    val initialCards = listOf(
        SnapActionCard(
            id = "demo-1",
            category = ClassificationCategory.BILL_RECEIPT,
            confidenceScore = 0.99,
            imageUri = "https://images.unsplash.com/photo-1554224155-8d04cb21cd6c?w=800&auto=format&fit=crop&q=60",
            summaryTitle = "Metro Electric Utility Invoice",
            timestamp = System.currentTimeMillis() - 3600000 * 2,
            expenseDetails = ExpenseDetails(
                isExpense = true,
                merchant = "Metro Electric Utility Corp",
                totalAmount = 84.50,
                currency = "USD",
                date = "2026-08-15"
            ),
            extractedItems = listOf(
                ExtractedItem(name = "Residential Electric Usage kWh", quantity = "420 kWh", estimatedPrice = 84.50)
            )
        ),
        SnapActionCard(
            id = "demo-2",
            category = ClassificationCategory.FOOD_DISH,
            confidenceScore = 0.96,
            imageUri = "https://images.unsplash.com/photo-1604908176997-125f25cc6f3d?w=800&auto=format&fit=crop&q=60",
            summaryTitle = "Creamy Tuscan Garlic Chicken",
            timestamp = System.currentTimeMillis() - 3600000 * 5,
            recipeDetails = RecipeDetails(
                dishName = "Creamy Tuscan Garlic Chicken",
                estimatedIngredientsRequired = listOf(
                    "Boneless Chicken Breasts",
                    "Heavy Whip Cream",
                    "Sun-dried Tomatoes in oil",
                    "Fresh Baby Spinach",
                    "Garlic Cloves",
                    "Grated Parmesan Cheese"
                ),
                notes = "Inferred recipe ingredients from plate photo."
            )
        ),
        SnapActionCard(
            id = "demo-3",
            category = ClassificationCategory.GROCERY_LIST,
            confidenceScore = 0.97,
            imageUri = "https://images.unsplash.com/photo-1542838132-92c53300491e?w=800&auto=format&fit=crop&q=60",
            summaryTitle = "Pantry & Produce Restock Checklist",
            timestamp = System.currentTimeMillis() - 3600000 * 12,
            extractedItems = listOf(
                ExtractedItem(name = "Organic Whole Milk 1 Gal", quantity = "1 gal", estimatedPrice = 4.29),
                ExtractedItem(name = "Fresh Hass Avocados 4-pack", quantity = "1 bag", estimatedPrice = 5.99),
                ExtractedItem(name = "Greek Yogurt Vanilla 32oz", quantity = "1 tub", estimatedPrice = 5.49)
            )
        ),
        SnapActionCard(
            id = "demo-4",
            category = ClassificationCategory.PACKAGED_ITEM,
            confidenceScore = 0.94,
            imageUri = "https://images.unsplash.com/photo-1527661591475-527312dd65f5?w=800&auto=format&fit=crop&q=60",
            summaryTitle = "Cold-Pressed Sparkling Sparkling Water",
            timestamp = System.currentTimeMillis() - 3600000 * 18,
            extractedItems = listOf(
                ExtractedItem(name = "Sparkling Water Grapefruit 12-pack", quantity = "1 case", estimatedPrice = 6.99)
            )
        ),
        SnapActionCard(
            id = "demo-5",
            category = ClassificationCategory.OTHER,
            confidenceScore = 0.92,
            imageUri = "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=800&auto=format&fit=crop&q=60",
            summaryTitle = "10-Minute Morning Spine Mobility Routine",
            timestamp = System.currentTimeMillis() - 3600000 * 24,
            recipeDetails = RecipeDetails(
                notes = "Saved social media snippet & fitness routine note."
            )
        )
    )
}
