package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object DailyRegister : Screen("daily_register", "Daily Register", Icons.Default.Assignment)
    object Customers : Screen("customers", "Customers", Icons.Default.People)
    object LedgerPayments : Screen("ledger_payments", "Payments & Ledger", Icons.AutoMirrored.Filled.ReceiptLong)
    object Reports : Screen("reports", "Reports & Bills")
    object SettingsRole : Screen("settings_role", "Settings & Role", Icons.Default.Settings)
    
    object CustomerDetail : Screen("customer_detail/{customerId}", "Customer Ledger") {
        fun createRoute(customerId: Long) = "customer_detail/$customerId"
    }
    
    object AddEditCustomer : Screen("add_edit_customer?customerId={customerId}", "Manage Customer") {
        fun createRoute(customerId: Long? = null) = if (customerId != null) "add_edit_customer?customerId=$customerId" else "add_edit_customer"
    }
}
