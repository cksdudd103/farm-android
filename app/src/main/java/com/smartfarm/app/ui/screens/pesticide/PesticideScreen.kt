package com.smartfarm.app.ui.screens.pesticide

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.smartfarm.app.data.model.PesticideInfo
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PesticideUiState(val isLoading: Boolean = true, val items: List<PesticideInfo> = emptyList(), val error: String? = null)

class PesticideViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(PesticideUiState())
    val uiState: StateFlow<PesticideUiState> = _uiState.asStateFlow()

    fun search(query: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchPesticides(query)) {
                is ApiResult.Success -> _uiState.value = PesticideUiState(isLoading = false, items = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PesticideViewModel(container) as T
        }
    }
}

@Composable
fun PesticideScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: PesticideViewModel = viewModel(factory = PesticideViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.search() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "농약 정보", subtitle = "작물별 농약 안전 사용 정보를 확인하세요")
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("작물명 또는 병해충명 검색") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            trailingIcon = {
                TextButton(onClick = { viewModel.search(query) }) { Text("검색") }
            },
        )
        Spacer(Modifier.height(8.dp))
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.search(query) })
            state.items.isEmpty() -> EmptyState("검색 결과가 없습니다.")
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.items) { p ->
                    SectionCard {
                        Row {
                            Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.width(8.dp))
                            StatusChip(p.type, FarmGreen40)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("적용 대상: ${p.target}", style = MaterialTheme.typography.bodyMedium)
                        Text("적용 작물: ${p.crops}", style = MaterialTheme.typography.bodyMedium)
                        Text("안전 사용 기준: ${p.safetyPeriod}", style = MaterialTheme.typography.bodyMedium)
                        Text("희석 배수: ${p.dilution}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
