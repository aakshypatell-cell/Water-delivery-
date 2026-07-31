package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.DeliveryStatus

@Composable
fun QuantityOverrideDialog(
    customer: Customer,
    currentStatus: DeliveryStatus,
    currentQuantity: Int,
    date: String,
    onDismiss: () -> Unit,
    onConfirm: (status: DeliveryStatus, quantity: Int, notes: String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(currentStatus) }
    var qty by remember { mutableIntStateOf(if (currentQuantity > 0) currentQuantity else customer.dailyQuantity) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Mark Delivery - ${customer.name}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "Date: $date • Default: ${customer.dailyQuantity} cans (${customer.camperSize})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Status", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    DeliveryStatus.values().forEach { status ->
                        FilterChip(
                            selected = selectedStatus == status,
                            onClick = {
                                selectedStatus = status
                                if (status != DeliveryStatus.DELIVERED) {
                                    qty = 0
                                } else if (qty == 0) {
                                    qty = customer.dailyQuantity
                                }
                            },
                            label = {
                                Text(
                                    when (status) {
                                        DeliveryStatus.DELIVERED -> "Delivered"
                                        DeliveryStatus.SKIP -> "Skip"
                                        DeliveryStatus.HOLIDAY -> "Holiday"
                                        DeliveryStatus.NOT_DELIVERED -> "Reset"
                                    }
                                )
                            },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }

                if (selectedStatus == DeliveryStatus.DELIVERED) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Quantity Delivered", style = MaterialTheme.typography.labelLarge)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = { if (qty > 1) qty-- }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }

                        Text(
                            text = "$qty Cans",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        IconButton(
                            onClick = { qty++ }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedStatus, qty, notes)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}
