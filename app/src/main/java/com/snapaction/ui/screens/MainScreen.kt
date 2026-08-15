package com.snapaction.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
                    selected = uiState.selectedTab == FeedTab.ALL,
                    onClick = { viewModel.selectTab(FeedTab.ALL) },
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
                    selected = uiState.selectedTab == FeedTab.EVENTS,
                    onClick = { viewModel.selectTab(FeedTab.EVENTS) },
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

            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search extracted reminders, groceries, expenses, bookmarks...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            val filteredCards = remember(uiState.cards, uiState.selectedTab, uiState.searchQuery) {
                uiState.cards.filter { card ->
                    val matchesTab = when (uiState.selectedTab) {
                        FeedTab.ALL -> true
                        FeedTab.GROCERIES -> card.category == IntentCategory.GROCERY
                        FeedTab.EXPENSES -> card.category == IntentCategory.EXPENSE
                        FeedTab.EVENTS -> card.category == IntentCategory.BOOKMARK
                    }
                    val matchesSearch = if (uiState.searchQuery.isEmpty()) true else {
                        val title = card.event?.title ?: card.grocery?.dishName ?: card.expense?.vendor ?: card.bookmark?.headline ?: ""
                        title.lowercase().contains(uiState.searchQuery.lowercase())
                    }
                    matchesTab && matchesSearch
                }
            }

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

    uiState.activeEditCard?.let { activeCard ->
        EditActionSheet(
            card = activeCard,
            onDismiss = { viewModel.closeEditCard() },
            onSave = { updated -> viewModel.saveEditedCard(updated) }
        )
    }
}
