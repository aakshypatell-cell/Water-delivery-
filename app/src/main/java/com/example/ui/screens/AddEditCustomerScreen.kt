package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.ui.viewmodel.WaterCamperViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCustomerScreen(
    customerId: Long?,
    viewModel: WaterCamperViewModel,
    onBack: () -> Unit
) {
    val allCustomers by viewModel.allCustomers.collectAsState()
    val existingCustomer = remember(customerId, allCustomers) {
        if (customerId != null) allCustomers.find { it.id == customerId } else null
    }

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var dailyQuantity by remember { mutableIntStateOf(1) }
    var camperSize by remember { mutableStateOf("20L") }
    var pricePerCamperText by remember { mutableStateOf("30") }
    var isActive by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }

    var errorMsg by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(existingCustomer) {
        existingCustomer?.let { cust ->
            name = cust.name
            phone = cust.phone
            address = cust.address
            area = cust.area
            dailyQuantity = cust.dailyQuantity
            camperSize = cust.camperSize
            pricePerCamperText = cust.pricePerCamper.toInt().toString()
            isActive = cust.isActive
            notes = cust.notes
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (existingCustomer == null) "Add New Customer" else "Edit Customer",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (existingCustomer != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                        }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Delivery Address / House / Flat No *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = area,
                onValueChange = { area = it },
                label = { Text("Route / Area / Sector Name *") },
                placeholder = { Text("e.g. Sector 14, Commercial Market") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Camper Size selection chips
            Column {
                Text("Camper Size", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("20L", "10L", "25L", "15L").forEach { size ->
                        FilterChip(
                            selected = camperSize == size,
                            onClick = { camperSize = size },
                            label = { Text(size) },
                            modifier = Modifier.padding(end = 6.dp)
                        )
                    }
                }
            }

            // Daily Default Quantity Counter
            Column {
                Text("Default Daily Quantity", style = MaterialTheme.typography.labelLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (dailyQuantity > 1) dailyQuantity-- },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease")
                    }

                    Text(
                        text = "$dailyQuantity Cans / day",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    OutlinedButton(
                        onClick = { dailyQuantity++ },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase")
                    }
                }
            }

            OutlinedTextField(
                value = pricePerCamperText,
                onValueChange = { pricePerCamperText = it },
                label = { Text("Price per Camper (₹) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Customer Status", fontWeight = FontWeight.Bold)
                    Text(if (isActive) "Active (Appears in daily delivery list)" else "Inactive", fontSize = 12.sp, color = Color.Gray)
                }
                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Delivery Notes / Instructions (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    val price = pricePerCamperText.toDoubleOrNull()
                    if (name.isBlank() || phone.isBlank() || address.isBlank() || area.isBlank()) {
                        errorMsg = "Please fill in all mandatory fields (*)"
                    } else if (price == null || price <= 0) {
                        errorMsg = "Please enter a valid price per camper"
                    } else {
                        val cust = Customer(
                            id = existingCustomer?.id ?: 0L,
                            name = name.trim(),
                            phone = phone.trim(),
                            address = address.trim(),
                            area = area.trim(),
                            dailyQuantity = dailyQuantity,
                            camperSize = camperSize,
                            pricePerCamper = price,
                            isActive = isActive,
                            notes = notes.trim()
                        )
                        viewModel.saveCustomer(cust)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (existingCustomer == null) "Add Customer" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showDeleteConfirm && existingCustomer != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete ${existingCustomer.name}? Delivery and payment history will be preserved.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(existingCustomer)
                        showDeleteConfirm = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
