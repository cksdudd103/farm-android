package com.smartfarm.app.ui.screens.crops

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.Crop
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class CropsUiState(
    val isLoading: Boolean = true,
    val crops: List<Crop> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
)

class CropsViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(CropsUiState())
    val uiState: StateFlow<CropsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchCrops()) {
                is ApiResult.Success -> _uiState.value = CropsUiState(isLoading = false, crops = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createCrop(
        name: String, variety: String, fieldLocation: String, area: String,
        plantingDate: String, expectedHarvestDate: String, status: String, memo: String,
        imageFile: File?, onDone: (Boolean, String?) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = container.farmRepository.createCrop(
                name, variety, fieldLocation, area, plantingDate, expectedHarvestDate, status, memo, imageFile
            )) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    load()
                    onDone(true, null)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onDone(false, result.message)
                }
            }
        }
    }

    fun updateCrop(
        id: Int, name: String, variety: String, fieldLocation: String, area: String,
        plantingDate: String, expectedHarvestDate: String, status: String, memo: String,
        imageFile: File?, onDone: (Boolean, String?) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = container.farmRepository.updateCrop(
                id, name, variety, fieldLocation, area, plantingDate, expectedHarvestDate, status, memo, imageFile
            )) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    load()
                    onDone(true, null)
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    onDone(false, result.message)
                }
            }
        }
    }

    fun deleteCrop(id: Int) {
        viewModelScope.launch {
            container.farmRepository.deleteCrop(id)
            load()
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CropsViewModel(container) as T
        }
    }
}
