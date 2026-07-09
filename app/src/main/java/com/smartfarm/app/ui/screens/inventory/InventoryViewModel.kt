package com.smartfarm.app.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.InventoryItem
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isLoading: Boolean = true,
    val items: List<InventoryItem> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
)

class InventoryViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchInventory()) {
                is ApiResult.Success -> _uiState.value = InventoryUiState(isLoading = false, items = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createItem(name: String, category: String, quantity: Double, unit: String, location: String, expiryDate: String, memo: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = container.farmRepository.createInventory(name, category, quantity, unit, location, expiryDate, memo)) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isSaving = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isSaving = false); onDone(false, result.message) }
            }
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch { container.farmRepository.deleteInventory(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = InventoryViewModel(container) as T
        }
    }
}
