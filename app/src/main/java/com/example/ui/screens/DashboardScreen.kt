package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.DeliveryStatusChip
import com.example.ui.components.MetricCard
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PendingRed
import com.example.ui.theme.PendingRedContainer
import com.example.ui.theme.StatusDeliveredGreen
import com.example.ui.theme.StatusDeliveredGreenContainer
import com.example.ui.theme.WaterBluePrimary
import com.example.ui.theme.WaterBluePrimaryContainer
import com.example.ui.viewmodel.WaterCamperViewModel

@Composable
fun DashboardScreen(
    viewModel: WaterCamperViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToAddCustomer: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val state by viewModel.dashboardState.collectAsState()
    val deliveries by viewModel.registerDeliveries.collectAsState()
    val customers by viewModel.activeCustomers.collectAsState()

    val totalActive = state.totalCustomers
    val deliveredCount = state.todayDeliveredCount
    val pendingCount = state.todayPendingCount
    val progress = if (totalActive > 0) deliveredCount.toFloat() / totalActive.toFloat() else 0f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Business Header Banner
        item {
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = "Water",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = state.businessName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Delivery Management Portal",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Role Tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (state.currentRole == UserRole.ADMIN) Color(0xFFFFD54F) else Color(0xFF81C784)
                        ) {
                            Text(
                                text = state.currentRole.name,
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Route Progress
                    Text(
                        text = "Today's Delivery Progress: $deliveredCount of $totalActive Customers Done",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF00E676),
                        trackColor = Color.White.copy(alpha = 0.3f),
                    )
                }
            }
        }

        // Action Buttons Row
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onNavigateToRegister,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Daily Register", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onNavigateToAddCustomer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Customer", fontSize = 13.sp)
                }
            }
        }

        // Dashboard Metric Cards Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Today Delivered",
                        value = "${state.todayTotalCans} Cans",
                        subtext = "${state.todayDeliveredCount} done",
                        icon = Icons.Default.CheckCircle,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        iconBgColor = StatusDeliveredGreenContainer,
                        iconTint = StatusDeliveredGreen,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToRegister
                    )
                    MetricCard(
                        title = "Customers",
                        value = "${state.totalCustomers}",
                        subtext = "Active routes",
                        icon = Icons.Default.People,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = CardBorder,
                        iconBgColor = WaterBluePrimaryContainer,
                        iconTint = WaterBluePrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCustomers
                    )
                }

                if (state.currentRole == UserRole.ADMIN) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetricCard(
                            title = "Pending Due",
                            value = "₹${state.totalPendingBalance.toInt()}",
                            subtext = "Uncollected balance",
                            icon = Icons.Default.Payments,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = PendingRed,
                            borderColor = CardBorder,
                            iconBgColor = PendingRedContainer,
                            iconTint = PendingRed,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToPayments
                        )
                        MetricCard(
                            title = "Revenue",
                            value = "₹${state.monthRevenue.toInt()}",
                            subtext = "This month",
                            icon = Icons.Default.LocalShipping,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            iconBgColor = MaterialTheme.colorScheme.primaryContainer,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToReports
                        )
                    }
                }
            }
        }

        // Quick Daily Register Section Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = "Today's Delivery Log",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                ElevatedButton(onClick = onNavigateToRegister) {
                    Text("View Full Register")
                }
            }
        }

        // List of Customers for today's quick register preview
        val customerMap = customers.associateBy { it.id }
        val deliveriesList = deliveries.take(6)

        if (deliveriesList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("No deliveries marked for today yet.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onNavigateToRegister) {
                            Text("Open Daily Register to Mark")
                        }
                    }
                }
            }
        } else {
            items(deliveriesList) { del ->
                val cust = customerMap[del.customerId]
                if (cust != null) {
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
                                Text(
                                    text = cust.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${cust.area} • ${cust.camperSize}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
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
    }
}
