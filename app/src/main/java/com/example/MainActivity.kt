package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.repository.ExpenseRepository
import com.example.ui.ExpenseViewModel
import com.example.ui.NavTab
import com.example.ui.components.FloatingPillNavBar
import com.example.ui.dialogs.AddEditTransactionDialog
import com.example.ui.dialogs.RecategorizeDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.SmsMlScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ExpenseRepository(database.transactionDao())
        val factory = ExpenseViewModel.Factory(repository)
        viewModel = ViewModelProvider(this, factory)[ExpenseViewModel::class.java]

        setContent {
            ExpenseTrackerTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: ExpenseViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val showAddDialog by viewModel.showAddDialog.collectAsState()
    val editingTx by viewModel.editingTransaction.collectAsState()
    val recategorizeTx by viewModel.recategorizeTarget.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            FloatingPillNavBar(
                currentTab = currentTab,
                onTabSelected = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                NavTab.OVERVIEW -> OverviewScreen(viewModel = viewModel)
                NavTab.TRANSACTIONS -> TransactionsScreen(viewModel = viewModel)
                NavTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                NavTab.SMS_ML -> SmsMlScreen(viewModel = viewModel)
                NavTab.BUDGETS -> BudgetsScreen(viewModel = viewModel)
            }

            if (showAddDialog) {
                AddEditTransactionDialog(
                    transaction = editingTx,
                    onDismiss = { viewModel.closeAddDialog() },
                    onSave = { title, amount, category, type, merchant, bankAccount ->
                        viewModel.saveTransaction(title, amount, category, type, merchant, bankAccount)
                    }
                )
            }

            if (recategorizeTx != null) {
                RecategorizeDialog(
                    transaction = recategorizeTx!!,
                    onDismiss = { viewModel.closeRecategorizeDialog() },
                    onConfirm = { newCategory ->
                        viewModel.recategorizeAndTrainMl(recategorizeTx!!, newCategory)
                    }
                )
            }
        }
    }
}
