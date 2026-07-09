package com.smartfarm.app.ui.screens.support

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
import com.smartfarm.app.data.model.SupportProgram
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmAmber50
import com.smartfarm.app.ui.theme.FarmGreen50
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SupportUiState(val isLoading: Boolean = true, val items: List<SupportProgram> = emptyList(), val error: String? = null)

class SupportViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchSupportPrograms()) {
                is ApiResult.Success -> _uiState.value = SupportUiState(isLoading = false, items = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SupportViewModel(container) as T
        }
    }
}

@Composable
fun SupportScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: SupportViewModel = viewModel(factory = SupportViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "정부 지원사업", subtitle = "농업인을 위한 정부 지원 사업 정보")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items) { p ->
                    SectionCard {
                        Row {
                            Text(p.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            StatusChip(p.status, if (p.status.contains("모집")) FarmGreen50 else FarmAmber50)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("주관: ${p.agency}", style = MaterialTheme.typography.bodyMedium)
                        Text("신청 기간: ${p.period}", style = MaterialTheme.typography.bodyMedium)
                        Text("대상: ${p.target}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(p.content, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
