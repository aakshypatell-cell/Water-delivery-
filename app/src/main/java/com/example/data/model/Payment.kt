package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PaymentMode {
    CASH,
    UPI,
    BANK_TRANSFER,
    OTHER
}

@Entity(tableName = "payments")
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val date: String, // Format: "YYYY-MM-DD"
    val amount: Double,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val referenceNo: String = "",
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
