package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ExpenseViewModel
import com.example.ui.components.ExpressiveCategoryBadge
import com.example.ui.components.getCategoryColor
import com.example.ui.components.getCategoryIcon
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.allTransactions.collectAsState()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    val expenses = transactions.filter { it.type == "EXPENSE" }
    val totalExpense = expenses.sumOf { it.amount }

    // Category summary map
    val categoryTotals = expenses.groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header
        item {
            Column {
                Text(
                    text = "Visual Analytics & Trends",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Automated insights from parsed SMS and manual logs",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Category Breakdown Donut Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Category Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalExpense > 0) {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DonutChartCanvas(categoryTotals = categoryTotals, totalExpense = totalExpense)

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total Spent",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currencyFormat.format(totalExpense),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Category Percentage Legend Pills
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categoryTotals.forEach { (cat, amount) ->
                                val percentage = ((amount / totalExpense) * 100).toInt()
                                val color = getCategoryColor(cat)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(color)
                                        )
                                        Text(
                                            text = cat,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = currencyFormat.format(amount),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            modifier = Modifier.clip(CircleShape),
                                            color = color.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$percentage%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = color,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("No expense data available for chart.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 2. Spending Trend Wave Bar Chart
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Monthly Spending Pattern",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Daily expense velocity visualization",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SpendingBarChartCanvas(expenses = expenses)
                }
            }
        }
    }
}

@Composable
fun DonutChartCanvas(
    categoryTotals: List<Pair<String, Double>>,
    totalExpense: Double
) {
    Canvas(modifier = Modifier.size(190.dp)) {
        val strokeWidth = 32.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val radius = diameter / 2
        val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

        var startAngle = -90f

        categoryTotals.forEach { (cat, amount) ->
            val sweepAngle = ((amount / totalExpense) * 360f).toFloat()
            val color = getCategoryColor(cat)

            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 3f, // Gap between segments
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
fun SpendingBarChartCanvas(expenses: List<com.example.data.TransactionEntity>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val barColor = MaterialTheme.colorScheme.primaryContainer

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        val width = size.width
        val height = size.height

        // Simulated 7 day spending slots
        val sampleDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val values = listOf(45f, 120f, 85f, 30f, 160f, 210f, 75f)
        val maxValue = values.maxOrNull() ?: 1f

        val barWidth = 28.dp.toPx()
        val spacing = (width - (values.size * barWidth)) / (values.size + 1)

        values.forEachIndexed { idx, valAmount ->
            val barHeight = (valAmount / maxValue) * (height - 40.dp.toPx())
            val x = spacing + idx * (barWidth + spacing)
            val y = height - barHeight - 20.dp.toPx()

            // Draw rounded bar
            drawRoundRect(
                color = if (valAmount == maxValue) primaryColor else barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
            )
        }
    }
}
