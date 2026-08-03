package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ExpenseViewModel
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BudgetsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val budgets by viewModel.allBudgets.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    val categoryExpenses = transactions
        .filter { it.type == "EXPENSE" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Monthly Category Budgets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Expressive progress tracking with warning limits",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(budgets, key = { it.categoryName }) { budget ->
            val spent = categoryExpenses[budget.categoryName] ?: 0.0
            val progress = (spent / budget.monthlyBudget).coerceIn(0.0, 1.0).toFloat()
            val categoryColor = getCategoryColor(budget.categoryName)
            val isOverBudget = spent > budget.monthlyBudget

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(categoryColor.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getCategoryIcon(budget.categoryName),
                                    contentDescription = null,
                                    tint = categoryColor
                                )
                            }

                            Column {
                                Text(
                                    text = budget.categoryName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${currencyFormat.format(spent)} spent of ${currencyFormat.format(budget.monthlyBudget)}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.clip(CircleShape),
                            color = if (isOverBudget) MaterialTheme.colorScheme.errorContainer else categoryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (isOverBudget) "Over Limit" else "${(progress * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverBudget) MaterialTheme.colorScheme.error else categoryColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Expressive Pill Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(CircleShape)
                                .background(if (isOverBudget) MaterialTheme.colorScheme.error else categoryColor)
                        )
                    }
                }
            }
        }
    }
}
