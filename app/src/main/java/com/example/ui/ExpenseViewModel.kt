package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CategoryBudgetEntity
import com.example.data.TransactionEntity
import com.example.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavTab {
    OVERVIEW,
    TRANSACTIONS,
    ANALYTICS,
    SMS_ML,
    BUDGETS
}

class ExpenseViewModel(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _currentTab = MutableStateFlow(NavTab.OVERVIEW)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("All")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSmsScanning = MutableStateFlow(false)
    val isSmsScanning: StateFlow<Boolean> = _isSmsScanning.asStateFlow()

    private val _lastSyncMessage = MutableStateFlow<String?>(null)
    val lastSyncMessage: StateFlow<String?> = _lastSyncMessage.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction: StateFlow<TransactionEntity?> = _editingTransaction.asStateFlow()

    private val _recategorizeTarget = MutableStateFlow<TransactionEntity?>(null)
    val recategorizeTarget: StateFlow<TransactionEntity?> = _recategorizeTarget.asStateFlow()

    val allTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allBudgets: StateFlow<List<CategoryBudgetEntity>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered transactions for UI based on selected category and search query
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        allTransactions,
        selectedCategory,
        searchQuery
    ) { txs, category, query ->
        txs.filter { tx ->
            val matchesCategory = category == null || category == "All" || tx.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    tx.title.contains(query, ignoreCase = true) ||
                    tx.merchant.contains(query, ignoreCase = true) ||
                    tx.category.contains(query, ignoreCase = true) ||
                    tx.bankAccount.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: NavTab) {
        _currentTab.value = tab
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openAddDialog() {
        _editingTransaction.value = null
        _showAddDialog.value = true
    }

    fun openEditDialog(transaction: TransactionEntity) {
        _editingTransaction.value = transaction
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
        _editingTransaction.value = null
    }

    fun openRecategorizeDialog(transaction: TransactionEntity) {
        _recategorizeTarget.value = transaction
    }

    fun closeRecategorizeDialog() {
        _recategorizeTarget.value = null
    }

    fun saveTransaction(
        title: String,
        amount: Double,
        category: String,
        type: String,
        merchant: String,
        bankAccount: String
    ) {
        viewModelScope.launch {
            val editing = _editingTransaction.value
            if (editing != null) {
                repository.updateTransaction(
                    editing.copy(
                        title = title,
                        amount = amount,
                        category = category,
                        type = type,
                        merchant = merchant,
                        bankAccount = bankAccount
                    )
                )
            } else {
                val newTx = TransactionEntity(
                    title = title,
                    amount = amount,
                    dateMillis = System.currentTimeMillis(),
                    type = type,
                    category = category,
                    merchant = merchant,
                    bankAccount = bankAccount,
                    sourceType = "MANUAL",
                    mlConfidence = 1.0f,
                    isUserModified = true
                )
                repository.insertTransaction(newTx)
            }
            closeAddDialog()
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun recategorizeAndTrainMl(transaction: TransactionEntity, newCategory: String) {
        viewModelScope.launch {
            repository.recategorizeAndTrainMl(transaction, newCategory)
            _lastSyncMessage.value = "ML Model retrained for category '$newCategory'!"
            closeRecategorizeDialog()
        }
    }

    fun updateBudget(categoryName: String, limit: Double) {
        viewModelScope.launch {
            val existing = allBudgets.value.find { it.categoryName == categoryName }
            val updated = existing?.copy(monthlyBudget = limit)
                ?: CategoryBudgetEntity(categoryName, limit, "category", "#00F5D4")
            repository.updateBudget(updated)
        }
    }

    fun scanSms(context: Context) {
        viewModelScope.launch {
            _isSmsScanning.value = true
            _lastSyncMessage.value = "Scanning Google Messages inbox..."
            try {
                val count = repository.scanAndSyncSms(context)
                _lastSyncMessage.value = if (count > 0) {
                    "Successfully imported & ML-categorized $count new transactions from Google Messages!"
                } else {
                    "No new financial messages found in SMS inbox."
                }
            } catch (e: Exception) {
                _lastSyncMessage.value = "SMS Scan Error: ${e.message}"
            } finally {
                _isSmsScanning.value = false
            }
        }
    }

    fun injectSampleSms(sender: String, body: String) {
        viewModelScope.launch {
            _isSmsScanning.value = true
            val success = repository.injectSampleSms(sender, body)
            _isSmsScanning.value = false
            if (success) {
                _lastSyncMessage.value = "Sample bank SMS parsed & ML categorized!"
            } else {
                _lastSyncMessage.value = "Could not parse amount from sample SMS."
            }
        }
    }

    fun clearSyncMessage() {
        _lastSyncMessage.value = null
    }

    class Factory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
                return ExpenseViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
