package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.TransactionEntity
import com.example.ui.components.ExpressivePillChip

@Composable
fun AddEditTransactionDialog(
    transaction: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, category: String, type: String, merchant: String, bankAccount: String) -> Unit
) {
    var title by remember { mutableStateOf(transaction?.title ?: "") }
    var amountText by remember { mutableStateOf(transaction?.amount?.toString() ?: "") }
    var category by remember { mutableStateOf(transaction?.category ?: "Food & Dining") }
    var type by remember { mutableStateOf(transaction?.type ?: "EXPENSE") }
    var merchant by remember { mutableStateOf(transaction?.merchant ?: "") }
    var bankAccount by remember { mutableStateOf(transaction?.bankAccount ?: "HDFC *8021") }

    val categories = listOf(
        "Food & Dining", "Shopping", "Bills & Utilities",
        "Transport", "Entertainment", "Health", "Income", "Transfer", "General"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (transaction == null) "Add Expense / Income" else "Edit Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Expense / Income Toggle Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpressivePillChip(
                        label = "Expense",
                        isSelected = type == "EXPENSE",
                        onClick = { type = "EXPENSE" },
                        modifier = Modifier.weight(1f)
                    )
                    ExpressivePillChip(
                        label = "Income",
                        isSelected = type == "INCOME",
                        onClick = { type = "INCOME" },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Description") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    singleLine = true
                )

                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant / Payee") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    singleLine = true
                )

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        ExpressivePillChip(
                            label = cat,
                            isSelected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amount > 0) {
                                onSave(title, amount, category, type, merchant.ifBlank { "Merchant" }, bankAccount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = CircleShape
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
