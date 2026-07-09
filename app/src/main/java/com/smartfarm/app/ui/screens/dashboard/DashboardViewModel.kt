package com.smartfarm.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.DashboardSummary
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val summary: DashboardSummary? = null,
    val error: String? = null,
)

class DashboardViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchDashboardSummary()) {
                is ApiResult.Success -> _uiState.value = DashboardUiState(isLoading = false, summary = result.data)
                is ApiResult.Error -> _uiState.value = DashboardUiState(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DashboardViewModel(container) as T
        }
    }
}
