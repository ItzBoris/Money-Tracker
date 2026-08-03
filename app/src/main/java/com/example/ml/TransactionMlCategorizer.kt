package com.example.ml

import com.example.data.MlRuleEntity
import java.util.Locale

data class MlClassificationResult(
    val category: String,
    val confidence: Float,
    val matchedKeywords: List<String>,
    val explanation: String
)

class TransactionMlCategorizer {

    private val categories = listOf(
        "Food & Dining",
        "Shopping",
        "Bills & Utilities",
        "Transport",
        "Entertainment",
        "Health",
        "Income",
        "Transfer",
        "General"
    )

    /**
     * Categorizes a transaction using on-device token probability scoring and keyword ML rules.
     */
    fun classify(
        merchant: String,
        smsText: String?,
        isIncome: Boolean,
        rules: List<MlRuleEntity>
    ): MlClassificationResult {

        if (isIncome) {
            return MlClassificationResult(
                category = "Income",
                confidence = 0.98f,
                matchedKeywords = listOf("income", "credit", "deposit"),
                explanation = "Classified as Income based on transaction type credit pattern."
            )
        }

        val rawText = "${merchant.lowercase(Locale.ROOT)} ${smsText?.lowercase(Locale.ROOT) ?: ""}"
        val tokens = tokenize(rawText)

        // Map of category -> score
        val categoryScores = mutableMapOf<String, Float>()
        categories.forEach { categoryScores[it] = 0.1f } // Base prior probability

        val matchedKeywords = mutableListOf<String>()

        // 1. Evaluate against loaded ML rules
        for (rule in rules) {
            val ruleToken = rule.keyword.lowercase(Locale.ROOT)
            if (rawText.contains(ruleToken)) {
                val current = categoryScores[rule.categoryName] ?: 0.1f
                categoryScores[rule.categoryName] = current + (2.5f * rule.weight)
                matchedKeywords.add(ruleToken)
            }
        }

        // 2. Built-in Naive Bayes Token Heuristics
        for (token in tokens) {
            when {
                token in listOf("starbucks", "mcdonalds", "swiggy", "zomato", "cafe", "coffee", "restaurant", "dining", "pizza", "subway", "bakery", "food", "burger", "chipotle", "dunkin", "dominos", "foodpanda", "eats") -> {
                    categoryScores["Food & Dining"] = (categoryScores["Food & Dining"] ?: 0f) + 1.8f
                }
                token in listOf("amazon", "walmart", "target", "zara", "nike", "clothing", "fashion", "mall", "store", "ebay", "apple", "bestbuy", "ikea", "hm", "adidas") -> {
                    categoryScores["Shopping"] = (categoryScores["Shopping"] ?: 0f) + 1.8f
                }
                token in listOf("electric", "verizon", "wifi", "broadband", "utility", "water", "gas", "power", "rent", "recharge", "at&t", "tmobile", "bill", "insurance", "telecom") -> {
                    categoryScores["Bills & Utilities"] = (categoryScores["Bills & Utilities"] ?: 0f) + 1.8f
                }
                token in listOf("uber", "lyft", "taxi", "cab", "shell", "fuel", "petrol", "gasoline", "transit", "metro", "flight", "airline", "parking", "toll", "train") -> {
                    categoryScores["Transport"] = (categoryScores["Transport"] ?: 0f) + 1.8f
                }
                token in listOf("netflix", "spotify", "cinema", "movie", "steam", "playstation", "xbox", "nintendo", "ticket", "disney", "hbo", "concert", "theatre") -> {
                    categoryScores["Entertainment"] = (categoryScores["Entertainment"] ?: 0f) + 1.8f
                }
                token in listOf("cvs", "pharmacy", "doctor", "hospital", "clinic", "health", "medicine", "dental", "gym", "fitness", "walgreens", "lab") -> {
                    categoryScores["Health"] = (categoryScores["Health"] ?: 0f) + 1.8f
                }
                token in listOf("venmo", "zelle", "paypal", "wire", "upi", "transfer", "vpa", "send", "p2p") -> {
                    categoryScores["Transfer"] = (categoryScores["Transfer"] ?: 0f) + 1.8f
                }
            }
        }

        // Find highest scoring category
        var bestCategory = "General"
        var maxScore = 0.0f
        var totalScore = 0.0f

        categoryScores.forEach { (cat, score) ->
            totalScore += score
            if (score > maxScore) {
                maxScore = score
                bestCategory = cat
            }
        }

        // Compute Softmax-like confidence probability
        val confidence = if (maxScore > 0.5f) {
            (0.70f + (maxScore / (totalScore + 0.1f)) * 0.28f).coerceAtMost(0.99f)
        } else {
            0.65f
        }

        val explanation = if (matchedKeywords.isNotEmpty()) {
            "Matched keywords: ${matchedKeywords.distinct().take(3).joinToString(", ")} with ${(confidence * 100).toInt()}% ML probability."
        } else {
            "Categorized using on-device token inference with ${(confidence * 100).toInt()}% confidence."
        }

        return MlClassificationResult(
            category = bestCategory,
            confidence = confidence,
            matchedKeywords = matchedKeywords.distinct(),
            explanation = explanation
        )
    }

    private fun tokenize(text: String): List<String> {
        return text.replace(Regex("[^a-zA-Z0-9 ]"), " ")
            .split("\\s+".toRegex())
            .filter { it.length > 2 }
    }
}
