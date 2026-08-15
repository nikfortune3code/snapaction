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
import com.snapaction.data.model.ClassificationCategory
import com.snapaction.ui.FeedTab
import com.snapaction.ui.SnapViewModel
import com.snapaction.ui.components.ActionCardItem
import com.snapaction.ui.components.EditActionSheet
import com.snapaction.ui.components.UploadHub
import com.snapaction.ui.theme.ExpenseBadgeColor
import java.util.Locale

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
                    icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "All") },
                    label = { Text("All Feeds") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.EXPENSES,
                    onClick = { viewModel.selectTab(FeedTab.EXPENSES) },
                    icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Bills") },
                    label = { Text("Bills") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.GROCERIES,
                    onClick = { viewModel.selectTab(FeedTab.GROCERIES) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Groceries") },
                    label = { Text("Groceries") }
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == FeedTab.EVENTS,
                    onClick = { viewModel.selectTab(FeedTab.EVENTS) },
                    icon = { Icon(Icons.Default.Restaurant, contentDescription = "Dishes") },
                    label = { Text("Dishes") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Upload & Ingestion Hub
            UploadHub(
                processingState = uiState.processingState,
                onPickImage = { uri -> viewModel.uploadScreenshot(uri) }
            )

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Search extracted receipts, recipes, items...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filtered Cards Feed
            val filteredCards = remember(uiState.cards, uiState.selectedTab, uiState.searchQuery) {
                uiState.cards.filter { card ->
                    val matchesTab = when (uiState.selectedTab) {
                        FeedTab.ALL -> true
                        FeedTab.EXPENSES -> card.category == ClassificationCategory.BILL_RECEIPT
                        FeedTab.GROCERIES -> card.category == ClassificationCategory.GROCERY_LIST || card.category == ClassificationCategory.PACKAGED_ITEM
                        FeedTab.EVENTS -> card.category == ClassificationCategory.FOOD_DISH
                    }
                    val matchesSearch = if (uiState.searchQuery.isEmpty()) true else {
                        card.summaryTitle.lowercase().contains(uiState.searchQuery.lowercase())
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

    // Modal Edit Sheet
    uiState.activeEditCard?.let { activeCard ->
        EditActionSheet(
            card = activeCard,
            onDismiss = { viewModel.closeEditCard() },
            onSave = { updated -> viewModel.saveEditedCard(updated) }
        )
    }
}
