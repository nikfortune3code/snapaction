package com.snapaction.data.repository

import android.content.Context
import android.util.Log
import com.snapaction.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Persistent local storage for SnapActionCards using SharedPreferences + JSON serialization.
 *
 * Design principles:
 * - Transaction SMS cards are stored persistently and NEVER removed on re-scan.
 * - Only new unique cards (by rawSmsText fingerprint) are appended on each scan.
 * - Manual cards (photos, events, bookmarks) are also persisted across app restarts.
 * - Data survives app updates — SharedPreferences persists through APK updates.
 */
class CardStorageRepository(private val context: Context) {

    companion object {
        private const val TAG = "CardStorageRepository"
        private const val PREFS_NAME = "snapaction_cards_store"
        private const val KEY_SMS_CARDS = "sms_transaction_cards"
        private const val KEY_MANUAL_CARDS = "manual_cards"
    }

    private val prefs by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    // ---------------------------------------------------------------------------
    // SMS Transaction Cards (persistent, additive-only)
    // ---------------------------------------------------------------------------

    /**
     * Load all previously persisted SMS transaction cards from local storage.
     * Called on every app launch to restore the transaction history.
     */
    fun loadSmsCards(): List<SnapActionCard> {
        return try {
            val stored = prefs.getString(KEY_SMS_CARDS, "[]") ?: "[]"
            json.decodeFromString<List<SnapActionCard>>(stored)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading SMS cards from storage", e)
            emptyList()
        }
    }

    /**
     * Merges newly scanned SMS cards with existing stored cards.
     * ONLY adds cards whose rawSmsText fingerprint is not already stored.
     * Existing records are NEVER removed or overwritten.
     *
     * @param newCards Freshly parsed cards from the current SMS scan
     * @return Count of truly new cards that were added
     */
    fun mergeAndSaveSmsCards(newCards: List<SnapActionCard>): Int {
        val existing = loadSmsCards().toMutableList()

        // Build a fingerprint set from rawSmsText of already stored cards
        val existingFingerprints = existing
            .mapNotNull { it.expense?.rawSmsText?.trim() }
            .toHashSet()

        val toAdd = newCards.filter { card ->
            val fingerprint = card.expense?.rawSmsText?.trim()
            fingerprint != null && fingerprint !in existingFingerprints
        }

        if (toAdd.isNotEmpty()) {
            // Prepend new cards (newest first), keep existing intact
            val merged = toAdd + existing
            saveSmsCards(merged)
            Log.d(TAG, "Merged ${toAdd.size} new SMS cards. Total stored: ${merged.size}")
        } else {
            Log.d(TAG, "No new SMS cards to add. Already stored: ${existing.size}")
        }

        return toAdd.size
    }

    private fun saveSmsCards(cards: List<SnapActionCard>) {
        try {
            val encoded = json.encodeToString(cards)
            prefs.edit().putString(KEY_SMS_CARDS, encoded).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error saving SMS cards to storage", e)
        }
    }

    // ---------------------------------------------------------------------------
    // Manual Cards (photos, events, groceries, bookmarks)
    // ---------------------------------------------------------------------------

    /**
     * Load all manually created/uploaded cards (photos, events, groceries, bookmarks).
     */
    fun loadManualCards(): List<SnapActionCard> {
        return try {
            val stored = prefs.getString(KEY_MANUAL_CARDS, "[]") ?: "[]"
            json.decodeFromString<List<SnapActionCard>>(stored)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading manual cards from storage", e)
            emptyList()
        }
    }

    /**
     * Prepend a new manually created/uploaded card to storage.
     */
    fun addManualCard(card: SnapActionCard) {
        try {
            val existing = loadManualCards().toMutableList()
            // Avoid duplicates by ID
            if (existing.none { it.id == card.id }) {
                val updated = listOf(card) + existing
                val encoded = json.encodeToString(updated)
                prefs.edit().putString(KEY_MANUAL_CARDS, encoded).apply()
                Log.d(TAG, "Saved manual card: ${card.id} (${card.category})")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving manual card", e)
        }
    }

    /**
     * Update an existing manual card (e.g., after user edits it).
     */
    fun updateManualCard(updated: SnapActionCard) {
        try {
            val existing = loadManualCards().map { if (it.id == updated.id) updated else it }
            prefs.edit().putString(KEY_MANUAL_CARDS, json.encodeToString(existing)).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating manual card", e)
        }
    }

    /**
     * Delete a card by ID from both SMS and manual stores.
     */
    fun deleteCard(cardId: String) {
        try {
            val smsCards = loadSmsCards().filter { it.id != cardId }
            prefs.edit().putString(KEY_SMS_CARDS, json.encodeToString(smsCards)).apply()

            val manualCards = loadManualCards().filter { it.id != cardId }
            prefs.edit().putString(KEY_MANUAL_CARDS, json.encodeToString(manualCards)).apply()

            Log.d(TAG, "Deleted card: $cardId")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting card", e)
        }
    }

    /**
     * Returns all stored cards (SMS + manual), sorted by timestamp descending.
     */
    fun loadAllCards(): List<SnapActionCard> {
        return (loadSmsCards() + loadManualCards()).sortedByDescending { it.timestamp }
    }
}
