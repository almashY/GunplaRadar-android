package com.example.gunplaradar.data.dao

import androidx.room.*
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatrolPlanDao {
    @Query("SELECT * FROM patrol_plans ORDER BY date ASC, time ASC")
    fun getAllPlans(): Flow<List<PatrolPlanEntity>>

    @Query("SELECT * FROM patrol_plans WHERE id = :id")
    suspend fun getPlanById(id: String): PatrolPlanEntity?

    @Query("SELECT * FROM patrol_plans WHERE date = :date ORDER BY time ASC")
    fun getPlansByDate(date: Long): Flow<List<PatrolPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: PatrolPlanEntity)

    @Update
    suspend fun updatePlan(plan: PatrolPlanEntity)

    @Delete
    suspend fun deletePlan(plan: PatrolPlanEntity)

    @Query("DELETE FROM patrol_plans")
    suspend fun deleteAllPlans()
}
