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

enum class FeedTab {
    ALL,
    GROCERIES,
    EXPENSES,
    EVENTS
}

data class UiState(
    val selectedTab: FeedTab = FeedTab.ALL,
    val searchQuery: String = "",
    val cards: List<SnapActionCard> = DemoData.initialCards,
    val processingState: ProcessingState? = null,
    val activeEditCard: SnapActionCard? = null,
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

    fun uploadScreenshot(imageUri: String) {
        viewModelScope.launch {
            visionRepository.processScreenshot(imageUri).collect { state ->
                if (state.step == ProcessingStep.COMPLETED && state.card != null) {
                    val updatedList = listOf(state.card) + _uiState.value.cards
                    _uiState.value = _uiState.value.copy(
                        processingState = null,
                        cards = updatedList
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
        _uiState.value = _uiState.value.copy(cards = updatedCards, activeEditCard = null)
    }

    fun deleteCard(cardId: String) {
        val updatedCards = _uiState.value.cards.filter { it.id != cardId }
        _uiState.value = _uiState.value.copy(cards = updatedCards)
    }
}
