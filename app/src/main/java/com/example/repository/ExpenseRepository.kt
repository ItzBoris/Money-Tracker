package com.example.repository

import android.content.Context
import com.example.data.CategoryBudgetEntity
import com.example.data.MlRuleEntity
import com.example.data.TransactionDao
import com.example.data.TransactionEntity
import com.example.ml.TransactionMlCategorizer
import com.example.sms.SmsFinancialParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExpenseRepository(
    private val transactionDao: TransactionDao,
    private val smsParser: SmsFinancialParser = SmsFinancialParser(),
    private val mlCategorizer: TransactionMlCategorizer = TransactionMlCategorizer()
) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val allBudgets: Flow<List<CategoryBudgetEntity>> = transactionDao.getAllCategoryBudgets()
    val allMlRules: Flow<List<MlRuleEntity>> = transactionDao.getAllMlRules()

    suspend fun insertTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(id)
    }

    suspend fun updateBudget(budget: CategoryBudgetEntity) = withContext(Dispatchers.IO) {
        transactionDao.insertCategoryBudget(budget)
    }

    /**
     * Scans real SMS from device (Google Messages inbox) and inserts new non-duplicate financial transactions.
     */
    suspend fun scanAndSyncSms(context: Context): Int = withContext(Dispatchers.IO) {
        val rules = transactionDao.getMlRulesList()
        val parsedTransactions = smsParser.readFinancialSmsFromDevice(context, rules)

        if (parsedTransactions.isEmpty()) return@withContext 0

        transactionDao.insertTransactions(parsedTransactions)
        return@withContext parsedTransactions.size
    }

    /**
     * Simulates receiving a Google Messages financial SMS and categorizes it instantly using ML.
     */
    suspend fun injectSampleSms(sender: String, body: String): Boolean = withContext(Dispatchers.IO) {
        val rules = transactionDao.getMlRulesList()
        val parsed = smsParser.parseSingleSms(body, System.currentTimeMillis(), sender) ?: return@withContext false

        val classification = mlCategorizer.classify(
            merchant = parsed.merchant,
            smsText = parsed.smsBody,
            isIncome = parsed.type == "INCOME",
            rules = rules
        )

        val transaction = TransactionEntity(
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

        transactionDao.insertTransaction(transaction)
        return@withContext true
    }

    /**
     * Re-categorizes a transaction and retrains the on-device ML model by saving new keyword weights!
     */
    suspend fun recategorizeAndTrainMl(transaction: TransactionEntity, newCategory: String) = withContext(Dispatchers.IO) {
        // 1. Update the transaction
        val updatedTx = transaction.copy(
            category = newCategory,
            isUserModified = true,
            mlConfidence = 0.99f
        )
        transactionDao.updateTransaction(updatedTx)

        // 2. Extract key merchant tokens to retrain ML rules
        val merchantKey = transaction.merchant.lowercase().split(" ").firstOrNull { it.length > 2 }
        if (!merchantKey.isNullOrEmpty()) {
            val rule = MlRuleEntity(
                keyword = merchantKey,
                categoryName = newCategory,
                weight = 3.0f // Higher user weight
            )
            transactionDao.insertMlRule(rule)
        }
    }
}
