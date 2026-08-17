package com.snapaction.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.snapaction.data.model.*
import com.snapaction.data.network.CartAnalysisResult
import com.snapaction.data.network.CartApiService
import com.snapaction.data.repository.AiVisionRepository
import com.snapaction.data.repository.CardStorageRepository
import com.snapaction.data.repository.ProcessingState
import com.snapaction.data.repository.SmsTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

enum class FeedTab {
    GROCERIES,
    EXPENSES,
    REMINDERS,
    BOOKMARKS
}

data class UiState(
    val selectedTab: FeedTab = FeedTab.GROCERIES,
    val searchQuery: String = "",
    val cards: List<SnapActionCard> = emptyList(),
    val processingState: ProcessingState? = null,
    val activeEditCard: SnapActionCard? = null,
    val showAddEventModal: Boolean = false,
    val isDarkMode: Boolean = true,
    val isSmsScanning: Boolean = false,
    val smsScannedCount: Int = 0,
    val isLoadingStorage: Boolean = true,  // true while reading from SharedPreferences on launch

    // ── Cart AI feature ──────────────────────────────────────────────────────
    /** Persisted cart items saved by the user via "Add to Cart" */
    val cartItems: List<CartItem> = emptyList(),
    /** True while image is being uploaded and analysed by the backend */
    val isCartAnalyzing: Boolean = false,
    /** Holds the AI result awaiting user confirmation (shown in form dialog) */
    val pendingCartAnalysis: CartAnalysisResult? = null,
    /** URI of the image the user selected for cart analysis */
    val pendingCartImageUri: String? = null,
    /** Error message if cart analysis failed */
    val cartAnalysisError: String? = null
)

