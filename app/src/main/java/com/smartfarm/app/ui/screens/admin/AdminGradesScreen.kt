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
import com.smartfarm.app.data.model.Grade
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmRed50
import com.smartfarm.app.ui.theme.White
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminGradesUiState(
    val isLoading: Boolean = true,
    val grades: List<Grade> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
)

class AdminGradesViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminGradesUiState())
    val uiState: StateFlow<AdminGradesUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchGrades()) {
                is ApiResult.Success -> _uiState.value = AdminGradesUiState(isLoading = false, grades = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createGrade(code: String, name: String, discountPercent: Int, minSpend: Int, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            val displayOrder = _uiState.value.grades.size
            when (val result = container.farmRepository.createGrade(code, name, discountPercent, minSpend, "#2E7D32", displayOrder, null)) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isSaving = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isSaving = false); onDone(false, result.message) }
            }
        }
    }

    fun deleteGrade(id: Int) {
        viewModelScope.launch { container.farmRepository.deleteGrade(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminGradesViewModel(container) as T
        }
    }
}

@Composable
fun AdminGradesScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: AdminGradesViewModel = viewModel(factory = AdminGradesViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Grade?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "등급 추가", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "등급 관리", subtitle = "회원 등급과 혜택을 관리하세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.grades.isEmpty() -> EmptyState("등록된 등급이 없습니다.")
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.grades, key = { it.id }) { grade ->
                        SectionCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(grade.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text("할인율 ${grade.discountPercent}% · 최소 누적금액 ${"%,d".format(grade.minSpend)}원", style = MaterialTheme.typography.bodyMedium)
                                }
                                StatusChip(grade.code, FarmGreen40)
                            }
                            if (!grade.description.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(grade.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(8.dp))
                            TextButton(onClick = { deleteTarget = grade }) { Text("삭제", color = FarmRed50) }
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
        var discountPercent by remember { mutableStateOf("") }
        var minSpend by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("등급 추가") },
            text = {
                Column {
                    OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("코드 (예: gold)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = discountPercent, onValueChange = { discountPercent = it }, label = { Text("할인율(%)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = minSpend, onValueChange = { minSpend = it }, label = { Text("최소 누적금액") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createGrade(code, name, discountPercent.toIntOrNull() ?: 0, minSpend.toIntOrNull() ?: 0) { ok, _ -> if (ok) showDialog = false }
                    },
                    enabled = !state.isSaving && code.isNotBlank() && name.isNotBlank(),
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } },
        )
    }

    deleteTarget?.let { grade ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("등급 삭제") },
            text = { Text("'${grade.name}' 등급을 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteGrade(grade.id); deleteTarget = null }) { Text("삭제") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("취소") } },
        )
    }
}
