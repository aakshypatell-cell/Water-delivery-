package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeliveryStatus
import com.example.ui.util.PdfReportGenerator
import com.example.ui.util.WhatsAppShareHelper
import com.example.ui.viewmodel.WaterCamperViewModel

@Composable
fun ReportsScreen(
    viewModel: WaterCamperViewModel,
    onNavigateToCustomerLedger: (Long) -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.activeCustomers.collectAsState()
    val appSettings by viewModel.appSettings.collectAsState()

    val monthPrefix = viewModel.currentMonthStr

    val monthDeliveries by viewModel.repository.getAllDeliveriesForMonth(monthPrefix).collectAsState(initial = emptyList())
    val monthPayments by viewModel.repository.getPaymentsForMonth(monthPrefix).collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }

    val totalCansDelivered = monthDeliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered }
    val totalBilled = monthDeliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered * it.priceAtTime }
    val totalCollected = monthPayments.sumOf { it.amount }
    val netPending = (totalBilled - totalCollected).coerceAtLeast(0.0)

    val filteredCustomers = customers.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.area.contains(searchQuery, ignoreCase = true)
    }

    val customerDeliveriesMap = monthDeliveries.groupBy { it.customerId }
    val customerPaymentsMap = monthPayments.groupBy { it.customerId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Business Monthly Performance Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Monthly Reports & Bills ($monthPrefix)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text("Total Cans Delivered", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("$totalCansDelivered Cans", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Total Billed", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("₹${totalBilled.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column {
                        Text("Collected", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("₹${totalCollected.toInt()}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB9F6CA))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search customer for invoice...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Customer Bills List", fontWeight = FontWeight.Bold, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredCustomers, key = { it.id }) { cust ->
                val custDeliveries = customerDeliveriesMap[cust.id] ?: emptyList()
                val custPayments = customerPaymentsMap[cust.id] ?: emptyList()

                val deliveredDays = custDeliveries.count { it.status == DeliveryStatus.DELIVERED }
                val cansCount = custDeliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered }
                val billed = custDeliveries.filter { it.status == DeliveryStatus.DELIVERED }.sumOf { it.quantityDelivered * it.priceAtTime }
                val paid = custPayments.sumOf { it.amount }
                val due = (billed - paid).coerceAtLeast(0.0)

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToCustomerLedger(cust.id) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(text = cust.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = "${cust.area} • $cansCount Cans ($deliveredDays days)", fontSize = 12.sp, color = Color.Gray)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Bill: ₹${billed.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(
                                    text = if (due > 0) "Pending: ₹${due.toInt()}" else "Settled ✅",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (due > 0) Color.Red else Color(0xFF2E7D32)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    WhatsAppShareHelper.sendBillOnWhatsApp(
                                        context = context,
                                        customer = cust,
                                        monthName = monthPrefix,
                                        deliveredDays = deliveredDays,
                                        totalCans = cansCount,
                                        totalAmount = billed,
                                        paidAmount = paid,
                                        pendingAmount = due,
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

                            OutlinedButton(
                                onClick = {
                                    PdfReportGenerator.generateCustomerMonthlyPdf(
                                        context = context,
                                        businessName = appSettings?.businessName ?: "Aqua Pure Water",
                                        monthName = monthPrefix,
                                        customer = cust,
                                        deliveries = custDeliveries,
                                        payments = custPayments,
                                        totalAmount = billed,
                                        totalPaid = paid,
                                        pendingAmount = due
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export PDF", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
