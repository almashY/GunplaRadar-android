package com.example.gunplaradar.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gunplaradar.data.dao.GunplaItemDao
import com.example.gunplaradar.data.dao.PatrolPlanDao
import com.example.gunplaradar.data.dao.StockDelayRecordDao
import com.example.gunplaradar.data.dao.StoreDao
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import com.example.gunplaradar.data.entity.StockDelayRecordEntity
import com.example.gunplaradar.data.entity.StoreEntity

@Database(
    entities = [
        GunplaItemEntity::class,
        StoreEntity::class,
        StockDelayRecordEntity::class,
        PatrolPlanEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gunplaItemDao(): GunplaItemDao
    abstract fun storeDao(): StoreDao
    abstract fun stockDelayRecordDao(): StockDelayRecordDao
    abstract fun patrolPlanDao(): PatrolPlanDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gunpla_radar_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
