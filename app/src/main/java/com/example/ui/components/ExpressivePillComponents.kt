package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.theme.BillsCategoryColor
import com.example.ui.theme.EntertainmentCategoryColor
import com.example.ui.theme.FoodCategoryColor
import com.example.ui.theme.GeneralCategoryColor
import com.example.ui.theme.HealthCategoryColor
import com.example.ui.theme.IncomeCategoryColor
import com.example.ui.theme.ShoppingCategoryColor
import com.example.ui.theme.TransportCategoryColor
import com.example.ui.theme.TransferCategoryColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getCategoryColor(category: String): Color {
    return when (category.lowercase(Locale.ROOT)) {
        "food & dining", "food" -> FoodCategoryColor
        "shopping" -> ShoppingCategoryColor
        "bills & utilities", "bills" -> BillsCategoryColor
        "transport" -> TransportCategoryColor
        "entertainment" -> EntertainmentCategoryColor
        "health" -> HealthCategoryColor
        "income" -> IncomeCategoryColor
        "transfer" -> TransferCategoryColor
        else -> GeneralCategoryColor
    }
}

fun getCategoryIcon(category: String): ImageVector {
    return when (category.lowercase(Locale.ROOT)) {
        "food & dining", "food" -> Icons.Default.Restaurant
        "shopping" -> Icons.Default.ShoppingBag
        "bills & utilities", "bills" -> Icons.Default.Bolt
        "transport" -> Icons.Default.DirectionsCar
        "entertainment" -> Icons.Default.Movie
        "health" -> Icons.Default.MedicalServices
        "income" -> Icons.Default.ArrowUpward
        "transfer" -> Icons.Default.ArrowDownward
        else -> Icons.Default.Category
    }
}

@Composable
fun ExpressivePillChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        label = "chip_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "chip_content"
    )

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = backgroundColor,
        contentColor = contentColor,
        shape = CircleShape
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
fun ExpressiveCategoryBadge(
    category: String,
    modifier: Modifier = Modifier
) {
    val color = getCategoryColor(category)
    val icon = getCategoryIcon(category)

    Surface(
        modifier = modifier.clip(CircleShape),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = category,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun MlConfidencePill(
    confidence: Float,
    isSmsParsed: Boolean,
    modifier: Modifier = Modifier
) {
    val percent = (confidence * 100).toInt()
    Surface(
        modifier = modifier.clip(CircleShape),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = if (isSmsParsed) Icons.Default.Sms else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = "$percent% ML",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ExpressiveFloatingSummaryHeader(
    totalSpent: Double,
    totalIncome: Double,
    netBalance: Double,
    smsCount: Int,
    onSyncSmsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, shape = RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Net Balance",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(netBalance),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onSyncSmsClick),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sync Messages ($smsCount)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Income Pill Stat
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    color = IncomeCategoryColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IncomeCategoryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = IncomeCategoryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(text = "Income", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(totalIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = IncomeCategoryColor
                            )
                        }
                    }
                }

                // Spent Pill Stat
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp)),
                    color = FoodCategoryColor.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(FoodCategoryColor.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = null,
                                tint = FoodCategoryColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(text = "Expenses", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = currencyFormat.format(totalSpent),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = FoodCategoryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onEditClick: () -> Unit,
    onRecategorizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.US)
    val isExpense = transaction.type == "EXPENSE"
    val categoryColor = getCategoryColor(transaction.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onEditClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon Circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = transaction.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.merchant,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(text = "•", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = transaction.bankAccount,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExpressiveCategoryBadge(category = transaction.category)
                        MlConfidencePill(
                            confidence = transaction.mlConfidence,
                            isSmsParsed = transaction.sourceType == "SMS"
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = if (isExpense) "-${currencyFormat.format(transaction.amount)}" else "+${currencyFormat.format(transaction.amount)}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = if (isExpense) MaterialTheme.colorScheme.onSurface else IncomeCategoryColor
                )

                Text(
                    text = dateFormat.format(Date(transaction.dateMillis)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                IconButton(
                    onClick = onRecategorizeClick,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Recategorize",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
