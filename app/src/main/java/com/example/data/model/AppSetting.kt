package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN, // Full owner access (financials, customer edits, pricing, payments)
    STAFF  // Delivery staff access (marking daily register, quick view)
}

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val id: Int = 1,
    val currentRole: UserRole = UserRole.ADMIN,
    val businessName: String = "Aqua Pure Water Services",
    val ownerPhone: String = "+91 98765 43210",
    val upiId: String = "aquapure@upi",
    val defaultPrice: Double = 30.0,
    val autoBackupEnabled: Boolean = true
)
