package com.example.data.database

import androidx.room.TypeConverter
import com.example.data.model.DeliveryStatus
import com.example.data.model.PaymentMode
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromDeliveryStatus(status: DeliveryStatus): String = status.name

    @TypeConverter
    fun toDeliveryStatus(value: String): DeliveryStatus {
        return try {
            DeliveryStatus.valueOf(value)
        } catch (e: Exception) {
            DeliveryStatus.DELIVERED
        }
    }

    @TypeConverter
    fun fromPaymentMode(mode: PaymentMode): String = mode.name

    @TypeConverter
    fun toPaymentMode(value: String): PaymentMode {
        return try {
            PaymentMode.valueOf(value)
        } catch (e: Exception) {
            PaymentMode.CASH
        }
    }

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return try {
            UserRole.valueOf(value)
        } catch (e: Exception) {
            UserRole.ADMIN
        }
    }
}
