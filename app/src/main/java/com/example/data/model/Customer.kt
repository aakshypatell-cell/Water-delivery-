package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String,
    val address: String,
    val area: String, // Area / Route e.g. "North Zone", "Commercial Market", "Sector 14"
    val dailyQuantity: Int = 1, // Default daily campers
    val camperSize: String = "20L", // "20L", "10L", "25L"
    val pricePerCamper: Double = 30.0, // e.g. ₹30 per camper
    val isActive: Boolean = true,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
