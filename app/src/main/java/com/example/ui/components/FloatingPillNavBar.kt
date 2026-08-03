package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.NavTab

@Composable
fun FloatingPillNavBar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .shadow(elevation = 12.dp, shape = CircleShape)
                .clip(CircleShape),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.96f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavBarItem(
                    tab = NavTab.OVERVIEW,
                    icon = Icons.Default.Home,
                    label = "Overview",
                    isSelected = currentTab == NavTab.OVERVIEW,
                    onClick = { onTabSelected(NavTab.OVERVIEW) }
                )
                NavBarItem(
                    tab = NavTab.TRANSACTIONS,
                    icon = Icons.Default.ReceiptLong,
                    label = "Items",
                    isSelected = currentTab == NavTab.TRANSACTIONS,
                    onClick = { onTabSelected(NavTab.TRANSACTIONS) }
                )
                NavBarItem(
                    tab = NavTab.ANALYTICS,
                    icon = Icons.Default.BarChart,
                    label = "Graphs",
                    isSelected = currentTab == NavTab.ANALYTICS,
                    onClick = { onTabSelected(NavTab.ANALYTICS) }
                )
                NavBarItem(
                    tab = NavTab.SMS_ML,
                    icon = Icons.Default.AutoAwesome,
                    label = "SMS ML",
                    isSelected = currentTab == NavTab.SMS_ML,
                    onClick = { onTabSelected(NavTab.SMS_ML) }
                )
                NavBarItem(
                    tab = NavTab.BUDGETS,
                    icon = Icons.Default.TrackChanges,
                    label = "Budgets",
                    isSelected = currentTab == NavTab.BUDGETS,
                    onClick = { onTabSelected(NavTab.BUDGETS) }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    tab: NavTab,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_bg_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "nav_content_color"
    )

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.padding(2.dp)
            )
            if (isSelected) {
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
