package com.example.gunplaradar.data.dao

import androidx.room.*
import com.example.gunplaradar.data.entity.StockDelayRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StockDelayRecordDao {
    @Query("SELECT * FROM stock_delay_records ORDER BY actualStockDate DESC")
    fun getAllRecords(): Flow<List<StockDelayRecordEntity>>

    @Query("SELECT * FROM stock_delay_records WHERE storeId = :storeId ORDER BY actualStockDate DESC")
    fun getRecordsByStore(storeId: String): Flow<List<StockDelayRecordEntity>>

    @Query("SELECT AVG(delayHours) FROM stock_delay_records WHERE storeId = :storeId")
    suspend fun getAverageDelayHours(storeId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: StockDelayRecordEntity)

    @Delete
    suspend fun deleteRecord(record: StockDelayRecordEntity)

    @Query("DELETE FROM stock_delay_records")
    suspend fun deleteAllRecords()
}
