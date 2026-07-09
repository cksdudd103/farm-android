package com.smartfarm.app.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.smartfarm.app.data.model.Grade
import com.smartfarm.app.data.model.User
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmRed50
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminUsersUiState(
    val isLoading: Boolean = true,
    val users: List<User> = emptyList(),
    val grades: List<Grade> = emptyList(),
    val error: String? = null,
)

class AdminUsersViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUsersUiState())
    val uiState: StateFlow<AdminUsersUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val usersResult = container.farmRepository.fetchUsers()
            val gradesResult = container.farmRepository.fetchGrades()
            when {
                usersResult is ApiResult.Success -> {
                    val grades = (gradesResult as? ApiResult.Success)?.data ?: emptyList()
                    _uiState.value = AdminUsersUiState(isLoading = false, users = usersResult.data, grades = grades)
                }
                usersResult is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = usersResult.message)
            }
        }
    }

    fun updateRole(id: Int, role: String) {
        viewModelScope.launch { container.farmRepository.updateUserRole(id, role); load() }
    }

    fun updateActive(id: Int, isActive: Boolean) {
        viewModelScope.launch { container.farmRepository.updateUserActive(id, isActive); load() }
    }

    fun updateGrade(id: Int, gradeId: Int?) {
        viewModelScope.launch { container.farmRepository.updateUserGrade(id, gradeId); load() }
    }

    fun deleteUser(id: Int) {
        viewModelScope.launch { container.farmRepository.deleteUser(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = AdminUsersViewModel(container) as T
        }
    }
}

@Composable
fun AdminUsersScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: AdminUsersViewModel = viewModel(factory = AdminUsersViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var expandedUserId by remember { mutableStateOf<Int?>(null) }
    var deleteTarget by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "사용자 관리", subtitle = "회원 정보와 권한, 등급을 관리하세요")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            state.users.isEmpty() -> EmptyState("등록된 사용자가 없습니다.")
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.users, key = { it.id }) { user ->
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusChip(if (user.role == "admin") "관리자" else "농민", FarmGreen40)
                        }
                        Spacer(Modifier.height(6.dp))
                        Row {
                            StatusChip(user.gradeName ?: "일반회원", FarmGreen40)
                            Spacer(Modifier.width(6.dp))
                            StatusChip(if (user.isActiveUser) "활성" else "비활성", if (user.isActiveUser) FarmGreen40 else FarmRed50)
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = {
                                viewModel.updateRole(user.id, if (user.role == "admin") "farmer" else "admin")
                            }) { Text(if (user.role == "admin") "관리자 해제" else "관리자 지정") }
                            TextButton(onClick = { viewModel.updateActive(user.id, !user.isActiveUser) }) {
                                Text(if (user.isActiveUser) "비활성화" else "활성화")
                            }
                            TextButton(onClick = { expandedUserId = if (expandedUserId == user.id) null else user.id }) { Text("등급 변경") }
                            TextButton(onClick = { deleteTarget = user }) { Text("삭제", color = FarmRed50) }
                        }
                        if (expandedUserId == user.id) {
                            Spacer(Modifier.height(8.dp))
                            var gradeExpanded by remember { mutableStateOf(false) }
                            var selectedGradeName by remember(user.id) { mutableStateOf(user.gradeName ?: "일반회원") }
                            ExposedDropdownMenuBox(expanded = gradeExpanded, onExpandedChange = { gradeExpanded = it }) {
                                OutlinedTextField(
                                    value = selectedGradeName, onValueChange = {}, readOnly = true, label = { Text("등급") },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                                )
                                ExposedDropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                                    state.grades.forEach { grade ->
                                        DropdownMenuItem(text = { Text(grade.name) }, onClick = {
                                            selectedGradeName = grade.name
                                            gradeExpanded = false
                                            viewModel.updateGrade(user.id, grade.id)
                                            expandedUserId = null
                                        })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deleteTarget?.let { user ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("사용자 삭제") },
            text = { Text("${user.name}님을 삭제하시겠습니까?") },
            confirmButton = {
                Button(onClick = { viewModel.deleteUser(user.id); deleteTarget = null }) { Text("삭제") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("취소") } },
        )
    }
}
