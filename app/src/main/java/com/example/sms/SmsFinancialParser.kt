package com.example.sms

import android.content.Context
import android.net.Uri
import com.example.data.TransactionEntity
import com.example.ml.TransactionMlCategorizer
import com.example.data.MlRuleEntity
import java.util.regex.Pattern

data class ParsedSmsResult(
    val title: String,
    val amount: Double,
    val dateMillis: Long,
    val type: String, // "EXPENSE" or "INCOME"
    val merchant: String,
    val bankAccount: String,
    val smsBody: String,
    val sender: String
)

class SmsFinancialParser {

    private val mlCategorizer = TransactionMlCategorizer()

    // Regex patterns for amounts
    private val amountPattern = Pattern.compile(
        "(?:[S$]|USD|INR|Rs\\.?|EUR|GBP)?\\s?([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?|[0-9]+(?:\\.[0-9]{1,2})?)\\s?(?:USD|INR|Rs)?",
        Pattern.CASE_INSENSITIVE
    )

    // Regex patterns for merchants/vendors
    private val merchantPatternAt = Pattern.compile("(?:at|to|vpa|for|merchant)\\s+([A-Za-z0-9\\s\\.\\*\\&\\'-]{2,25})", Pattern.CASE_INSENSITIVE)
    private val merchantPatternFrom = Pattern.compile("(?:from|by)\\s+([A-Za-z0-9\\s\\.\\*\\&\\'-]{2,25})", Pattern.CASE_INSENSITIVE)

