package com.snapaction.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapaction.data.model.IntentCategory
import com.snapaction.ui.FeedTab
import com.snapaction.ui.SnapViewModel
import com.snapaction.ui.components.ActionCardItem
import com.snapaction.ui.components.EditActionSheet
import com.snapaction.ui.components.UploadHub

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SnapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "SnapAction Logo",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SnapAction",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.REMINDERS,
                    onClick = { viewModel.selectTab(FeedTab.REMINDERS) },
                    icon = { Icon(Icons.Default.CalendarToday, contentDescription = "Reminders") },
                    label = { Text("Reminders") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.GROCERIES,
                    onClick = { viewModel.selectTab(FeedTab.GROCERIES) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Groceries") },
                    label = { Text("Groceries") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.EXPENSES,
                    onClick = { viewModel.selectTab(FeedTab.EXPENSES) },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Expenses") },
                    label = { Text("Expenses") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.BOOKMARKS,
                    onClick = { viewModel.selectTab(FeedTab.BOOKMARKS) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Bookmarks") },
                    label = { Text("Bookmarks") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            UploadHub(
                processingState = uiState.processingState,
                onPickImage = { uri -> viewModel.uploadScreenshot(uri) }
            )

            // Search & Action Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Search extracted cards...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                if (uiState.selectedTab == FeedTab.REMINDERS) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { viewModel.openAddEventModal() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Event", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // STRICT Filtering Logic per Tab
            val filteredCards = remember(uiState.cards, uiState.selectedTab, uiState.searchQuery) {
                uiState.cards.filter { card ->
                    val matchesTab = when (uiState.selectedTab) {
                        FeedTab.REMINDERS -> card.category == IntentCategory.EVENT // STRICT: ONLY EVENTS
                        FeedTab.GROCERIES -> card.category == IntentCategory.GROCERY
                        FeedTab.EXPENSES -> card.category == IntentCategory.EXPENSE
                        FeedTab.BOOKMARKS -> card.category == IntentCategory.BOOKMARK
                    }
                    val matchesSearch = if (uiState.searchQuery.isEmpty()) true else {
                        val title = card.event?.title ?: card.grocery?.dishName ?: card.expense?.vendor ?: card.bookmark?.headline ?: ""
                        title.lowercase().contains(uiState.searchQuery.lowercase())
                    }
                    matchesTab && matchesSearch
                }
            }

            if (filteredCards.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (uiState.selectedTab == FeedTab.REMINDERS) "No Reminders or Events yet" else "No items in this tab",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.selectedTab == FeedTab.REMINDERS) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.openAddEventModal() }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Event Manually")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredCards, key = { it.id }) { card ->
                        ActionCardItem(
                            card = card,
                            onToggleGrocery = { cardId, itemId -> viewModel.toggleGroceryItem(cardId, itemId) },
                            onTogglePaid = { cardId -> viewModel.toggleExpensePaid(cardId) },
                            onEditCard = { editCard -> viewModel.openEditCard(editCard) },
                            onDeleteCard = { delId -> viewModel.deleteCard(delId) }
                        )
                    }
                }
            }
        }
    }

    // Modal Edit Sheet
    uiState.activeEditCard?.let { activeCard ->
        EditActionSheet(
            card = activeCard,
            onDismiss = { viewModel.closeEditCard() },
            onSave = { updated -> viewModel.saveEditedCard(updated) }
        )
    }

    // Manual Event Creation Dialog
    if (uiState.showAddEventModal) {
        AddEventDialog(
            onDismiss = { viewModel.closeAddEventModal() },
            onAddEvent = { title, startDate, startTime, location, details ->
                viewModel.addManualEvent(title, startDate, startTime, location, details)
            }
        )
    }
}

@Composable
fun AddEventDialog(
    onDismiss: () -> Unit,
    onAddEvent: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("12:00") }
    var location by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Event / Reminder", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Date (YYYY-MM-DD) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Time (HH:MM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Details & Description") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && startDate.isNotBlank()) {
                        onAddEvent(title, startDate, startTime, location, details)
                    }
                },
                enabled = title.isNotBlank() && startDate.isNotBlank()
            ) {
                Text("Add Event")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
