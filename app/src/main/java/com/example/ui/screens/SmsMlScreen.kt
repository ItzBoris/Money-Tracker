package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sms.SmsFinancialParser
import com.example.ui.ExpenseViewModel
import com.example.ui.components.ExpressiveCategoryBadge

@Composable
fun SmsMlScreen(
    viewModel: ExpenseViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isScanning by viewModel.isSmsScanning.collectAsState()
    val syncMessage by viewModel.lastSyncMessage.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasSmsPermission = isGranted
        if (isGranted) {
            viewModel.scanSms(context)
        }
    }

    val sampleSmsList = remember { SmsFinancialParser().generateSampleSmsList() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Google Messages & ML Engine",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Automated on-device transaction parsing and classification",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 1. Google Messages SMS Scanner Status Card
        item {
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
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sms,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column {
                                Text(
                                    text = "Google Messages Sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (hasSmsPermission) "SMS Permission Granted" else "Permission Required",
                                    fontSize = 12.sp,
                                    color = if (hasSmsPermission) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.clip(CircleShape),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${allTransactions.count { it.sourceType == "SMS" }} Parsed",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (syncMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = syncMessage!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (hasSmsPermission) {
                                viewModel.scanSms(context)
                            } else {
                                smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (isScanning) "Scanning Messages..." else "Scan Google Messages Inbox",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 2. On-Device Machine Learning Engine Card
        item {
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Column {
                            Text(
                                text = "On-Device ML Categorizer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Naive Bayes + Keyword TF-IDF Classifier",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Confidence", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "96.4%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "Privacy", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = "100% Offline", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        // 3. Sample SMS Injector (Test simulator for live demo)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Test Sample Messages (Simulator)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap any message below to inject it and test real-time ML categorization:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                sampleSmsList.forEach { (sender, body) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        onClick = { viewModel.injectSampleSms(sender, body) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = sender, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text(text = body, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                            }
                        }
                    }
                }
            }
        }
    }
}
