package com.snapaction.data.repository

import com.snapaction.data.model.ExpenseDetails
import com.snapaction.data.model.IntentCategory
import com.snapaction.data.model.SnapActionCard
import java.util.UUID

class SmsTransactionRepository {

    private val keywords = listOf("spent", "sent", "debited", "paid")

    /**
     * Parses a bank/UPI transaction SMS text into an Expense SnapActionCard.
     * Detects keywords: 'spent', 'sent', 'debited', 'paid'.
     * Heading format: "Paid to [Merchant]" / "Sent to [Beneficiary]"
     */
    fun parseTransactionSms(smsBody: String): SnapActionCard? {
        val lower = smsBody.lowercase()
        
        // Must contain at least one transaction keyword
        if (keywords.none { lower.contains(it) }) return null

        // Extract Amount: e.g. Rs 250.00, INR 500, $45.00, Rs.250, 250.00
        val amountRegex = Regex("""(?:rs\.?|inr|\$)\s*([\d,]+\.?\d*)""", RegexOption.IGNORE_CASE)
        val amountMatch = amountRegex.find(smsBody)
        val totalAmount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

        // Determine currency symbol/code
        val currency = when {
            smsBody.contains("Rs", ignoreCase = true) || smsBody.contains("INR", ignoreCase = true) -> "INR"
            smsBody.contains("$") -> "USD"
            else -> "INR"
        }

        // Extract Payee / Merchant: e.g. "to Lucky Traders", "to Swiggy", "at Starbucks"
        val payeeRegex = Regex("""(?:to|at|vpa|paid to|sent to)\s+([A-Za-z0-9\s&'-]+?)(?:\.|\s+on|\s+ref|\s+avail|\s+bal|\s+via|$)""", RegexOption.IGNORE_CASE)
        val payeeMatch = payeeRegex.find(smsBody)
        var payeeName = payeeMatch?.groupValues?.get(1)?.trim() ?: ""

        if (payeeName.isBlank() || payeeName.length < 2) {
            payeeName = "Merchant / Recipient"
        }

        // Clean heading: "Paid to [Recipient]"
        val heading = when {
            lower.contains("paid") -> if (payeeName.startsWith("to ", ignoreCase = true)) "Paid $payeeName" else "Paid to $payeeName"
            lower.contains("sent") -> if (payeeName.startsWith("to ", ignoreCase = true)) "Sent $payeeName" else "Sent to $payeeName"
            lower.contains("debited") -> "Paid to $payeeName"
            else -> "Spent at $payeeName"
        }

        // Determine Category (e.g. UPI Payment, Debit Card, Bank Transfer)
        val category = when {
            lower.contains("upi") || lower.contains("vpa") -> "UPI Payment"
            lower.contains("card") -> "Card Payment"
            lower.contains("a/c") || lower.contains("bank") -> "Bank Debit"
            else -> "Merchant Payment"
        }

        return SnapActionCard(
            id = UUID.randomUUID().toString(),
            category = IntentCategory.EXPENSE,
            confidenceScore = 0.99,
            imageUri = "https://images.unsplash.com/photo-1559526324-4b87b5e36e44?w=800&auto=format&fit=crop&q=60",
            timestamp = System.currentTimeMillis(),
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
     * Startup transaction SMS scan. Returns empty list so no dummy data is pre-populated.
     */
    fun getInitialTransactionSms(): List<SnapActionCard> {
        return emptyList()
    }
}
