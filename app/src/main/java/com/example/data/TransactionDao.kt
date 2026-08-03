package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE dateMillis >= :startTimeMillis AND dateMillis <= :endTimeMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startTimeMillis: Long, endTimeMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // Category Budgets
    @Query("SELECT * FROM category_budgets")
    fun getAllCategoryBudgets(): Flow<List<CategoryBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudget(budget: CategoryBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryBudgets(budgets: List<CategoryBudgetEntity>)

    // ML Rules
    @Query("SELECT * FROM ml_rules")
    fun getAllMlRules(): Flow<List<MlRuleEntity>>

    @Query("SELECT * FROM ml_rules")
    suspend fun getMlRulesList(): List<MlRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMlRule(rule: MlRuleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMlRules(rules: List<MlRuleEntity>)
}
