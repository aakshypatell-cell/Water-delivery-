package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.navigation.Screen
import com.example.ui.screens.AddEditCustomerScreen
import com.example.ui.screens.CustomerDetailScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DailyRegisterScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LedgerPaymentsScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsRoleScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WaterCamperViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                WaterCamperApp()
            }
        }
    }
}

@Composable
fun WaterCamperApp() {
    val navController = rememberNavController()
    val viewModel: WaterCamperViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        Screen.Dashboard,
        Screen.DailyRegister,
        Screen.Customers,
        Screen.LedgerPayments,
        Screen.SettingsRole
    )

    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon ?: Icons.Default.Dashboard,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Screen.DailyRegister.route) },
                    onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
                    onNavigateToAddCustomer = { navController.navigate(Screen.AddEditCustomer.createRoute(null)) },
                    onNavigateToPayments = { navController.navigate(Screen.LedgerPayments.route) },
                    onNavigateToReports = { navController.navigate(Screen.Reports.route) }
                )
            }

            composable(Screen.DailyRegister.route) {
                DailyRegisterScreen(viewModel = viewModel)
            }

            composable(Screen.Customers.route) {
                CustomersScreen(
                    viewModel = viewModel,
                    onNavigateToAddCustomer = { customerId ->
                        navController.navigate(Screen.AddEditCustomer.createRoute(customerId))
                    },
                    onNavigateToCustomerLedger = { customerId ->
                        navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                    }
                )
            }

            composable(Screen.LedgerPayments.route) {
                LedgerPaymentsScreen(
                    viewModel = viewModel,
                    onNavigateToCustomerLedger = { customerId ->
                        navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                    }
                )
            }

            composable(Screen.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel,
                    onNavigateToCustomerLedger = { customerId ->
                        navController.navigate(Screen.CustomerDetail.createRoute(customerId))
                    }
                )
            }

            composable(Screen.SettingsRole.route) {
                SettingsRoleScreen(viewModel = viewModel)
            }

            composable(
                route = Screen.CustomerDetail.route,
                arguments = listOf(navArgument("customerId") { type = NavType.LongType })
            ) { backStackEntry ->
                val customerId = backStackEntry.arguments?.getLong("customerId") ?: 0L
                CustomerDetailScreen(
                    customerId = customerId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToEdit = { id ->
                        navController.navigate(Screen.AddEditCustomer.createRoute(id))
                    }
                )
            }

            composable(
                route = Screen.AddEditCustomer.route,
                arguments = listOf(navArgument("customerId") {
                    type = NavType.LongType
                    defaultValue = -1L
                })
            ) { backStackEntry ->
                val idArg = backStackEntry.arguments?.getLong("customerId") ?: -1L
                val custId = if (idArg != -1L) idArg else null
                AddEditCustomerScreen(
                    customerId = custId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