class SnapViewModel(
    private val storage: CardStorageRepository,
    private val visionRepository: AiVisionRepository = AiVisionRepository(),
    private val smsRepository: SmsTransactionRepository = SmsTransactionRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        // On every launch: restore all persisted cards from local storage
        loadPersistedCards()
    }

    /**
     * Reads all previously synced cards from SharedPreferences and populates the UI.
     * This ensures transaction history is never lost after restarts or app updates.
     */
    private fun loadPersistedCards() {
        viewModelScope.launch {
            val allCards = withContext(Dispatchers.IO) {
                storage.loadAllCards()
            }
            _uiState.value = _uiState.value.copy(
                cards = allCards,
                isLoadingStorage = false
            )
        }
    }

    /**
     * Scans the Android native Messages inbox (last 90 days) for bank/UPI transactions.
     * Only NEW transactions (not already stored) are appended — existing records are never removed.
     * Persists the merged result immediately so it survives the next app restart.
     */
    fun scanDeviceSmsMessages(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSmsScanning = true,
                selectedTab = FeedTab.EXPENSES
            )

            val addedCount = withContext(Dispatchers.IO) {
                // 1. Read SMS inbox (last 90 days)
                val freshCards = smsRepository.readTransactionSmsFromDevice(context)
                // 2. Merge into persistent store (additive only — returns count of new cards)
                storage.mergeAndSaveSmsCards(freshCards)
            }

            // 3. Reload full card list from storage (includes all historical + new)
            val allCards = withContext(Dispatchers.IO) {
                storage.loadAllCards()
            }

            _uiState.value = _uiState.value.copy(
                cards = allCards,
                isSmsScanning = false,
                smsScannedCount = addedCount
            )
        }
    }

    /**
     * Parse a single SMS text manually entered by the user and persist it.
     */
    fun processTransactionSmsText(smsText: String) {
        val parsedCard = smsRepository.parseTransactionSms(smsText)
        if (parsedCard != null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    storage.mergeAndSaveSmsCards(listOf(parsedCard))
                }
                val allCards = withContext(Dispatchers.IO) { storage.loadAllCards() }
                _uiState.value = _uiState.value.copy(
                    cards = allCards,
                    selectedTab = FeedTab.EXPENSES
                )
            }
        }
    }

    fun clearSmsScannedCount() {
        _uiState.value = _uiState.value.copy(smsScannedCount = 0)
    }

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
        viewModelScope.launch {
            withContext(Dispatchers.IO) { storage.addManualCard(newCard) }
            val allCards = withContext(Dispatchers.IO) { storage.loadAllCards() }
            _uiState.value = _uiState.value.copy(
                cards = allCards,
                selectedTab = FeedTab.REMINDERS,
                showAddEventModal = false
            )
        }
    }

    fun uploadScreenshot(imageUri: String, context: Context? = null) {
        val currentPreferred = when (_uiState.value.selectedTab) {
            FeedTab.GROCERIES -> IntentCategory.GROCERY
            FeedTab.EXPENSES -> IntentCategory.EXPENSE
            FeedTab.REMINDERS -> IntentCategory.EVENT
            FeedTab.BOOKMARKS -> IntentCategory.BOOKMARK
        }
        viewModelScope.launch {
            visionRepository.processScreenshot(context, imageUri, currentPreferred).collect { state ->
                if (state.step == ProcessingStep.COMPLETED && state.card != null) {
                    val targetTab = when (state.card.category) {
                        IntentCategory.GROCERY -> FeedTab.GROCERIES
                        IntentCategory.EXPENSE -> FeedTab.EXPENSES
                        IntentCategory.EVENT -> FeedTab.REMINDERS
                        IntentCategory.BOOKMARK -> FeedTab.BOOKMARKS
                    }
                    // Persist the card from the photo/screenshot
                    withContext(Dispatchers.IO) { storage.addManualCard(state.card) }
                    val allCards = withContext(Dispatchers.IO) { storage.loadAllCards() }
                    _uiState.value = _uiState.value.copy(
                        processingState = null,
                        cards = allCards,
                        selectedTab = targetTab
                    )
                } else {
                    _uiState.value = _uiState.value.copy(processingState = state)
                }
            }
        }
    }

    /**
     * Add a manually entered offline expense (e.g. Cash or manual transaction).
     */
    fun addOfflineExpense(vendor: String, amount: Double, category: String, isPaid: Boolean) {
        val cleanVendor = when {
            vendor.startsWith("Paid to", ignoreCase = true) ||
            vendor.startsWith("Sent to", ignoreCase = true) ||
            vendor.startsWith("Spent at", ignoreCase = true) -> vendor
            else -> "Paid to $vendor"
        }

        val newCard = SnapActionCard(
            id = UUID.randomUUID().toString(),
            category = IntentCategory.EXPENSE,
            confidenceScore = 1.0,
            imageUri = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis(),
            expense = ExpenseDetails(
                vendor = cleanVendor,
                totalAmount = amount,
                currency = "INR",
                dueDate = null,
                category = category.ifBlank { "Cash / Offline" },
                isPaid = isPaid,
                isTransactionSms = false,
                rawSmsText = null
            )
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) { storage.addManualCard(newCard) }
            val allCards = withContext(Dispatchers.IO) { storage.loadAllCards() }
            _uiState.value = _uiState.value.copy(
                cards = allCards,
                selectedTab = FeedTab.EXPENSES
            )
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
        // Persist the toggled state
        val updatedCard = updatedCards.find { it.id == cardId }
        if (updatedCard != null) {
            viewModelScope.launch { withContext(Dispatchers.IO) { storage.updateCard(updatedCard) } }
        }
    }

    fun toggleExpensePaid(cardId: String) {
        val updatedCards = _uiState.value.cards.map { card ->
            if (card.id == cardId && card.expense != null) {
                card.copy(expense = card.expense.copy(isPaid = !card.expense.isPaid))
            } else card
        }
        _uiState.value = _uiState.value.copy(cards = updatedCards)
        val updatedCard = updatedCards.find { it.id == cardId }
        if (updatedCard != null) {
            viewModelScope.launch { withContext(Dispatchers.IO) { storage.updateCard(updatedCard) } }
        }
    }

    fun openEditCard(card: SnapActionCard) {
        _uiState.value = _uiState.value.copy(activeEditCard = card)
    }

    fun closeEditCard() {
        _uiState.value = _uiState.value.copy(activeEditCard = null)
    }

    fun saveEditedCard(updatedCard: SnapActionCard) {
        val targetTab = when (updatedCard.category) {
            IntentCategory.GROCERY -> FeedTab.GROCERIES
            IntentCategory.EXPENSE -> FeedTab.EXPENSES
            IntentCategory.EVENT -> FeedTab.REMINDERS
            IntentCategory.BOOKMARK -> FeedTab.BOOKMARKS
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { storage.updateCard(updatedCard) }
            val allCards = withContext(Dispatchers.IO) { storage.loadAllCards() }
            _uiState.value = _uiState.value.copy(
                cards = allCards,
                activeEditCard = null,
                selectedTab = targetTab
            )
        }
    }

    fun deleteCard(cardId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { storage.deleteCard(cardId) }
            val allCards = withContext(Dispatchers.IO) { storage.loadAllCards() }
            _uiState.value = _uiState.value.copy(cards = allCards)
        }
    }

    // ── Cart AI Functions ────────────────────────────────────────────────────

    /**
     * Uploads [imageFile] to the Node.js backend for Gemini Vision analysis.
     * On success, stores the result in [UiState.pendingCartAnalysis] so the
     * form dialog can pre-fill productName and brandName.
     * On failure, sets [UiState.cartAnalysisError] with a user-facing message.
     */
    fun analyzeCartImage(imageFile: File, imageUri: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCartAnalyzing = true,
                cartAnalysisError = null,
                pendingCartAnalysis = null,
                pendingCartImageUri = imageUri
            )
            try {
                val result = withContext(Dispatchers.IO) {
                    CartApiService.analyzeCartImage(imageFile)
                }
                _uiState.value = _uiState.value.copy(
                    isCartAnalyzing = false,
                    pendingCartAnalysis = result
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCartAnalyzing = false,
                    cartAnalysisError = "Could not analyse image: ${e.message}"
                )
            }
        }
    }

    /** Called when the user taps Save in the Cart form dialog. */
    fun saveCartItem(productName: String, brandName: String?, quantity: Int) {
        val imageUri = _uiState.value.pendingCartImageUri
            ?: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=400"
        val newItem = CartItem(
            id = UUID.randomUUID().toString(),
            imageUri = imageUri,
            productName = productName.ifBlank { "Unknown Product" },
            brandName = brandName?.ifBlank { null },
            quantity = quantity.coerceAtLeast(1)
        )
        _uiState.value = _uiState.value.copy(
            cartItems = listOf(newItem) + _uiState.value.cartItems,
            pendingCartAnalysis = null,
            pendingCartImageUri = null
        )
    }

    /** Dismisses the pending cart analysis dialog without saving. */
    fun dismissCartAnalysis() {
        _uiState.value = _uiState.value.copy(
            pendingCartAnalysis = null,
            pendingCartImageUri = null,
            cartAnalysisError = null
        )
    }

    /** Clears a transient cart analysis error toast. */
    fun clearCartAnalysisError() {
        _uiState.value = _uiState.value.copy(cartAnalysisError = null)
    }
}

/**
 * Factory to inject CardStorageRepository (which needs Context) into SnapViewModel.
 */
class SnapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SnapViewModel(
            storage = CardStorageRepository(context.applicationContext)
        ) as T
    }
}
