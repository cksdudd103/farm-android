package com.smartfarm.app.ui.screens.shipment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.smartfarm.app.data.model.Shipment
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmAmber50
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmGreen50
import com.smartfarm.app.ui.theme.White
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val statuses = listOf("예정", "출하완료", "정산완료")

data class ShipmentUiState(val isLoading: Boolean = true, val items: List<Shipment> = emptyList(), val error: String? = null, val isSaving: Boolean = false)

class ShipmentViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(ShipmentUiState())
    val uiState: StateFlow<ShipmentUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchShipments()) {
                is ApiResult.Success -> _uiState.value = ShipmentUiState(isLoading = false, items = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createShipment(buyer: String, quantity: Double, unit: String, unitPrice: Double, shipmentDate: String, status: String, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = container.farmRepository.createShipment(null, buyer, quantity, unit, unitPrice, shipmentDate, status, "")) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isSaving = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isSaving = false); onDone(false, result.message) }
            }
        }
    }

    fun deleteShipment(id: Int) {
        viewModelScope.launch { container.farmRepository.deleteShipment(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ShipmentViewModel(container) as T
        }
    }
}

@Composable
fun ShipmentScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: ShipmentViewModel = viewModel(factory = ShipmentViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "출하 추가", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "출하 관리", subtitle = "농산물 출하 및 정산 내역을 관리하세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.items.isEmpty() -> EmptyState("등록된 출하 내역이 없습니다.")
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.items, key = { it.id }) { s ->
                        SectionCard {
                            Row {
                                Text(s.buyer ?: "미지정", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                StatusChip(s.status, shipmentStatusColor(s.status))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text("${s.quantity ?: 0} ${s.unit ?: ""} · ${"%,d".format((s.totalPrice ?: 0.0).toInt())}원", style = MaterialTheme.typography.bodyMedium)
                            Text(s.shipmentDate ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        var buyer by remember { mutableStateOf("") }
        var quantity by remember { mutableStateOf("") }
        var unit by remember { mutableStateOf("") }
        var unitPrice by remember { mutableStateOf("") }
        var shipmentDate by remember { mutableStateOf("") }
        var status by remember { mutableStateOf(statuses.first()) }
        var statusExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("출하 등록") },
            text = {
                Column {
                    OutlinedTextField(value = buyer, onValueChange = { buyer = it }, label = { Text("구매처") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("수량") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("단위") }, singleLine = true, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = unitPrice, onValueChange = { unitPrice = it }, label = { Text("단가") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = shipmentDate, onValueChange = { shipmentDate = it }, label = { Text("출하일 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    ExposedDropdownMenuBox(expanded = statusExpanded, onExpandedChange = { statusExpanded = it }) {
                        OutlinedTextField(
                            value = status, onValueChange = {}, readOnly = true, label = { Text("상태") },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        )
                        ExposedDropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                            statuses.forEach { s -> DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusExpanded = false }) }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createShipment(buyer, quantity.toDoubleOrNull() ?: 0.0, unit, unitPrice.toDoubleOrNull() ?: 0.0, shipmentDate, status) { ok, _ -> if (ok) showDialog = false }
                    },
                    enabled = !state.isSaving,
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } },
        )
    }
}

private fun shipmentStatusColor(status: String) = when (status) {
    "정산완료" -> FarmGreen50
    "출하완료" -> FarmAmber50
    else -> FarmGreen40
}
