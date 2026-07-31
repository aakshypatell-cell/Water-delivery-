package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY date DESC, timestamp DESC")
    fun getPaymentsForCustomer(customerId: Long): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE date LIKE :monthPrefix || '%' ORDER BY date DESC")
    fun getPaymentsForMonth(monthPrefix: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments ORDER BY date DESC, timestamp DESC")
    fun getAllPayments(): Flow<List<Payment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment): Long

    @Query("DELETE FROM payments WHERE id = :paymentId")
    suspend fun deletePayment(paymentId: Long)
}
