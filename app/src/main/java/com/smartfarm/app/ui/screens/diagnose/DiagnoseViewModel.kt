package com.smartfarm.app.ui.screens.diagnose

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.Diagnosis
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class DiagnoseUiState(
    val isLoading: Boolean = true,
    val history: List<Diagnosis> = emptyList(),
    val error: String? = null,
    val isDiagnosing: Boolean = false,
    val diagnoseError: String? = null,
    val lastResult: Diagnosis? = null,
)

class DiagnoseViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(DiagnoseUiState())
    val uiState: StateFlow<DiagnoseUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchDiagnoses()) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, history = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun diagnose(cropName: String, imageFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDiagnosing = true, diagnoseError = null, lastResult = null)
            when (val result = container.farmRepository.createDiagnosis(cropName, imageFile)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isDiagnosing = false, lastResult = result.data)
                    load()
                }
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isDiagnosing = false, diagnoseError = result.message)
            }
        }
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(lastResult = null, diagnoseError = null)
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = DiagnoseViewModel(container) as T
        }
    }
}
