package com.snapaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapaction.data.mock.DemoData
import com.snapaction.data.model.*
import com.snapaction.data.repository.AiVisionRepository
import com.snapaction.data.repository.ProcessingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class FeedTab {
    REMINDERS,
    GROCERIES,
    EXPENSES,
    BOOKMARKS
}

data class UiState(
    val selectedTab: FeedTab = FeedTab.REMINDERS,
    val searchQuery: String = "",
    val cards: List<SnapActionCard> = DemoData.initialCards,
    val processingState: ProcessingState? = null,
    val activeEditCard: SnapActionCard? = null,
    val showAddEventModal: Boolean = false,
    val isDarkMode: Boolean = true
)

class SnapViewModel(
    private val visionRepository: AiVisionRepository = AiVisionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun selectTab(tab: FeedTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(isDarkMode = !_uiState.value.isDarkMode)
    }

    fun openAddEventModal() {
        _uiState.value = _uiState.value.copy(showAddEventModal = true)
    }

    fun closeAddEventModal() {
        _uiState.value = _uiState.value.copy(showAddEventModal = false)
    }

    fun addManualEvent(title: String, startDate: String, startTime: String, location: String, details: String) {
        val newCard = SnapActionCard(
            id = UUID.randomUUID().toString(),
            category = IntentCategory.EVENT,
            confidenceScore = 1.0,
            imageUri = "https://images.unsplash.com/photo-1506784983877-45594efa4cbe?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis(),
            event = EventDetails(
                title = title,
                startDate = startDate,
                startTime = startTime.ifBlank { "12:00" },
                location = location.ifBlank { "Custom Reminder" },
                details = details.ifBlank { "Manually created reminder" }
            )
        )
        _uiState.value = _uiState.value.copy(
            cards = listOf(newCard) + _uiState.value.cards,
            selectedTab = FeedTab.REMINDERS,
            showAddEventModal = false
        )
    }

    fun uploadScreenshot(imageUri: String) {
        val currentPreferred = when (_uiState.value.selectedTab) {
            FeedTab.REMINDERS -> IntentCategory.EVENT
            FeedTab.GROCERIES -> IntentCategory.GROCERY
            FeedTab.EXPENSES -> IntentCategory.EXPENSE
            FeedTab.BOOKMARKS -> IntentCategory.BOOKMARK
        }
        viewModelScope.launch {
            visionRepository.processScreenshot(imageUri, currentPreferred).collect { state ->
                if (state.step == ProcessingStep.COMPLETED && state.card != null) {
                    val updatedList = listOf(state.card) + _uiState.value.cards
                    val targetTab = when (state.card.category) {
                        IntentCategory.EVENT -> FeedTab.REMINDERS
                        IntentCategory.GROCERY -> FeedTab.GROCERIES
                        IntentCategory.EXPENSE -> FeedTab.EXPENSES
                        IntentCategory.BOOKMARK -> FeedTab.BOOKMARKS
                    }
                    _uiState.value = _uiState.value.copy(
                        processingState = null,
                        cards = updatedList,
                        selectedTab = targetTab
                    )
                } else {
                    _uiState.value = _uiState.value.copy(processingState = state)
                }
            }
        }
    }

    fun toggleGroceryItem(cardId: String, itemId: String) {
        val updatedCards = _uiState.value.cards.map { card ->
            if (card.id == cardId && card.grocery != null) {
                val newItems = card.grocery.items.map { item ->
                    if (item.id == itemId) item.copy(isChecked = !item.isChecked) else item
                }
                card.copy(grocery = card.grocery.copy(items = newItems))
            } else card
        }
        _uiState.value = _uiState.value.copy(cards = updatedCards)
    }

    fun toggleExpensePaid(cardId: String) {
        val updatedCards = _uiState.value.cards.map { card ->
            if (card.id == cardId && card.expense != null) {
                card.copy(expense = card.expense.copy(isPaid = !card.expense.isPaid))
            } else card
        }
        _uiState.value = _uiState.value.copy(cards = updatedCards)
    }

    fun openEditCard(card: SnapActionCard) {
        _uiState.value = _uiState.value.copy(activeEditCard = card)
    }

    fun closeEditCard() {
        _uiState.value = _uiState.value.copy(activeEditCard = null)
    }

    fun saveEditedCard(updatedCard: SnapActionCard) {
        val updatedCards = _uiState.value.cards.map {
            if (it.id == updatedCard.id) updatedCard else it
        }
        val targetTab = when (updatedCard.category) {
            IntentCategory.EVENT -> FeedTab.REMINDERS
            IntentCategory.GROCERY -> FeedTab.GROCERIES
            IntentCategory.EXPENSE -> FeedTab.EXPENSES
            IntentCategory.BOOKMARK -> FeedTab.BOOKMARKS
        }
        _uiState.value = _uiState.value.copy(
            cards = updatedCards, 
            activeEditCard = null,
            selectedTab = targetTab
        )
    }

    fun deleteCard(cardId: String) {
        val updatedCards = _uiState.value.cards.filter { it.id != cardId }
        _uiState.value = _uiState.value.copy(cards = updatedCards)
    }
}
