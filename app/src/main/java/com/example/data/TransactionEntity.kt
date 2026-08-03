package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val dateMillis: Long,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String, // "Food & Dining", "Shopping", "Bills & Utilities", "Transport", "Entertainment", "Health", "Income", "Transfer", "General"
    val merchant: String,
    val bankAccount: String, // e.g. "Chase *4092", "Google Pay", "HDFC Bank"
    val sourceType: String, // "SMS", "MANUAL", "SIMULATED_SMS"
    val originalSmsText: String? = null,
    val mlConfidence: Float = 0.90f,
    val isUserModified: Boolean = false
)
