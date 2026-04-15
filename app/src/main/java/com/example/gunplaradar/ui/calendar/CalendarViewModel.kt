package com.example.gunplaradar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.entity.PatrolPlanEntity
import com.example.gunplaradar.data.entity.StockDelayRecordEntity
import com.example.gunplaradar.data.repository.GunplaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CalendarUiState(
    val itemsWithRestock: List<GunplaItemEntity> = emptyList(),
    val patrolPlans: List<PatrolPlanEntity> = emptyList(),
    val currentYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentMonth: Int = Calendar.getInstance().get(Calendar.MONTH)
)

class CalendarViewModel(private val repository: GunplaRepository) : ViewModel() {

    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))

    val uiState: StateFlow<CalendarUiState> = combine(
        repository.itemsWithRestockDate,
        repository.allPlans,
        _currentYear,
        _currentMonth
    ) { items, plans, year, month ->
        CalendarUiState(
            itemsWithRestock = items,
            patrolPlans = plans,
            currentYear = year,
            currentMonth = month
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            set(_currentYear.value, _currentMonth.value, 1)
            add(Calendar.MONTH, -1)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            set(_currentYear.value, _currentMonth.value, 1)
            add(Calendar.MONTH, 1)
        }
        _currentYear.value = cal.get(Calendar.YEAR)
        _currentMonth.value = cal.get(Calendar.MONTH)
    }

    fun insertStockDelayRecord(record: StockDelayRecordEntity) {
        viewModelScope.launch {
            repository.insertRecord(record)
            // 店舗の平均遅延時間を更新
            val avg = repository.getAverageDelayHours(record.storeId)
            if (avg != null) {
                val store = repository.getStoreById(record.storeId)
                store?.let {
                    repository.updateStore(it.copy(averageDelayHours = avg))
                }
            }
        }
    }

    class Factory(private val repository: GunplaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return CalendarViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
