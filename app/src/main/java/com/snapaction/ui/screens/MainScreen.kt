package com.snapaction.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.snapaction.data.model.IntentCategory
import com.snapaction.data.model.SnapActionCard
import com.snapaction.ui.FeedTab
import com.snapaction.ui.SnapViewModel
import com.snapaction.ui.components.ActionCardItem
import com.snapaction.ui.components.EditActionSheet
import com.snapaction.ui.components.UploadHub
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: SnapViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchExpanded by remember { mutableStateOf(false) }
    var showSmsInputDialog by remember { mutableStateOf(false) }
    var selectedMonthFilter by remember { mutableStateOf("All Months") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search all tabs...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
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
                    }
                },
                actions = {
                    // Global Search Button
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.updateSearchQuery("")
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search All Tabs",
                            tint = if (isSearchExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
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

            // Centered "Add Event" Button in Reminders Tab
            if (uiState.selectedTab == FeedTab.REMINDERS) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.openAddEventModal() },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Event", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            } else if (uiState.selectedTab == FeedTab.EXPENSES) {
                // SMS Transaction Parsing Shortcut & Monthly Analysis Header in Expenses Tab
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    OutlinedButton(
                        onClick = { showSmsInputDialog = true },
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(42.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Process SMS Transaction", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Filtering Logic per Tab (Global Search searches all fields when active)
            val filteredCards = remember(uiState.cards, uiState.selectedTab, uiState.searchQuery) {
                uiState.cards.filter { card ->
                    val matchesTab = when (uiState.selectedTab) {
                        FeedTab.REMINDERS -> card.category == IntentCategory.EVENT
                        FeedTab.GROCERIES -> card.category == IntentCategory.GROCERY
                        FeedTab.EXPENSES -> card.category == IntentCategory.EXPENSE
                        FeedTab.BOOKMARKS -> card.category == IntentCategory.BOOKMARK
                    }
                    val matchesSearch = if (uiState.searchQuery.isBlank()) true else {
                        val title = card.event?.title ?: card.grocery?.dishName ?: card.expense?.vendor ?: card.bookmark?.headline ?: ""
                        val details = card.event?.details ?: card.expense?.category ?: card.bookmark?.summary ?: ""
                        title.contains(uiState.searchQuery, ignoreCase = true) || details.contains(uiState.searchQuery, ignoreCase = true)
                    }
                    matchesTab && matchesSearch
                }
            }

            // EXPENSES TAB: Monthly Categorization & Spend Analysis
            if (uiState.selectedTab == FeedTab.EXPENSES && filteredCards.isNotEmpty()) {
                val expenseCards = filteredCards.filter { it.expense != null }
                val availableMonths = remember(expenseCards) {
                    listOf("All Months") + expenseCards.map { it.getMonthYearString() }.distinct()
                }

                // Selected Month Filtered List
                val monthFilteredExpenses = remember(expenseCards, selectedMonthFilter) {
                    if (selectedMonthFilter == "All Months") expenseCards else expenseCards.filter { it.getMonthYearString() == selectedMonthFilter }
                }

                val totalSpend = monthFilteredExpenses.sumOf { it.expense?.totalAmount ?: 0.0 }
                val paidSpend = monthFilteredExpenses.filter { it.expense?.isPaid == true }.sumOf { it.expense?.totalAmount ?: 0.0 }
                val pendingSpend = monthFilteredExpenses.filter { it.expense?.isPaid == false }.sumOf { it.expense?.totalAmount ?: 0.0 }

                // Monthly Summary Banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📊 Monthly Spend Analysis",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${monthFilteredExpenses.size} Expenses",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Spend", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "₹%.2f", totalSpend),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column {
                                Text("Paid Amount", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "₹%.2f", paidSpend),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Column {
                                Text("Pending Bills", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    text = String.format(Locale.US, "₹%.2f", pendingSpend),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // Month Selector Filter Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableMonths) { month ->
                        FilterChip(
                            selected = selectedMonthFilter == month,
                            onClick = { selectedMonthFilter = month },
                            label = { Text(month, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = if (selectedMonthFilter == month) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Group Expenses by Month
                val groupedByMonth = monthFilteredExpenses.groupBy { it.getMonthYearString() }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    groupedByMonth.forEach { (monthName, cardsInMonth) ->
                        val monthTotal = cardsInMonth.sumOf { it.expense?.totalAmount ?: 0.0 }
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📅 $monthName",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = String.format(Locale.US, "Total: ₹%.2f", monthTotal),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        items(cardsInMonth, key = { it.id }) { card ->
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
            } else if (filteredCards.isEmpty()) {
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

    // SMS Transaction Dialog
    if (showSmsInputDialog) {
        SmsTransactionDialog(
            onDismiss = { showSmsInputDialog = false },
            onParseSms = { smsText ->
                viewModel.processTransactionSmsText(smsText)
                showSmsInputDialog = false
            }
        )
    }
}

@Composable
fun SmsTransactionDialog(
    onDismiss: () -> Unit,
    onParseSms: (String) -> Unit
) {
    var smsText by remember { mutableStateOf("Sent Rs 250.00 to Lucky Traders via UPI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Parse Bank SMS Transaction", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Detects keywords: 'spent', 'sent', 'debited', 'paid'",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    label = { Text("SMS Message Body") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (smsText.isNotBlank()) onParseSms(smsText)
                },
                enabled = smsText.isNotBlank()
            ) {
                Text("Process SMS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