    /**
     * Reads financial SMS messages from Android ContentProvider (Google Messages / System SMS).
     */
    fun readFinancialSmsFromDevice(context: Context, rules: List<MlRuleEntity>): List<TransactionEntity> {
        val parsedTransactions = mutableListOf<TransactionEntity>()

        try {
            val uri = Uri.parse("content://sms/inbox")
            val projection = arrayOf("_id", "address", "body", "date")
            val selection = "body LIKE '%debited%' OR body LIKE '%spent%' OR body LIKE '%credited%' OR body LIKE '%charged%' OR body LIKE '%paid%' OR body LIKE '%card%' OR body LIKE '%bank%'"
            
            val cursor = context.contentResolver.query(
                uri,
                projection,
                selection,
                null,
                "date DESC LIMIT 50"
            )

            cursor?.use { c ->
                val bodyIdx = c.getColumnIndex("body")
                val dateIdx = c.getColumnIndex("date")
                val addressIdx = c.getColumnIndex("address")

                while (c.moveToNext()) {
                    val body = if (bodyIdx != -1) c.getString(bodyIdx) else ""
                    val date = if (dateIdx != -1) c.getLong(dateIdx) else System.currentTimeMillis()
                    val address = if (addressIdx != -1) c.getString(addressIdx) else "SMS"

                    val parsed = parseSingleSms(body, date, address)
                    if (parsed != null && parsed.amount > 0) {
                        val classification = mlCategorizer.classify(
                            merchant = parsed.merchant,
                            smsText = parsed.smsBody,
                            isIncome = parsed.type == "INCOME",
                            rules = rules
                        )

                        parsedTransactions.add(
                            TransactionEntity(
                                title = parsed.title,
                                amount = parsed.amount,
                                dateMillis = parsed.dateMillis,
                                type = parsed.type,
                                category = classification.category,
                                merchant = parsed.merchant,
                                bankAccount = parsed.bankAccount,
                                sourceType = "SMS",
                                originalSmsText = parsed.smsBody,
                                mlConfidence = classification.confidence
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return parsedTransactions
    }

    /**
     * Parses an individual SMS text body into financial fields.
     */
    fun parseSingleSms(body: String, dateMillis: Long, sender: String): ParsedSmsResult? {
        val lowerBody = body.lowercase()

        val isDebit = lowerBody.contains("debited") || lowerBody.contains("spent") ||
                lowerBody.contains("charged") || lowerBody.contains("paid") ||
                lowerBody.contains("purchase") || lowerBody.contains("sent")
        val isCredit = lowerBody.contains("credited") || lowerBody.contains("received") ||
                lowerBody.contains("salary") || lowerBody.contains("deposit") ||
                lowerBody.contains("added")

        if (!isDebit && !isCredit) return null

        val type = if (isCredit && !isDebit) "INCOME" else "EXPENSE"

        // Extract Amount
        var amount = 0.0
        val matcher = amountPattern.matcher(body)
        while (matcher.find()) {
            val numStr = matcher.group(1)?.replace(",", "") ?: ""
            val candidate = numStr.toDoubleOrNull() ?: 0.0
            if (candidate > 0.5 && candidate < 100000.0) {
                amount = candidate
                break
            }
        }

        if (amount <= 0) return null

        // Extract Merchant / Vendor
        var merchant = "Merchant"
        val atMatcher = merchantPatternAt.matcher(body)
        if (atMatcher.find()) {
            val candidate = atMatcher.group(1)?.trim() ?: ""
            if (candidate.length in 3..25 && !candidate.lowercase().contains("account") && !candidate.lowercase().contains("card")) {
                merchant = candidate
            }
        } else {
            val fromMatcher = merchantPatternFrom.matcher(body)
            if (fromMatcher.find()) {
                val candidate = fromMatcher.group(1)?.trim() ?: ""
                if (candidate.length in 3..25) {
                    merchant = candidate
                }
            }
        }

        // Refine merchant name if generic
        if (merchant == "Merchant") {
            when {
                lowerBody.contains("starbucks") -> merchant = "Starbucks"
                lowerBody.contains("amazon") -> merchant = "Amazon.com"
                lowerBody.contains("walmart") -> merchant = "Walmart"
                lowerBody.contains("target") -> merchant = "Target"
                lowerBody.contains("uber") -> merchant = "Uber"
                lowerBody.contains("netflix") -> merchant = "Netflix"
                lowerBody.contains("electric") -> merchant = "Electric Utility"
                lowerBody.contains("swiggy") -> merchant = "Swiggy Food"
                lowerBody.contains("zomato") -> merchant = "Zomato Food"
                lowerBody.contains("cvs") -> merchant = "CVS Pharmacy"
                lowerBody.contains("salary") -> merchant = "Payroll Employer"
                else -> merchant = sender.takeLast(10)
            }
        }

        // Account identifier
        val bankAccount = when {
            lowerBody.contains("card") -> "Card ending " + (Regex("\\d{4}").find(body)?.value ?: "x")
            lowerBody.contains("a/c") || lowerBody.contains("acct") -> "Bank A/C " + (Regex("\\d{4}").find(body)?.value ?: "x")
            lowerBody.contains("apple pay") -> "Apple Pay"
            lowerBody.contains("google pay") || lowerBody.contains("gpay") -> "Google Pay"
            lowerBody.contains("upi") -> "UPI Transfer"
            else -> "Bank Account"
        }

        val title = if (type == "INCOME") "Deposit from $merchant" else "$merchant Purchase"

        return ParsedSmsResult(
            title = title,
            amount = amount,
            dateMillis = dateMillis,
            type = type,
            merchant = merchant,
            bankAccount = bankAccount,
            smsBody = body,
            sender = sender
        )
    }

    /**
     * Generates test sample Google Messages financial SMSs for simulator & quick demo testing.
     */
    fun generateSampleSmsList(): List<Pair<String, String>> {
        val now = System.currentTimeMillis()
        return listOf(
            Pair(
                "HDFC Bank",
                "Alert: INR 420.00 debited at STARBUCKS COFFEE on card ending 8021. TxnRef: HDF88301."
            ),
            Pair(
                "SBI Bank",
                "Your A/C *1102 was charged Rs.1,850.00 for AIRTEL BROADBAND bill payment. Bal: Rs.24,100.00."
            ),
            Pair(
                "ICICI Bank",
                "Alert: Paid Rs.350.00 at SWIGGY FOOD ORDER with debit card ending 3042."
            ),
            Pair(
                "Google Pay",
                "Paid Rs.240.00 to UBER TRIP for ride booking via Google Pay UPI."
            ),
            Pair(
                "Axis Bank",
                "Transaction alert: Rs.2,499.00 spent at AMAZON INDIA on credit card *9912."
            ),
            Pair(
                "PhonePe",
                "Paid Rs.649.00 to NETFLIX INDIA for monthly subscription."
            )
        )
    }
}
