package com.example.gunplaradar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "stock_delay_records")
data class StockDelayRecordEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val storeId: String,
    val itemId: String,
    val restockDate: Long,
    val actualStockDate: Long,
    val delayHours: Double
)
