package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class DeliveryStatus {
    DELIVERED,
    NOT_DELIVERED,
    HOLIDAY,
    SKIP
}

@Entity(
    tableName = "delivery_records",
    indices = [
        Index(value = ["customerId", "date"], unique = true)
    ]
)
data class DeliveryRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val date: String, // Format: "YYYY-MM-DD"
    val status: DeliveryStatus = DeliveryStatus.DELIVERED,
    val quantityDelivered: Int = 1,
    val priceAtTime: Double = 30.0,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
