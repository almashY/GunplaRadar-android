package com.example.gunplaradar.ui.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gunplaradar.data.entity.StockDelayRecordEntity
import com.example.gunplaradar.data.entity.StoreEntity
import com.example.gunplaradar.data.repository.GunplaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class StoreUiState(
    val stores: List<StoreEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedStore: StoreEntity? = null,
    val storeRecords: List<StockDelayRecordEntity> = emptyList()
)

class StoreViewModel(private val repository: GunplaRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedStoreId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StoreUiState> = combine(
        repository.allStores,
        _searchQuery
    ) { stores, query ->
        val filtered = if (query.isBlank()) stores
        else stores.filter { it.name.contains(query, ignoreCase = true) }
        StoreUiState(stores = filtered, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StoreUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun insertStore(store: StoreEntity) {
        viewModelScope.launch {
            repository.insertStore(store)
        }
    }

    fun updateStore(store: StoreEntity) {
        viewModelScope.launch {
            repository.updateStore(store)
        }
    }

    fun toggleFavorite(store: StoreEntity) {
        viewModelScope.launch {
            repository.updateStore(store.copy(isFavorite = !store.isFavorite))
        }
    }

    fun deleteStore(store: StoreEntity) {
        viewModelScope.launch {
            repository.deleteStore(store)
        }
    }

    fun getRecordsByStore(storeId: String): Flow<List<StockDelayRecordEntity>> =
        repository.getRecordsByStore(storeId)

    class Factory(private val repository: GunplaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StoreViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
