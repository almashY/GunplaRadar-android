package com.example.gunplaradar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "patrol_plans")
data class PatrolPlanEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val date: Long,
    val time: Long,
    val storeId: String,
    val targetItemIds: String = "",
    val notifyEnabled: Boolean = true
)
