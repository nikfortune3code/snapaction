package com.snapaction.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.snapaction.data.model.*
import com.snapaction.ui.theme.*
import java.util.Locale

@Composable
fun ActionCardItem(
    card: SnapActionCard,
    onToggleGrocery: (String, String) -> Unit,
    onTogglePaid: (String) -> Unit,
    onEditCard: (SnapActionCard) -> Unit,
    onDeleteCard: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // Header Row: Category Badge + Options Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = card.category)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onEditCard(card) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Card",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDeleteCard(card.id) }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Card",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Body: Screenshot Thumbnail + Dynamic Extracted Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Screenshot Thumbnail
                Image(
                    painter = rememberAsyncImagePainter(card.imageUri),
                    contentDescription = "Original Screenshot",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onEditCard(card) },
                    contentScale = ContentScale.Crop
                )

                // Extracted Card Details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.summaryTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    when (card.category) {
                        ClassificationCategory.BILL_RECEIPT -> BillReceiptContent(card.expenseDetails, card.extractedItems)
                        ClassificationCategory.FOOD_DISH -> FoodDishContent(card.recipeDetails)
                        ClassificationCategory.GROCERY_LIST -> ExtractedItemsListContent(card.extractedItems)
                        ClassificationCategory.PACKAGED_ITEM -> ExtractedItemsListContent(card.extractedItems)
                        ClassificationCategory.OTHER -> OtherNotesContent(card.recipeDetails?.notes)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: ClassificationCategory) {
    val (color, icon, label) = when (category) {
        ClassificationCategory.BILL_RECEIPT -> Triple(ExpenseBadgeColor, Icons.Default.ReceiptLong, "BILL / RECEIPT")
        ClassificationCategory.GROCERY_LIST -> Triple(GroceryBadgeColor, Icons.Default.ShoppingCart, "GROCERY LIST")
        ClassificationCategory.FOOD_DISH -> Triple(EventBadgeColor, Icons.Default.Restaurant, "FOOD DISH / RECIPE")
        ClassificationCategory.PACKAGED_ITEM -> Triple(BookmarkBadgeColor, Icons.Default.Inventory2, "PACKAGED ITEM")
        ClassificationCategory.OTHER -> Triple(BookmarkBadgeColor, Icons.Default.Bookmark, "OTHER / NOTE")
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun BillReceiptContent(expenseDetails: ExpenseDetails?, items: List<ExtractedItem>) {
    if (expenseDetails != null && expenseDetails.totalAmount != null) {
        Text(
            text = String.format(Locale.US, "$%.2f %s", expenseDetails.totalAmount, expenseDetails.currency ?: "USD"),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = ExpenseBadgeColor
        )
        expenseDetails.merchant?.let {
            Text(text = "Merchant: $it", style = MaterialTheme.typography.bodySmall)
        }
        expenseDetails.date?.let {
            Text(text = "Date: $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (items.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        items.take(3).forEach { item ->
            Text(text = "• ${item.name}" + if (item.quantity != null) " (${item.quantity})" else "", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun FoodDishContent(recipeDetails: RecipeDetails?) {
    if (recipeDetails == null) return
    recipeDetails.dishName?.let {
        Text(text = "Dish: $it", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
    if (recipeDetails.estimatedIngredientsRequired.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "Inferred Ingredients:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        recipeDetails.estimatedIngredientsRequired.take(4).forEach { ing ->
            Text(text = "• $ing", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun ExtractedItemsListContent(items: List<ExtractedItem>) {
    items.take(4).forEach { item ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "• ${item.name}" + if (item.quantity != null) " (${item.quantity})" else "",
                style = MaterialTheme.typography.bodySmall
            )
            item.estimatedPrice?.let { price ->
                Text(
                    text = String.format(Locale.US, "$%.2f", price),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun OtherNotesContent(notes: String?) {
    notes?.let {
        Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
