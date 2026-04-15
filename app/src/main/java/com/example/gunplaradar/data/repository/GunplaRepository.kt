package com.example.gunplaradar.data.repository

import com.example.gunplaradar.data.dao.GunplaItemDao
import com.example.gunplaradar.data.dao.PatrolPlanDao
import com.example.gunplaradar.data.dao.StockDelayRecordDao
import com.example.gunplaradar.data.dao.StoreDao
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import com.example.gunplaradar.data.entity.StockDelayRecordEntity
import com.example.gunplaradar.data.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

class GunplaRepository(
    private val gunplaItemDao: GunplaItemDao,
    private val storeDao: StoreDao,
    private val stockDelayRecordDao: StockDelayRecordDao,
    private val patrolPlanDao: PatrolPlanDao
) {
    // GunplaItem
    val allItems: Flow<List<GunplaItemEntity>> = gunplaItemDao.getAllItems()
    val unpurchasedItems: Flow<List<GunplaItemEntity>> = gunplaItemDao.getUnpurchasedItems()
    val itemsWithRestockDate: Flow<List<GunplaItemEntity>> = gunplaItemDao.getItemsWithRestockDate()

    suspend fun getItemById(id: String): GunplaItemEntity? = gunplaItemDao.getItemById(id)
    suspend fun insertItem(item: GunplaItemEntity) = gunplaItemDao.insertItem(item)
    suspend fun updateItem(item: GunplaItemEntity) = gunplaItemDao.updateItem(item)
    suspend fun deleteItem(item: GunplaItemEntity) = gunplaItemDao.deleteItem(item)
    suspend fun deleteAllItems() = gunplaItemDao.deleteAllItems()

    // Store
    val allStores: Flow<List<StoreEntity>> = storeDao.getAllStores()
    val favoriteStores: Flow<List<StoreEntity>> = storeDao.getFavoriteStores()

    suspend fun getStoreById(id: String): StoreEntity? = storeDao.getStoreById(id)
    suspend fun insertStore(store: StoreEntity) = storeDao.insertStore(store)
    suspend fun updateStore(store: StoreEntity) = storeDao.updateStore(store)
    suspend fun deleteStore(store: StoreEntity) = storeDao.deleteStore(store)
    suspend fun deleteAllStores() = storeDao.deleteAllStores()

    // StockDelayRecord
    val allRecords: Flow<List<StockDelayRecordEntity>> = stockDelayRecordDao.getAllRecords()

    fun getRecordsByStore(storeId: String): Flow<List<StockDelayRecordEntity>> =
        stockDelayRecordDao.getRecordsByStore(storeId)

    suspend fun getAverageDelayHours(storeId: String): Double? =
        stockDelayRecordDao.getAverageDelayHours(storeId)

    suspend fun insertRecord(record: StockDelayRecordEntity) =
        stockDelayRecordDao.insertRecord(record)

    suspend fun deleteRecord(record: StockDelayRecordEntity) =
        stockDelayRecordDao.deleteRecord(record)

    suspend fun deleteAllRecords() = stockDelayRecordDao.deleteAllRecords()

    // PatrolPlan
    val allPlans: Flow<List<PatrolPlanEntity>> = patrolPlanDao.getAllPlans()

    suspend fun getPlanById(id: String): PatrolPlanEntity? = patrolPlanDao.getPlanById(id)
    fun getPlansByDate(date: Long): Flow<List<PatrolPlanEntity>> = patrolPlanDao.getPlansByDate(date)
    suspend fun insertPlan(plan: PatrolPlanEntity) = patrolPlanDao.insertPlan(plan)
    suspend fun updatePlan(plan: PatrolPlanEntity) = patrolPlanDao.updatePlan(plan)
    suspend fun deletePlan(plan: PatrolPlanEntity) = patrolPlanDao.deletePlan(plan)
    suspend fun deleteAllPlans() = patrolPlanDao.deleteAllPlans()

    // Delete all data
    suspend fun deleteAllData() {
        deleteAllItems()
        deleteAllStores()
        deleteAllRecords()
        deleteAllPlans()
    }
}
