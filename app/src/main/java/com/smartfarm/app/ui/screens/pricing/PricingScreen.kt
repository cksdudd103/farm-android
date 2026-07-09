package com.smartfarm.app.ui.screens.pricing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.model.Plan
import com.smartfarm.app.data.model.Subscription
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmGreen95
import com.smartfarm.app.ui.theme.White
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PricingUiState(
    val isLoading: Boolean = true,
    val plans: List<Plan> = emptyList(),
    val subscription: Subscription? = null,
    val error: String? = null,
    val isUpgrading: Boolean = false,
)

class PricingViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(PricingUiState())
    val uiState: StateFlow<PricingUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val plansResult = container.farmRepository.fetchPlans()
            val subResult = container.farmRepository.fetchMySubscription()
            when {
                plansResult is ApiResult.Success -> {
                    val sub = (subResult as? ApiResult.Success)?.data
                    _uiState.value = PricingUiState(isLoading = false, plans = plansResult.data, subscription = sub)
                }
                plansResult is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = plansResult.message)
            }
        }
    }

    fun upgrade(planId: Int, billingCycle: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUpgrading = true)
            when (val result = container.farmRepository.upgradeSubscription(planId, billingCycle)) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isUpgrading = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isUpgrading = false); onDone(false, result.message) }
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = PricingViewModel(container) as T
        }
    }
}

@Composable
fun PricingScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: PricingViewModel = viewModel(factory = PricingViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var pendingPlanId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "요금제", subtitle = "우리 농장에 맞는 요금제를 선택하세요")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    state.subscription?.let { sub ->
                        SectionCard(modifier = Modifier.fillMaxWidth()) {
                            Text("현재 이용 중인 요금제", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(sub.planName ?: "무료", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            if (sub.isWaived) {
                                Spacer(Modifier.height(4.dp))
                                StatusChip("이용료 면제", FarmGreen40)
                            } else if (!sub.expiryDate.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text("만료일: ${sub.expiryDate}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                items(state.plans, key = { it.id }) { plan ->
                    val isCurrent = state.subscription?.planId == plan.id
                    SectionCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(plan.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                Text(
                                    if (plan.priceMonthly == 0) "무료" else "월 ${"%,d".format(plan.priceMonthly)}원",
                                    style = MaterialTheme.typography.bodyLarge, color = FarmGreen40, fontWeight = FontWeight.Bold,
                                )
                            }
                            if (isCurrent) StatusChip("이용중", FarmGreen40)
                        }
                        Spacer(Modifier.height(8.dp))
                        plan.features.forEach { feature ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = FarmGreen40, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(feature, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { pendingPlanId = plan.id },
                            enabled = !isCurrent && !state.isUpgrading,
                            colors = ButtonDefaults.buttonColors(containerColor = FarmGreen40, contentColor = White),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (isCurrent) "현재 요금제" else "선택하기")
                        }
                    }
                }
            }
        }
    }

    pendingPlanId?.let { planId ->
        AlertDialog(
            onDismissRequest = { pendingPlanId = null },
            title = { Text("요금제 변경") },
            text = { Text("선택한 요금제로 변경하시겠습니까?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.upgrade(planId, "monthly") { _, _ -> pendingPlanId = null }
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { pendingPlanId = null }) { Text("취소") } },
        )
    }
}
