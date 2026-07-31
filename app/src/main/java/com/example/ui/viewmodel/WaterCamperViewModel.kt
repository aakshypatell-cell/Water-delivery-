package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.WaterCamperDatabase
import com.example.data.model.AppSetting
import com.example.data.model.Customer
import com.example.data.model.DeliveryRecord
import com.example.data.model.DeliveryStatus
import com.example.data.model.Payment
import com.example.data.model.PaymentMode
import com.example.data.model.UserRole
import com.example.data.repository.WaterCamperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CustomerLedgerSummary(
    val customer: Customer,
    val totalCansDelivered: Int,
    val deliveredDaysCount: Int,
    val skippedDaysCount: Int,
    val totalBilledAmount: Double,
    val totalPaidAmount: Double,
    val pendingBalance: Double,
    val recentDeliveries: List<DeliveryRecord>,
    val recentPayments: List<Payment>
)

data class DashboardUiState(
    val totalCustomers: Int = 0,
    val todayDeliveredCount: Int = 0,
    val todayTotalCans: Int = 0,
    val todayPendingCount: Int = 0,
    val todaySkippedCount: Int = 0,
    val totalPendingBalance: Double = 0.0,
    val monthRevenue: Double = 0.0,
    val currentRole: UserRole = UserRole.ADMIN,
    val businessName: String = "Aqua Pure Water Services"
)

class WaterCamperViewModel(application: Application) : AndroidViewModel(application) {

    private val db = WaterCamperDatabase.getDatabase(application)
    val repository = WaterCamperRepository(
        db.customerDao(),
        db.deliveryDao(),
        db.paymentDao(),
        db.settingDao()
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val todayDateStr: String = dateFormat.format(Date())
    val currentMonthStr: String = monthFormat.format(Date())

    // UI state flows
    val selectedRegisterDate = MutableStateFlow(todayDateStr)
    val selectedAreaFilter = MutableStateFlow("All")
    val registerSearchQuery = MutableStateFlow("")

    val selectedReportMonth = MutableStateFlow(currentMonthStr)
    val selectedCustomerForDetail = MutableStateFlow<Customer?>(null)

    // App settings and active customers
    val appSettings: StateFlow<AppSetting?> = repository.appSettings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppSetting()
    )

    val activeCustomers: StateFlow<List<Customer>> = repository.activeCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allCustomers: StateFlow<List<Customer>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAreas: StateFlow<List<String>> = repository.allAreas.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allPayments: StateFlow<List<Payment>> = repository.allPayments.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Deliveries for selected register date
    val registerDeliveries: StateFlow<List<DeliveryRecord>> = selectedRegisterDate.combine(
        repository.allCustomers
    ) { date, _ ->
        date
    }.combine(repository.getDeliveriesForDate(selectedRegisterDate.value)) { _, deliveries ->
        deliveries
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combine dashboard state
    val dashboardState: StateFlow<DashboardUiState> = combine(
        activeCustomers,
        repository.getDeliveriesForDate(todayDateStr),
        repository.getAllDeliveriesForMonth(currentMonthStr),
        repository.getPaymentsForMonth(currentMonthStr),
        appSettings
    ) { customers, todayDeliveries, monthDeliveries, monthPayments, settings ->
        val deliveredToday = todayDeliveries.filter { it.status == DeliveryStatus.DELIVERED }
        val skippedToday = todayDeliveries.filter { it.status == DeliveryStatus.SKIP || it.status == DeliveryStatus.HOLIDAY }
        
        val todayDeliveredCustomerIds = todayDeliveries.map { it.customerId }.toSet()
        val pendingTodayCount = customers.size - todayDeliveredCustomerIds.size

        val totalDeliveredCansToday = deliveredToday.sumOf { it.quantityDelivered }

        // Billed total for month
        val totalBilledMonth = monthDeliveries
            .filter { it.status == DeliveryStatus.DELIVERED }
            .sumOf { it.quantityDelivered * it.priceAtTime }

        val totalPaidMonth = monthPayments.sumOf { it.amount }

        // Rough calculation for all pending balances across customers
        var grandPending = 0.0
        customers.forEach { cust ->
            // In full calculation, we sum up billed minus paid
            // We'll compute precise per-customer summary
        }

        DashboardUiState(
            totalCustomers = customers.size,
            todayDeliveredCount = deliveredToday.size,
            todayTotalCans = totalDeliveredCansToday,
            todayPendingCount = pendingTodayCount.coerceAtLeast(0),
            todaySkippedCount = skippedToday.size,
            totalPendingBalance = (totalBilledMonth - totalPaidMonth).coerceAtLeast(0.0),
            monthRevenue = totalPaidMonth,
            currentRole = settings?.currentRole ?: UserRole.ADMIN,
            businessName = settings?.businessName ?: "Aqua Pure Water Services"
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        viewModelScope.launch {
            repository.initializeSampleDataIfNeeded()
        }
    }

    // Quick status mark for a customer on selected date
    fun markDeliveryStatus(
        customerId: Long,
        status: DeliveryStatus,
        quantity: Int,
        priceAtTime: Double,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.markDeliveryStatus(
                customerId = customerId,
                date = selectedRegisterDate.value,
                status = status,
                quantity = quantity,
                priceAtTime = priceAtTime,
                notes = notes
            )
        }
    }

    // Quick mark all pending active customers for selected date as DELIVERED
    fun markAllPendingAsDelivered() {
        viewModelScope.launch {
            val date = selectedRegisterDate.value
            val currentDeliveriesMap = repository.getDeliveriesForDate(date).first().associateBy { it.customerId }
            val customers = repository.activeCustomers.first()

            customers.forEach { cust ->
                if (!currentDeliveriesMap.containsKey(cust.id)) {
                    repository.markDeliveryStatus(
                        customerId = cust.id,
                        date = date,
                        status = DeliveryStatus.DELIVERED,
                        quantity = cust.dailyQuantity,
                        priceAtTime = cust.pricePerCamper,
                        notes = "Auto-marked"
                    )
                }
            }
        }
    }

    // Save/Update Customer
    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            if (customer.id == 0L) {
                repository.insertCustomer(customer)
            } else {
                repository.updateCustomer(customer)
            }
        }
    }

    // Delete Customer
    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Record Payment
    fun recordPayment(
        customerId: Long,
        amount: Double,
        date: String,
        mode: PaymentMode,
        refNo: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.recordPayment(
                customerId = customerId,
                amount = amount,
                date = date,
                mode = mode,
                referenceNo = refNo,
                notes = notes
            )
        }
    }

    // Role Switcher
    fun setRole(role: UserRole) {
        viewModelScope.launch {
            repository.updateRole(role)
        }
    }

    // Save Settings
    fun saveBusinessSettings(name: String, phone: String, upi: String) {
        viewModelScope.launch {
            val current = repository.appSettings.first() ?: AppSetting()
            repository.saveSettings(
                current.copy(
                    businessName = name,
                    ownerPhone = phone,
                    upiId = upi
                )
            )
        }
    }
}
