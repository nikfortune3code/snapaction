package com.snapaction.data.repository

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.snapaction.data.model.ExpenseDetails
import com.snapaction.data.model.IntentCategory
import com.snapaction.data.model.SnapActionCard
import java.util.UUID

class SmsTransactionRepository {

    companion object {
        private const val TAG = "SmsTransactionRepository"
        private val KEYWORDS = listOf("spent", "sent", "debited", "paid", "credited", "deducted")
        // Rolling 90-day window — always reads the most recent 3 months of messages
        private const val LOOKBACK_DAYS = 90L
        val LOOKBACK_MS get() = System.currentTimeMillis() - LOOKBACK_DAYS * 24 * 60 * 60 * 1000
    }

    /**
     * Reads transaction-related SMS messages directly from the Android native Messages app
     * using ContentResolver on the SMS inbox. Requires READ_SMS permission.
     *
     * Returns list of parsed SnapActionCards (EXPENSE type) for all transaction messages found.
     */
    fun readTransactionSmsFromDevice(context: Context): List<SnapActionCard> {
        val results = mutableListOf<SnapActionCard>()
        val seenBodies = mutableSetOf<String>() // Deduplicate same SMS text

        try {
            val smsUri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("_id", "address", "body", "date")

            // Query SMS inbox: last 90 days, sorted newest first
            val cursor: Cursor? = context.contentResolver.query(
                smsUri,
                projection,
                "date >= ?",
                arrayOf(LOOKBACK_MS.toString()),
                "date DESC"
            )

            cursor?.use { c ->
                Log.d(TAG, "Total SMS found in inbox: ${c.count}")
                val bodyIndex = c.getColumnIndex("body")
                val dateIndex = c.getColumnIndex("date")
                val addressIndex = c.getColumnIndex("address")

                while (c.moveToNext()) {
                    val body = if (bodyIndex >= 0) c.getString(bodyIndex) ?: "" else ""
                    val date = if (dateIndex >= 0) c.getLong(dateIndex) else System.currentTimeMillis()
                    val address = if (addressIndex >= 0) c.getString(addressIndex) ?: "" else ""

                    if (body.isBlank()) continue

                    // Only process transaction-related SMS
                    val lowerBody = body.lowercase()
                    if (KEYWORDS.none { lowerBody.contains(it) }) continue

                    // Deduplicate
                    val key = body.trim()
                    if (key in seenBodies) continue
                    seenBodies.add(key)

                    val card = parseTransactionSms(body, timestamp = date)
                    if (card != null) {
                        Log.d(TAG, "Parsed transaction from: $address → ${card.expense?.vendor} ₹${card.expense?.totalAmount}")
                        results.add(card)
                    }
                }
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "SMS permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Error reading device SMS", e)
        }

        Log.d(TAG, "Total transaction SMS parsed: ${results.size}")
        return results
    }

    /**
     * Parses a bank/UPI transaction SMS text into an Expense SnapActionCard.
     * Detects keywords: 'spent', 'sent', 'debited', 'paid'.
     * Heading format: "Paid to [Merchant]" / "Sent to [Beneficiary]"
     */
    fun parseTransactionSms(smsBody: String, timestamp: Long = System.currentTimeMillis()): SnapActionCard? {
        val lower = smsBody.lowercase()

        // Ignore non-transaction alerts like disbursement consent notices
        if (lower.contains("disbursement")) return null

        // Must contain at least one transaction keyword
        if (KEYWORDS.none { lower.contains(it) }) return null

        // Extract Amount: e.g. Rs 250.00, INR 500, $45.00, Rs.250, 250.00
        val amountRegex = Regex("""(?:rs\.?|inr|\$|₹)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(smsBody)
        val totalAmount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

        // Determine currency symbol/code
        val currency = when {
            smsBody.contains("Rs", ignoreCase = true) || smsBody.contains("INR", ignoreCase = true) || smsBody.contains("₹") -> "INR"
            smsBody.contains("$") -> "USD"
            else -> "INR"
        }

        // Extract Payee / Merchant: e.g. "to Lucky Traders", "to Swiggy", "at Starbucks"
        val payeeRegex = Regex("""(?:to|at|vpa|paid to|sent to)\s+([A-Za-z0-9\s&'.\-]+?)(?:\.|,|\s+on|\s+ref|\s+avail|\s+bal|\s+via|\s+upi|$)""", RegexOption.IGNORE_CASE)
        val payeeMatch = payeeRegex.find(smsBody)
        var payeeName = payeeMatch?.groupValues?.get(1)?.trim() ?: ""

        if (payeeName.isBlank() || payeeName.length < 2) {
            payeeName = "Merchant / Recipient"
        }

        // Clean heading: "Paid to [Recipient]"
        val heading = when {
            lower.contains("paid") -> if (payeeName.startsWith("to ", ignoreCase = true)) "Paid $payeeName" else "Paid to $payeeName"
            lower.contains("sent") -> if (payeeName.startsWith("to ", ignoreCase = true)) "Sent $payeeName" else "Sent to $payeeName"
            lower.contains("debited") -> "Debited - $payeeName"
            lower.contains("credited") -> "Credited - $payeeName"
            else -> "Spent at $payeeName"
        }

        // Determine Category (e.g. UPI Payment, Debit Card, Bank Transfer)
        val category = when {
            lower.contains("upi") || lower.contains("vpa") -> "UPI Payment"
            lower.contains("card") -> "Card Payment"
            lower.contains("a/c") || lower.contains("bank") || lower.contains("neft") || lower.contains("imps") -> "Bank Transfer"
            lower.contains("credited") -> "Credit / Refund"
            else -> "Merchant Payment"
        }

        return SnapActionCard(
            id = UUID.randomUUID().toString(),
            category = IntentCategory.EXPENSE,
            confidenceScore = 0.99,
            imageUri = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=60",
            timestamp = timestamp,
            expense = ExpenseDetails(
                vendor = heading,
                totalAmount = totalAmount,
                currency = currency,
                dueDate = null, // Transaction SMS is an instant paid transaction
                category = category,
                isPaid = true,
                isTransactionSms = true,
                rawSmsText = smsBody
            )
        )
    }

    /**
     * Startup transaction SMS scan. Returns empty list until user grants READ_SMS permission.
     */
    fun getInitialTransactionSms(): List<SnapActionCard> {
        return emptyList()
    }
}
