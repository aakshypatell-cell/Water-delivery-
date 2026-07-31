package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Weekend
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.DeliveryRecord
import com.example.data.model.DeliveryStatus
import com.example.ui.components.DeliveryStatusChip
import com.example.ui.components.QuantityOverrideDialog
import com.example.ui.theme.StatusDeliveredGreen
import com.example.ui.theme.StatusDeliveredGreenContainer
import com.example.ui.theme.StatusHolidayPurple
import com.example.ui.theme.StatusSkippedOrange
import com.example.ui.viewmodel.WaterCamperViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyRegisterScreen(
    viewModel: WaterCamperViewModel
) {
    val selectedDate by viewModel.selectedRegisterDate.collectAsState()
    val activeCustomers by viewModel.activeCustomers.collectAsState()
    val deliveries by viewModel.registerDeliveries.collectAsState()
    val areas by viewModel.allAreas.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedArea by remember { mutableStateOf("All") }

    var showMarkAllDialog by remember { mutableStateOf(false) }
    var customerForQuantityDialog by remember { mutableStateOf<Customer?>(null) }

    val deliveryMap = deliveries.associateBy { it.customerId }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Date navigation logic
    val navigateDate: (Int) -> Unit = { daysOffset ->
        try {
            val dateObj = dateFormat.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance()
            cal.time = dateObj
            cal.add(Calendar.DAY_OF_YEAR, daysOffset)
            viewModel.selectedRegisterDate.value = dateFormat.format(cal.time)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Filter customers
    val filteredCustomers = activeCustomers.filter { cust ->
        val matchesArea = (selectedArea == "All" || cust.area == selectedArea)
        val matchesSearch = cust.name.contains(searchQuery, ignoreCase = true) ||
                cust.phone.contains(searchQuery) ||
                cust.address.contains(searchQuery, ignoreCase = true)
        matchesArea && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date Selector Bar
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = { navigateDate(-1) }) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val isToday = selectedDate == viewModel.todayDateStr
                    Text(
                        text = if (isToday) "Today ($selectedDate)" else "Date: $selectedDate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Tap arrows to change date",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { navigateDate(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search customer, phone or address...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Route/Area Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedArea == "All",
                    onClick = { selectedArea = "All" },
                    label = { Text("All Routes") }
                )
            }
            items(areas) { area ->
                FilterChip(
                    selected = selectedArea == area,
                    onClick = { selectedArea = area },
                    label = { Text(area) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Bulk Action Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "${filteredCustomers.size} Customers in list",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )

            Button(
                onClick = { showMarkAllDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusDeliveredGreenContainer, contentColor = StatusDeliveredGreen)
            ) {
                Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark All Pending", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customer List for Daily Register
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCustomers, key = { it.id }) { customer ->
                val delivery = deliveryMap[customer.id]
                val currentStatus = delivery?.status ?: DeliveryStatus.NOT_DELIVERED
                val currentQty = delivery?.quantityDelivered ?: customer.dailyQuantity

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = customer.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "${customer.area} • ${customer.address}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Default: ${customer.dailyQuantity} x ${customer.camperSize} @ ₹${customer.pricePerCamper.toInt()}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            DeliveryStatusChip(
                                status = currentStatus,
                                quantity = currentQty,
                                onClick = {
                                    customerForQuantityDialog = customer
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // One-Tap Quick Marking Buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Delivered Button
                            Button(
                                onClick = {
                                    viewModel.markDeliveryStatus(
                                        customerId = customer.id,
                                        status = DeliveryStatus.DELIVERED,
                                        quantity = customer.dailyQuantity,
                                        priceAtTime = customer.pricePerCamper
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == DeliveryStatus.DELIVERED) StatusDeliveredGreen else Color(0xFFE8F5E9),
                                    contentColor = if (currentStatus == DeliveryStatus.DELIVERED) Color.White else StatusDeliveredGreen
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delivered", fontSize = 12.sp)
                            }

                            // Skip Button
                            Button(
                                onClick = {
                                    viewModel.markDeliveryStatus(
                                        customerId = customer.id,
                                        status = DeliveryStatus.SKIP,
                                        quantity = 0,
                                        priceAtTime = customer.pricePerCamper
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (currentStatus == DeliveryStatus.SKIP) StatusSkippedOrange else Color(0xFFFFF3E0),
                                    contentColor = if (currentStatus == DeliveryStatus.SKIP) Color.White else StatusSkippedOrange
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Skip", fontSize = 12.sp)
                            }

                            // Edit Qty / More Button
                            IconButton(
                                onClick = { customerForQuantityDialog = customer },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F7FA))
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit Quantity",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialog for Mark All
    if (showMarkAllDialog) {
        AlertDialog(
            onDismissRequest = { showMarkAllDialog = false },
            title = { Text("Mark All Pending?") },
            text = { Text("This will mark all unmarked active customers for $selectedDate as Delivered with their default daily quantity.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.markAllPendingAsDelivered()
                        showMarkAllDialog = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMarkAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Quantity Override Dialog
    customerForQuantityDialog?.let { cust ->
        val delivery = deliveryMap[cust.id]
        QuantityOverrideDialog(
            customer = cust,
            currentStatus = delivery?.status ?: DeliveryStatus.DELIVERED,
            currentQuantity = delivery?.quantityDelivered ?: cust.dailyQuantity,
            date = selectedDate,
            onDismiss = { customerForQuantityDialog = null },
            onConfirm = { status, qty, notes ->
                viewModel.markDeliveryStatus(
                    customerId = cust.id,
                    status = status,
                    quantity = qty,
                    priceAtTime = cust.pricePerCamper,
                    notes = notes
                )
                customerForQuantityDialog = null
            }
        )
    }
}
