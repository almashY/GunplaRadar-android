package com.example.gunplaradar.data.dao

import androidx.room.*
import com.example.gunplaradar.data.entity.GunplaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GunplaItemDao {
    @Query("SELECT * FROM gunpla_items ORDER BY priority ASC, name ASC")
    fun getAllItems(): Flow<List<GunplaItemEntity>>

    @Query("SELECT * FROM gunpla_items WHERE purchasedDate IS NULL ORDER BY priority ASC, name ASC")
    fun getUnpurchasedItems(): Flow<List<GunplaItemEntity>>

    @Query("SELECT * FROM gunpla_items WHERE id = :id")
    suspend fun getItemById(id: String): GunplaItemEntity?

    @Query("SELECT * FROM gunpla_items WHERE restockDate IS NOT NULL ORDER BY restockDate ASC")
    fun getItemsWithRestockDate(): Flow<List<GunplaItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: GunplaItemEntity)

    @Update
    suspend fun updateItem(item: GunplaItemEntity)

    @Delete
    suspend fun deleteItem(item: GunplaItemEntity)

    @Query("DELETE FROM gunpla_items")
    suspend fun deleteAllItems()
}
