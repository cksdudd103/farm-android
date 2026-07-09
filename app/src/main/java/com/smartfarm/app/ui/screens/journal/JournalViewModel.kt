package com.smartfarm.app.ui.screens.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.Journal
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class JournalUiState(
    val isLoading: Boolean = true,
    val journals: List<Journal> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
)

class JournalViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchJournals()) {
                is ApiResult.Success -> _uiState.value = JournalUiState(isLoading = false, journals = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createJournal(
        cropId: Int?, date: String, workType: String, weather: String, content: String,
        imageFile: File?, onDone: (Boolean, String?) -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = container.farmRepository.createJournal(cropId, date, workType, weather, content, imageFile)) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isSaving = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isSaving = false); onDone(false, result.message) }
            }
        }
    }

    fun deleteJournal(id: Int) {
        viewModelScope.launch { container.farmRepository.deleteJournal(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = JournalViewModel(container) as T
        }
    }
}
