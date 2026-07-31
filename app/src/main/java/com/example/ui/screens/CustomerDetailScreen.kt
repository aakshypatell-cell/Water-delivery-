package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.ui.components.DeliveryStatusChip
import com.example.ui.components.RecordPaymentDialog
import com.example.ui.util.PdfReportGenerator
import com.example.ui.util.WhatsAppShareHelper
import com.example.ui.viewmodel.WaterCamperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDetailScreen(
    customerId: Long,
    viewModel: WaterCamperViewModel,
    onBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.allCustomers.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val customer = remember(customerId, customers) { customers.find { it.id == customerId } }

    val monthPrefix = viewModel.currentMonthStr

    val deliveriesState by viewModel.repository.getDeliveriesForCustomer(customerId).collectAsState(initial = emptyList())
    val paymentsState by viewModel.repository.getPaymentsForCustomer(customerId).collectAsState(initial = emptyList())

    val monthDeliveries = remember(deliveriesState, monthPrefix) {
        deliveriesState.filter { it.date.startsWith(monthPrefix) }
    }
    val monthPayments = remember(paymentsState, monthPrefix) {
        paymentsState.filter { it.date.startsWith(monthPrefix) }
    }

    val deliveredDays = monthDeliveries.count { it.status == DeliveryStatus.DELIVERED }
    val skippedDays = monthDeliveries.count { it.status == DeliveryStatus.SKIP || it.status == DeliveryStatus.HOLIDAY }
    val totalCans = monthDeliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered }
    val totalBilled = monthDeliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered * it.priceAtTime }
    val totalPaid = paymentsState.sumOf { it.amount } // total payments overall or for month
    val allBilledOverall = deliveriesState.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered * it.priceAtTime }
    val pendingBalance = (allBilledOverall - totalPaid).coerceAtLeast(0.0)

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }

    if (customer == null) {
        Scaffold { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Customer not found")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(customer.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(customer.id) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Customer")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Customer Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(text = customer.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text(text = "${customer.area} • ${customer.address}", fontSize = 12.sp, color = Color.Gray)
                        }
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))
                                context.startActivity(intent)
                            }
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Rate: ₹${customer.pricePerCamper.toInt()}/can (${customer.camperSize}) • Daily: ${customer.dailyQuantity} cans",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Monthly Ledger Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Monthly Statement ($monthPrefix)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color.Gray)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("Delivered Days", fontSize = 11.sp, color = Color.Gray)
                            Text("$deliveredDays days ($totalCans cans)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Billed Amount", fontSize = 11.sp, color = Color.Gray)
                            Text("₹${totalBilled.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text("Net Pending Due", fontSize = 11.sp, color = Color.Gray)
                            Text("₹${pendingBalance.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (pendingBalance > 0) Color.Red else Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sharing and Payment Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // WhatsApp Bill
                        Button(
                            onClick = {
                                WhatsAppShareHelper.sendBillOnWhatsApp(
                                    context = context,
                                    customer = customer,
                                    monthName = monthPrefix,
                                    deliveredDays = deliveredDays,
                                    totalCans = totalCans,
                                    totalAmount = totalBilled,
                                    paidAmount = totalPaid,
                                    pendingAmount = pendingBalance,
                                    upiId = appSettings?.upiId ?: "aquapure@upi"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 12.sp)
                        }

                        // PDF Invoice
                        OutlinedButton(
                            onClick = {
                                PdfReportGenerator.generateCustomerMonthlyPdf(
                                    context = context,
                                    businessName = appSettings?.businessName ?: "Aqua Pure Water",
                                    monthName = monthPrefix,
                                    customer = customer,
                                    deliveries = monthDeliveries,
                                    payments = monthPayments,
                                    totalAmount = totalBilled,
                                    totalPaid = totalPaid,
                                    pendingAmount = pendingBalance
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF Bill", fontSize = 12.sp)
                        }

                        // Record Payment
                        Button(
                            onClick = { showRecordPaymentDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AddCard, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Payment", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tabs for Delivery History vs Payment History
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Daily Register (${deliveriesState.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Payments Log (${paymentsState.size})") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> {
                    // Deliveries Log
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(deliveriesState, key = { it.id }) { del ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(text = del.date, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        if (del.notes.isNotBlank()) {
                                            Text(text = del.notes, fontSize = 11.sp, color = Color.Gray)
                                        }
                                    }

                                    DeliveryStatusChip(
                                        status = del.status,
                                        quantity = del.quantityDelivered
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Payments Log
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(paymentsState, key = { it.id }) { pay ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Text(text = "₹${pay.amount.toInt()} (${pay.paymentMode.name})", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF2E7D32))
                                        Text(text = "Date: ${pay.date} ${if (pay.referenceNo.isNotBlank()) "• Ref: ${pay.referenceNo}" else ""}", fontSize = 12.sp, color = Color.Gray)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = "Received",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            customer = customer,
            suggestedPendingAmount = pendingBalance,
            onDismiss = { showRecordPaymentDialog = false },
            onSavePayment = { amount, date, mode, refNo, notes ->
                viewModel.recordPayment(
                    customerId = customer.id,
                    amount = amount,
                    date = date,
                    mode = mode,
                    refNo = refNo,
                    notes = notes
                )
                showRecordPaymentDialog = false
            }
        )
    }
}
