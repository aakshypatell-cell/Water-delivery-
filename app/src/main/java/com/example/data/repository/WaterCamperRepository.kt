package com.example.data.repository

import com.example.data.dao.CustomerDao
import com.example.data.dao.DeliveryDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.SettingDao
import com.example.data.model.AppSetting
import com.example.data.model.Customer
import com.example.data.model.DeliveryRecord
import com.example.data.model.DeliveryStatus
import com.example.data.model.Payment
import com.example.data.model.PaymentMode
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WaterCamperRepository(
    private val customerDao: CustomerDao,
    private val deliveryDao: DeliveryDao,
    private val paymentDao: PaymentDao,
    private val settingDao: SettingDao
) {
    val allCustomers: Flow<List<Customer>> = customerDao.getAllCustomers()
    val activeCustomers: Flow<List<Customer>> = customerDao.getActiveCustomers()
    val allAreas: Flow<List<String>> = customerDao.getAllAreas()
    val appSettings: Flow<AppSetting?> = settingDao.getSettings()

    fun getDeliveriesForDate(date: String): Flow<List<DeliveryRecord>> =
        deliveryDao.getDeliveriesForDate(date)

    fun getDeliveriesForCustomer(customerId: Long): Flow<List<DeliveryRecord>> =
        deliveryDao.getDeliveriesForCustomer(customerId)

    fun getDeliveriesForCustomerMonth(customerId: Long, monthPrefix: String): Flow<List<DeliveryRecord>> =
        deliveryDao.getDeliveriesForCustomerMonth(customerId, monthPrefix)

    fun getAllDeliveriesForMonth(monthPrefix: String): Flow<List<DeliveryRecord>> =
        deliveryDao.getAllDeliveriesForMonth(monthPrefix)

    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>> =
        paymentDao.getPaymentsForCustomer(customerId)

    fun getPaymentsForMonth(monthPrefix: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForMonth(monthPrefix)

    val allPayments: Flow<List<Payment>> = paymentDao.getAllPayments()

    suspend fun insertCustomer(customer: Customer): Long =
        customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) =
        customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) =
        customerDao.deleteCustomer(customer)

    suspend fun markDeliveryStatus(
        customerId: Long,
        date: String,
        status: DeliveryStatus,
        quantity: Int,
        priceAtTime: Double,
        notes: String = ""
    ) {
        val record = DeliveryRecord(
            customerId = customerId,
            date = date,
            status = status,
            quantityDelivered = if (status == DeliveryStatus.DELIVERED) quantity else 0,
            priceAtTime = priceAtTime,
            notes = notes
        )
        deliveryDao.insertOrUpdateDelivery(record)
    }

    suspend fun recordPayment(
        customerId: Long,
        amount: Double,
        date: String,
        mode: PaymentMode,
        referenceNo: String,
        notes: String
    ): Long {
        val payment = Payment(
            customerId = customerId,
            date = date,
            amount = amount,
            paymentMode = mode,
            referenceNo = referenceNo,
            notes = notes
        )
        return paymentDao.insertPayment(payment)
    }

    suspend fun saveSettings(setting: AppSetting) =
        settingDao.saveSettings(setting)

    suspend fun updateRole(newRole: UserRole) {
        val current = settingDao.getSettings().first() ?: AppSetting()
        settingDao.saveSettings(current.copy(currentRole = newRole))
    }

    // Pre-populates realistic sample data on first launch
    suspend fun initializeSampleDataIfNeeded() {
        val customers = customerDao.getAllCustomers().first()
        if (customers.isEmpty()) {
            val sampleCustomers = listOf(
                Customer(name = "Ramesh Sharma", phone = "+91 98230 11223", address = "House 42, Main St", area = "Sector 14", dailyQuantity = 2, camperSize = "20L", pricePerCamper = 30.0, isActive = true, notes = "Morning delivery before 9 AM"),
                Customer(name = "Sunshine Cafe", phone = "+91 98112 33445", address = "Shop 12, High Market", area = "Commercial Market", dailyQuantity = 5, camperSize = "20L", pricePerCamper = 35.0, isActive = true, notes = "Need chilled campers"),
                Customer(name = "Tech Park Office", phone = "+91 99887 66554", address = "Block C, Floor 3", area = "IT Zone", dailyQuantity = 10, camperSize = "25L", pricePerCamper = 40.0, isActive = true, notes = "Deliver on weekdays only"),
                Customer(name = "Suresh Kumar", phone = "+91 97654 32109", address = "Flat 302, Royal Heights", area = "North Route", dailyQuantity = 1, camperSize = "20L", pricePerCamper = 30.0, isActive = true, notes = "Leave at gate if door locked"),
                Customer(name = "City Clinic", phone = "+91 98900 12345", address = "Opposite Govt Hospital", area = "Civil Line", dailyQuantity = 3, camperSize = "20L", pricePerCamper = 35.0, isActive = true, notes = "Sanitized campers required"),
                Customer(name = "Green Valley School", phone = "+91 95432 10987", address = "Ring Road, Gate 2", area = "South Route", dailyQuantity = 8, camperSize = "25L", pricePerCamper = 38.0, isActive = true, notes = "Deliver during school hours"),
                Customer(name = "Fit Life Gym", phone = "+91 98765 00112", address = "2nd Floor, Grand Mall", area = "Commercial Market", dailyQuantity = 4, camperSize = "20L", pricePerCamper = 35.0, isActive = true, notes = "Call before delivery"),
                Customer(name = "Sunil Agarwal", phone = "+91 91234 56789", address = "House 108, Street 4", area = "Sector 14", dailyQuantity = 2, camperSize = "20L", pricePerCamper = 30.0, isActive = true, notes = "Monthly UPI payment")
            )

            val insertedIds = mutableListOf<Long>()
            for (cust in sampleCustomers) {
                val id = customerDao.insertCustomer(cust)
                insertedIds.add(id)
            }

            // Create initial settings
            settingDao.saveSettings(AppSetting(
                id = 1,
                currentRole = UserRole.ADMIN,
                businessName = "Aqua Pure Water Services",
                ownerPhone = "+91 98765 43210",
                upiId = "aquapure@upi"
            ))

            // Populate sample delivery history for past 10 days of current month
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            for (dayOffset in 0..10) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, -dayOffset)
                val dateStr = dateFormat.format(cal.time)

                insertedIds.forEachIndexed { index, custId ->
                    val cust = sampleCustomers[index]
                    val status = when {
                        dayOffset == 0 -> if (index % 3 == 0) DeliveryStatus.DELIVERED else if (index % 3 == 1) DeliveryStatus.SKIP else DeliveryStatus.DELIVERED
                        (dayOffset + index) % 7 == 0 -> DeliveryStatus.HOLIDAY
                        (dayOffset + index) % 5 == 0 -> DeliveryStatus.SKIP
                        else -> DeliveryStatus.DELIVERED
                    }

                    val qty = if (status == DeliveryStatus.DELIVERED) cust.dailyQuantity else 0
                    deliveryDao.insertOrUpdateDelivery(DeliveryRecord(
                        customerId = custId,
                        date = dateStr,
                        status = status,
                        quantityDelivered = qty,
                        priceAtTime = cust.pricePerCamper,
                        notes = if (status == DeliveryStatus.SKIP) "Customer on leave" else ""
                    ))
                }
            }

            // Populate sample payment logs
            val currentMonthStr = dateFormat.format(Date()).substring(0, 7)
            if (insertedIds.isNotEmpty()) {
                paymentDao.insertPayment(Payment(customerId = insertedIds[0], date = "$currentMonthStr-05", amount = 1200.0, paymentMode = PaymentMode.UPI, referenceNo = "UPI982301923", notes = "Partial payment"))
                paymentDao.insertPayment(Payment(customerId = insertedIds[1], date = "$currentMonthStr-02", amount = 3000.0, paymentMode = PaymentMode.CASH, referenceNo = "", notes = "Cash received by driver"))
                paymentDao.insertPayment(Payment(customerId = insertedIds[2], date = "$currentMonthStr-08", amount = 5000.0, paymentMode = PaymentMode.BANK_TRANSFER, referenceNo = "NEFT-889102", notes = "Direct transfer"))
            }
        }
    }
}
