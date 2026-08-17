package com.snapaction.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.snapaction.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditActionSheet(
    card: SnapActionCard,
    onDismiss: () -> Unit,
    onSave: (SnapActionCard) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(card.category) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit & Verify Action Card",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Category Tab Assignment",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IntentCategory.values().forEach { cat ->
                    val label = when (cat) {
                        IntentCategory.EVENT -> "Reminders"
                        IntentCategory.GROCERY -> "Cart"
                        IntentCategory.EXPENSE -> "Expenses"
                        IntentCategory.BOOKMARK -> "Bookmarks"
                    }
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Original Screenshot Reference",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Image(
                painter = rememberAsyncImagePainter(card.imageUri),
                contentDescription = "Original Screenshot",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedCategory) {
                IntentCategory.EVENT -> {
                    var eventTitle by remember { mutableStateOf(card.event?.title ?: "Scanned Event") }
                    var startDate by remember { mutableStateOf(card.event?.startDate ?: "2026-08-25") }
                    var startTime by remember { mutableStateOf(card.event?.startTime ?: "19:00") }
                    var location by remember { mutableStateOf(card.event?.location ?: "Main Venue") }
                    var details by remember { mutableStateOf(card.event?.details ?: "Extracted reminder details") }

                    OutlinedTextField(
                        value = eventTitle,
                        onValueChange = { eventTitle = it },
                        label = { Text("Event / Reminder Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val updatedEv = EventDetails(title = eventTitle, startDate = startDate, startTime = startTime, location = location, details = details)
                            onSave(card.copy(category = IntentCategory.EVENT, event = updatedEv))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Reminders")
                    }
                }

                IntentCategory.GROCERY -> {
                    var dishName by remember { mutableStateOf(card.grocery?.dishName ?: "Grocery List / Recipe") }

                    OutlinedTextField(
                        value = dishName,
                        onValueChange = { dishName = it },
                        label = { Text("Grocery List / Dish Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val items = card.grocery?.items ?: listOf(
                                GroceryItem("g1", "Sample Grocery Item 1", "1 unit", false),
                                GroceryItem("g2", "Sample Grocery Item 2", "2 units", false)
                            )
                            val updatedG = GroceryDetails(dishName = dishName, items = items)
                            onSave(card.copy(category = IntentCategory.GROCERY, grocery = updatedG))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Cart")
                    }
                }

                IntentCategory.EXPENSE -> {
                    var vendor by remember { mutableStateOf(card.expense?.vendor ?: "Merchant / Store") }
                    var amountStr by remember { mutableStateOf((card.expense?.totalAmount ?: 0.0).toString()) }
                    var category by remember { mutableStateOf(card.expense?.category ?: "UPI Payment") }
                    var isPaid by remember { mutableStateOf(card.expense?.isPaid ?: true) }
                    var dueDate by remember { mutableStateOf(card.expense?.dueDate ?: "") }

                    OutlinedTextField(
                        value = vendor,
                        onValueChange = { vendor = it },
                        label = { Text("Merchant / Paid To Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Total Amount (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. UPI Payment, Cash, Utilities)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date (Optional, e.g. 2026-08-30)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isPaid,
                            onCheckedChange = { isPaid = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPaid) "Marked as Paid" else "Mark as Pending",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: 0.0
                            val updatedExp = ExpenseDetails(
                                vendor = vendor,
                                totalAmount = amt,
                                currency = "INR",
                                dueDate = dueDate.ifBlank { null },
                                category = category.ifBlank { "Expense" },
                                isPaid = isPaid,
                                isTransactionSms = card.expense?.isTransactionSms ?: false,
                                rawSmsText = card.expense?.rawSmsText
                            )
                            onSave(card.copy(category = IntentCategory.EXPENSE, expense = updatedExp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Expenses")
                    }
                }

                IntentCategory.BOOKMARK -> {
                    var headline by remember { mutableStateOf(card.bookmark?.headline ?: "Saved Note") }
                    var summary by remember { mutableStateOf(card.bookmark?.summary ?: "Screenshot note summary") }

                    OutlinedTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = { Text("Headline / Note Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            val updatedB = BookmarkDetails(headline = headline, summary = summary, keyTakeaways = listOf("Saved takeaway note"), sourcePlatform = "Screenshot Note")
                            onSave(card.copy(category = IntentCategory.BOOKMARK, bookmark = updatedB))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save to Bookmarks")
                    }
                }
            }
        }
    }
}
