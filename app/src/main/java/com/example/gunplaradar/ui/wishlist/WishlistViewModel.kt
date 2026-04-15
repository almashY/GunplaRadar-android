package com.example.gunplaradar.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.gunplaradar.data.entity.GunplaItemEntity
import com.example.gunplaradar.data.repository.GunplaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOrder {
    PRIORITY, NAME, PRICE_ASC, PRICE_DESC, RELEASE_DATE
}

data class WishlistUiState(
    val items: List<GunplaItemEntity> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.PRIORITY,
    val isLoading: Boolean = false
)

class WishlistViewModel(private val repository: GunplaRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.PRIORITY)

    val uiState: StateFlow<WishlistUiState> = combine(
        repository.unpurchasedItems,
        _searchQuery,
        _sortOrder
    ) { items, query, sort ->
        val filtered = if (query.isBlank()) items
        else items.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.grade.contains(query, ignoreCase = true)
        }
        val sorted = when (sort) {
            SortOrder.PRIORITY -> filtered.sortedBy { it.priority }
            SortOrder.NAME -> filtered.sortedBy { it.name }
            SortOrder.PRICE_ASC -> filtered.sortedBy { it.price ?: Int.MAX_VALUE }
            SortOrder.PRICE_DESC -> filtered.sortedByDescending { it.price ?: 0 }
            SortOrder.RELEASE_DATE -> filtered.sortedBy { it.releaseDate ?: Long.MAX_VALUE }
        }
        WishlistUiState(items = sorted, searchQuery = query, sortOrder = sort)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WishlistUiState()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: SortOrder) {
        _sortOrder.value = order
    }

    fun deleteItem(item: GunplaItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
        }
    }

    fun markAsPurchased(item: GunplaItemEntity, storeId: String? = null) {
        viewModelScope.launch {
            repository.updateItem(
                item.copy(
                    purchasedDate = System.currentTimeMillis(),
                    purchaseStoreId = storeId
                )
            )
        }
    }

    class Factory(private val repository: GunplaRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WishlistViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return WishlistViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
