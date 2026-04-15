package com.example.gunplaradar.ui.patrol

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import com.example.gunplaradar.data.entity.StoreEntity
import com.example.gunplaradar.data.repository.GunplaRepository
import com.example.gunplaradar.worker.PatrolNotificationWorker
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

data class PatrolUiState(
    val plans: List<PatrolPlanEntity> = emptyList(),
    val stores: List<StoreEntity> = emptyList(),
    val items: List<GunplaItemEntity> = emptyList()
)

class PatrolViewModel(
    private val repository: GunplaRepository,
    private val context: Context
) : ViewModel() {

    val uiState: StateFlow<PatrolUiState> = combine(
        repository.allPlans,
        repository.allStores,
        repository.unpurchasedItems
    ) { plans, stores, items ->
        PatrolUiState(plans = plans, stores = stores, items = items)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PatrolUiState()
    )

    fun insertPlan(plan: PatrolPlanEntity) {
        viewModelScope.launch {
            repository.insertPlan(plan)
            if (plan.notifyEnabled) {
                scheduleNotification(plan)
            }
        }
    }

    fun updatePlan(plan: PatrolPlanEntity) {
        viewModelScope.launch {
            repository.updatePlan(plan)
        }
    }

    fun deletePlan(plan: PatrolPlanEntity) {
        viewModelScope.launch {
            repository.deletePlan(plan)
        }
    }

    suspend fun getPlanById(id: String): PatrolPlanEntity? = repository.getPlanById(id)

    private fun scheduleNotification(plan: PatrolPlanEntity) {
        val store = uiState.value.stores.find { it.id == plan.storeId }
        val storeName = store?.name ?: "店舗"
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN)
        val cal = Calendar.getInstance().apply {
            timeInMillis = plan.date
            val timeCal = Calendar.getInstance().apply { timeInMillis = plan.time }
            set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE))
        }
        val planDateStr = dateFormat.format(cal.time)
        val delay = cal.timeInMillis - System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)

        if (delay > 0) {
            val inputData = Data.Builder()
                .putString(PatrolNotificationWorker.KEY_STORE_NAME, storeName)
                .putString(PatrolNotificationWorker.KEY_PLAN_DATE, planDateStr)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<PatrolNotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("patrol_${plan.id}")
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    class Factory(
        private val repository: GunplaRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PatrolViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PatrolViewModel(repository, context) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
