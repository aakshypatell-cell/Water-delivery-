package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.CustomerDao
import com.example.data.dao.DeliveryDao
import com.example.data.dao.PaymentDao
import com.example.data.dao.SettingDao
import com.example.data.model.AppSetting
import com.example.data.model.Customer
import com.example.data.model.DeliveryRecord
import com.example.data.model.Payment

@Database(
    entities = [
        Customer::class,
        DeliveryRecord::class,
        Payment::class,
        AppSetting::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WaterCamperDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun deliveryDao(): DeliveryDao
    abstract fun paymentDao(): PaymentDao
    abstract fun settingDao(): SettingDao

    companion object {
        @Volatile
        private var INSTANCE: WaterCamperDatabase? = null

        fun getDatabase(context: Context): WaterCamperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WaterCamperDatabase::class.java,
                    "water_camper_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
