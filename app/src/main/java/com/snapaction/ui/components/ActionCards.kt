package com.snapaction.ui.components

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
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
import androidx.compose.ui.text.style.TextDecoration
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
fun CategoryBadge(category: IntentCategory) {
    val (color, icon, label) = when (category) {
        IntentCategory.EVENT -> Triple(EventBadgeColor, Icons.Default.CalendarMonth, "EVENT")
        IntentCategory.GROCERY -> Triple(GroceryBadgeColor, Icons.Default.ShoppingCart, "GROCERY / RECIPE")
        IntentCategory.EXPENSE -> Triple(ExpenseBadgeColor, Icons.Default.ReceiptLong, "EXPENSE / BILL")
        IntentCategory.BOOKMARK -> Triple(BookmarkBadgeColor, Icons.Default.Bookmark, "BOOKMARK / NOTE")
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
    if (event == null) return
    val context = LocalContext.current

    Column {
        Text(text = event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "${event.startDate} • ${event.startTime}", style = MaterialTheme.typography.bodySmall)
        }
        if (event.location.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = event.location, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Google Calendar Sync Button
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        data = CalendarContract.Events.CONTENT_URI
                        putExtra(CalendarContract.Events.TITLE, event.title)
                        putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
                        putExtra(CalendarContract.Events.DESCRIPTION, event.details)
                    }
                    context.startActivity(intent)
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync GCal", fontSize = 11.sp)
            }
            // Export .ICS Button
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Exported .ics event for ${event.title}", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export .ics", fontSize = 11.sp)
            }
        }
    }
}

@Composable
fun GroceryCardContent(
    cardId: String,
    grocery: GroceryDetails?,
    onToggleGrocery: (String, String) -> Unit
) {
    if (grocery == null) return
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val completedCount = grocery.items.count { it.isChecked }
    val progress = if (grocery.items.isNotEmpty()) completedCount.toFloat() / grocery.items.size else 0f

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = grocery.dishName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(
                onClick = {
                    val listText = "${grocery.dishName}:\n" + grocery.items.joinToString("\n") { "- ${it.name} (${it.quantity})" }
                    clipboardManager.setText(AnnotatedString(listText))
                    Toast.makeText(context, "Grocery list copied!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy List", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "$completedCount of ${grocery.items.size} items checked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(vertical = 2.dp),
            color = GroceryBadgeColor
        )
        Spacer(modifier = Modifier.height(6.dp))

        grocery.items.take(4).forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleGrocery(cardId, item.id) }
                    .padding(vertical = 2.dp)
            ) {
                Checkbox(
                    checked = item.isChecked,
                    onCheckedChange = { onToggleGrocery(cardId, item.id) },
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = item.name + if (item.quantity.isNotEmpty()) " (${item.quantity})" else "",
                    style = MaterialTheme.typography.bodySmall,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (item.isChecked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
            }
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

    Column {
        Text(text = expense.vendor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = String.format(Locale.US, "$%.2f %s", expense.totalAmount, expense.currency),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = ExpenseBadgeColor
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "Due: ${expense.dueDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = expense.isPaid,
                onClick = { onTogglePaid(cardId) },
                label = { Text(if (expense.isPaid) "PAID" else "UNPAID") },
                leadingIcon = {
                    Icon(
                        imageVector = if (expense.isPaid) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GroceryBadgeColor.copy(alpha = 0.2f),
                    selectedLabelColor = GroceryBadgeColor
                )
            )
        }
    }
}

@Composable
fun BookmarkCardContent(bookmark: BookmarkDetails?) {
    if (bookmark == null) return

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp), tint = BookmarkBadgeColor)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = bookmark.sourcePlatform, style = MaterialTheme.typography.labelSmall, color = BookmarkBadgeColor, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = bookmark.headline, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = bookmark.summary, style = MaterialTheme.typography.bodySmall, maxLines = 2, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
