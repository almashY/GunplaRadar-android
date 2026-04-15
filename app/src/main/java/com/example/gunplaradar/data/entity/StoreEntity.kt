package com.example.gunplaradar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false,
    val averageDelayHours: Double = 0.0
)
