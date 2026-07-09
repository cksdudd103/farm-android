package com.smartfarm.app.ui.screens.rda

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.RdaNotice
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RdaUiState(val isLoading: Boolean = true, val items: List<RdaNotice> = emptyList(), val error: String? = null)

class RdaViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(RdaUiState())
    val uiState: StateFlow<RdaUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchRdaNotices()) {
                is ApiResult.Success -> _uiState.value = RdaUiState(isLoading = false, items = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = RdaViewModel(container) as T
        }
    }
}

@Composable
fun RdaScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: RdaViewModel = viewModel(factory = RdaViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "농업진흥청 새소식", subtitle = "최신 농업 기술 및 정책 소식")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            state.items.isEmpty() -> EmptyState("등록된 소식이 없습니다.")
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items) { notice ->
                    SectionCard {
                        Row {
                            StatusChip(notice.category, FarmGreen40)
                            Spacer(Modifier.width(8.dp))
                            Text(notice.noticeDate ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        if (!notice.content.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(notice.content, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
