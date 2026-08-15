package com.snapaction.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
            // Header Row: Category Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(category = card.category, isSms = card.expense?.isTransactionSms == true)
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

            // Main Body
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(card.imageUri),
                    contentDescription = "Original Reference",
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onEditCard(card) },
                    contentScale = ContentScale.Crop
                )

                Column(modifier = Modifier.weight(1f)) {
                    when (card.category) {
                        IntentCategory.EVENT -> EventCardContent(card.event)
                        IntentCategory.GROCERY -> GroceryCardContent(card.id, card.grocery, onToggleGrocery)
                        IntentCategory.EXPENSE -> ExpenseCardContent(card.id, card.expense, onTogglePaid)
                        IntentCategory.BOOKMARK -> BookmarkCardContent(card.bookmark)
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: IntentCategory, isSms: Boolean = false) {
    val (color, icon, label) = when (category) {
        IntentCategory.EVENT -> Triple(EventBadgeColor, Icons.Default.CalendarToday, "REMINDER")
        IntentCategory.GROCERY -> Triple(GroceryBadgeColor, Icons.Default.ShoppingCart, "GROCERIES")
        IntentCategory.EXPENSE -> if (isSms) Triple(ExpenseBadgeColor, Icons.Default.Sms, "SMS TRANSACTION") else Triple(ExpenseBadgeColor, Icons.Default.Receipt, "EXPENSES & BILLS")
        IntentCategory.BOOKMARK -> Triple(BookmarkBadgeColor, Icons.Default.Bookmark, "BOOKMARKS")
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
fun EventCardContent(event: EventDetails?) {
    val context = LocalContext.current
    if (event == null) return

    Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = "📅 ${event.startDate} at ${event.startTime}", style = MaterialTheme.typography.bodySmall)
    if (event.location.isNotBlank()) {
        Text(text = "📍 ${event.location}", style = MaterialTheme.typography.bodySmall)
    }

    Spacer(modifier = Modifier.height(8.dp))
    Button(
        onClick = {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = Uri.parse("content://com.android.calendar/events")
                putExtra("title", event.title)
                putExtra("eventLocation", event.location)
                putExtra("description", event.details)
            }
            context.startActivity(intent)
        },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add to Google Calendar", fontSize = 11.sp)
    }
}

@Composable
fun GroceryCardContent(
    cardId: String,
    grocery: GroceryDetails?,
    onToggle: (String, String) -> Unit
) {
    if (grocery == null) return
    Text(text = grocery.dishName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))

    grocery.items.take(4).forEach { item ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onToggle(cardId, item.id) }
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = { onToggle(cardId, item.id) },
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${item.name} (${item.quantity})",
                style = MaterialTheme.typography.bodySmall,
                color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ExpenseCardContent(
    cardId: String,
    expense: ExpenseDetails?,
    onTogglePaid: (String) -> Unit
) {
    if (expense == null) return

    val formattedAmount = if (expense.currency == "INR") {
        String.format(Locale.US, "₹%.2f", expense.totalAmount)
    } else {
        String.format(Locale.US, "$%.2f %s", expense.totalAmount, expense.currency)
    }

    // 1. Transaction Heading (e.g., "Paid to Lucky Traders")
    Text(text = expense.vendor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    
    Spacer(modifier = Modifier.height(2.dp))

    // 2. Amount below heading
    Text(
        text = formattedAmount,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold,
        color = ExpenseBadgeColor
    )
    
    Spacer(modifier = Modifier.height(2.dp))

    // 3. Category below amount
    if (expense.category.isNotBlank()) {
        Text(
            text = "Category: ${expense.category}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    // Optional Due Date (Shown ONLY if applicable for utility bills)
    if (!expense.dueDate.isNullOrBlank()) {
        Text(
            text = "🗓️ Due Date: ${expense.dueDate}",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.error
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Button(
            onClick = { onTogglePaid(cardId) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (expense.isPaid) MaterialTheme.colorScheme.secondary else ExpenseBadgeColor
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (expense.isPaid) "✓ Paid" else "Mark Paid", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BookmarkCardContent(bookmark: BookmarkDetails?) {
    if (bookmark == null) return
    Text(text = bookmark.headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = bookmark.summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    if (bookmark.keyTakeaways.isNotEmpty()) {
        Spacer(modifier = Modifier.height(4.dp))
        bookmark.keyTakeaways.take(2).forEach { point ->
            Text(text = "• $point", style = MaterialTheme.typography.bodySmall)
        }
    }
}
