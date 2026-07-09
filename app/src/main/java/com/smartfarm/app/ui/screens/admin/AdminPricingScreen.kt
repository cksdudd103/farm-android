package com.smartfarm.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.Plan
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmRed50
import com.smartfarm.app.ui.theme.White
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminPricingUiState(
    val isLoading: Boolean = true,
    val plans: List<Plan> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
)

class AdminPricingViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminPricingUiState())
    val uiState: StateFlow<AdminPricingUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchPlans()) {
                is ApiResult.Success -> _uiState.value = AdminPricingUiState(isLoading = false, plans = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createPlan(code: String, name: String, priceMonthly: Int, priceAnnual: Int, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val displayOrder = _uiState.value.plans.size
            when (val result = container.farmRepository.createPlan(code, name, priceMonthly, priceAnnual, true, displayOrder)) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isSaving = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isSaving = false); onDone(false, result.message) }
            }
        }
    }

    fun toggleActive(plan: Plan) {
        viewModelScope.launch { container.farmRepository.updatePlan(plan.id, isActive = !plan.isActive); load() }
    }

    fun deletePlan(id: Int) {
        viewModelScope.launch { container.farmRepository.deletePlan(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminPricingViewModel(container) as T
        }
    }
}

@Composable
fun AdminPricingScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: AdminPricingViewModel = viewModel(factory = AdminPricingViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Plan?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "요금제 추가", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "요금제 관리", subtitle = "구독 요금제를 생성하고 관리하세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.plans.isEmpty() -> EmptyState("등록된 요금제가 없습니다.")
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.plans, key = { it.id }) { plan ->
                        SectionCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(plan.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("월 ${"%,d".format(plan.priceMonthly)}원 / 연 ${"%,d".format(plan.priceAnnual)}원", style = MaterialTheme.typography.bodyMedium)
                                }
                                StatusChip(if (plan.isActive) "활성" else "비활성", if (plan.isActive) FarmGreen40 else FarmRed50)
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { viewModel.toggleActive(plan) }) { Text(if (plan.isActive) "비활성화" else "활성화") }
                                if (plan.code != "free") {
                                    TextButton(onClick = { deleteTarget = plan }) { Text("삭제", color = FarmRed50) }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        var code by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var priceMonthly by remember { mutableStateOf("") }
        var priceAnnual by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("요금제 추가") },
            text = {
                Column {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("코드 (예: pro)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = priceMonthly, onValueChange = { priceMonthly = it }, label = { Text("월 요금") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = priceAnnual, onValueChange = { priceAnnual = it }, label = { Text("연 요금") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createPlan(code, name, priceMonthly.toIntOrNull() ?: 0, priceAnnual.toIntOrNull() ?: 0) { ok, _ -> if (ok) showDialog = false }
                    },
                    enabled = !state.isSaving && code.isNotBlank() && name.isNotBlank(),
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } },
        )
    }

    deleteTarget?.let { plan ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("요금제 삭제") },
            text = { Text("'${plan.name}' 요금제를 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = { viewModel.deletePlan(plan.id); deleteTarget = null }) { Text("삭제") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("취소") } },
        )
    }
}
