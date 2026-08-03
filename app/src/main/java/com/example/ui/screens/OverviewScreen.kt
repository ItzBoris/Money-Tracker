package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ExpenseViewModel
import com.example.ui.components.ExpressiveFloatingSummaryHeader
import com.example.ui.components.ExpressivePillChip
import com.example.ui.components.TransactionCard

@Composable
fun OverviewScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val transactions by viewModel.allTransactions.collectAsState()
    val filteredTxs by viewModel.filteredTransactions.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val totalSpent = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val netBalance = totalIncome - totalSpent
    val smsCount = transactions.count { it.sourceType == "SMS" }

    val categories = listOf(
        "All", "Food & Dining", "Shopping", "Bills & Utilities",
        "Transport", "Entertainment", "Health", "Income", "Transfer"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Floating Top Summary Pill
        item {
            ExpressiveFloatingSummaryHeader(
                totalSpent = totalSpent,
                totalIncome = totalIncome,
                netBalance = netBalance,
                smsCount = smsCount,
                onSyncSmsClick = { viewModel.scanSms(context) }
            )
        }

        // Quick Expressive Action Pills
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { viewModel.openAddDialog() },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Text(text = "Add Expense", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.scanSms(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Sms, contentDescription = null)
                    Text(text = "Scan SMS", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Category Pills Filter Carousel
        item {
            Column {
                Text(
                    text = "Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(categories) { cat ->
                        ExpressivePillChip(
                            label = cat,
                            isSelected = selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) }
                        )
                    }
                }
            }
        }

        // Recent Financial Transactions Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "${filteredTxs.size} items",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of transactions
        if (filteredTxs.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions found in this category.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            items(filteredTxs.take(10), key = { it.id }) { tx ->
                TransactionCard(
                    transaction = tx,
                    onEditClick = { viewModel.openEditDialog(tx) },
                    onRecategorizeClick = { viewModel.openRecategorizeDialog(tx) }
                )
            }
        }
    }
}
