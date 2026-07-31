package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DeliveryRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DeliveryDao {
    @Query("SELECT * FROM delivery_records WHERE date = :date")
    fun getDeliveriesForDate(date: String): Flow<List<DeliveryRecord>>

    @Query("SELECT * FROM delivery_records WHERE customerId = :customerId ORDER BY date DESC")
    fun getDeliveriesForCustomer(customerId: Long): Flow<List<DeliveryRecord>>

    @Query("SELECT * FROM delivery_records WHERE customerId = :customerId AND date LIKE :monthPrefix || '%' ORDER BY date ASC")
    fun getDeliveriesForCustomerMonth(customerId: Long, monthPrefix: String): Flow<List<DeliveryRecord>>

    @Query("SELECT * FROM delivery_records WHERE date LIKE :monthPrefix || '%'")
    fun getAllDeliveriesForMonth(monthPrefix: String): Flow<List<DeliveryRecord>>

    @Query("SELECT * FROM delivery_records WHERE customerId = :customerId AND date = :date")
    suspend fun getDeliveryForCustomerAndDate(customerId: Long, date: String): DeliveryRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDelivery(record: DeliveryRecord): Long

    @Query("DELETE FROM delivery_records WHERE customerId = :customerId AND date = :date")
    suspend fun deleteDelivery(customerId: Long, date: String)
}
