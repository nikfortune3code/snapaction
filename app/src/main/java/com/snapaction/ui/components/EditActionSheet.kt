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
                    text = "Edit & Verify AI Action",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
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
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(20.dp))

            when (card.category) {
                IntentCategory.EVENT -> {
                    var eventTitle by remember { mutableStateOf(card.event?.title ?: "") }
                    var startDate by remember { mutableStateOf(card.event?.startDate ?: "") }
                    var startTime by remember { mutableStateOf(card.event?.startTime ?: "") }
                    var location by remember { mutableStateOf(card.event?.location ?: "") }

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

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val updatedEv = card.event?.copy(title = eventTitle, startDate = startDate, startTime = startTime, location = location)
                                ?: EventDetails(title = eventTitle, startDate = startDate, startTime = startTime, location = location)
                            onSave(card.copy(event = updatedEv))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Verified Event")
                    }
                }

                IntentCategory.GROCERY -> {
                    var dishName by remember { mutableStateOf(card.grocery?.dishName ?: "") }

                    OutlinedTextField(
                        value = dishName,
                        onValueChange = { dishName = it },
                        label = { Text("Grocery List / Dish Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val updatedG = card.grocery?.copy(dishName = dishName)
                                ?: GroceryDetails(dishName = dishName)
                            onSave(card.copy(grocery = updatedG))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Verified Groceries")
                    }
                }

                IntentCategory.EXPENSE -> {
                    var vendor by remember { mutableStateOf(card.expense?.vendor ?: "") }
                    var amountStr by remember { mutableStateOf((card.expense?.totalAmount ?: 0.0).toString()) }

                    OutlinedTextField(
                        value = vendor,
                        onValueChange = { vendor = it },
                        label = { Text("Merchant / Biller") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Total Amount ($)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull() ?: (card.expense?.totalAmount ?: 0.0)
                            val updatedExp = card.expense?.copy(vendor = vendor, totalAmount = amt)
                                ?: ExpenseDetails(vendor = vendor, totalAmount = amt)
                            onSave(card.copy(expense = updatedExp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Verified Expense")
                    }
                }

                IntentCategory.BOOKMARK -> {
                    var headline by remember { mutableStateOf(card.bookmark?.headline ?: "") }
                    var summary by remember { mutableStateOf(card.bookmark?.summary ?: "") }

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

                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            val updatedB = card.bookmark?.copy(headline = headline, summary = summary)
                                ?: BookmarkDetails(headline = headline, summary = summary)
                            onSave(card.copy(bookmark = updatedB))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Verified Bookmark")
                    }
                }
            }
        }
    }
}
