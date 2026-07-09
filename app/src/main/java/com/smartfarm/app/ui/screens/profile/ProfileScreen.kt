package com.smartfarm.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
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
import com.smartfarm.app.data.model.User
import com.smartfarm.app.ui.AppViewModel
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmGreen95
import com.smartfarm.app.ui.theme.White
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(val isLoading: Boolean = true, val user: User? = null, val error: String? = null)

class ProfileViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.authRepository.restoreSession()) {
                is ApiResult.Success -> _uiState.value = ProfileUiState(isLoading = false, user = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = ProfileViewModel(container) as T
        }
    }
}

@Composable
fun ProfileScreen(appViewModel: AppViewModel) {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: ProfileViewModel = viewModel(factory = ProfileViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(title = "회원 등급/프로필", subtitle = "내 정보와 등급 혜택을 확인하세요")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            else -> state.user?.let { user ->
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = MaterialTheme.shapes.large, color = FarmGreen95, modifier = Modifier.size(56.dp)) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(Icons.Filled.Person, contentDescription = null, tint = FarmGreen40)
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(user.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                Text(user.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    SectionCard {
                        Text("회원 등급", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        StatusChip(user.gradeName ?: "일반회원", FarmGreen40)
                        if (user.gradeDiscount > 0) {
                            Spacer(Modifier.height(6.dp))
                            Text("요금제 ${user.gradeDiscount}% 할인 혜택", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    SectionCard {
                        Text("이용 중인 요금제", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(user.planName ?: "무료", style = MaterialTheme.typography.bodyLarge)
                        if (!user.subscriptionExpiry.isNullOrBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text("만료일: ${user.subscriptionExpiry}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    SectionCard {
                        Text("농장 정보", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("농장명: ${user.farmName ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                        Text("지역: ${user.region ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                        Text("연락처: ${user.phone ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        onClick = { showLogoutDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("로그아웃")
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("로그아웃") },
            text = { Text("로그아웃 하시겠습니까?") },
            confirmButton = {
                Button(onClick = { showLogoutDialog = false; appViewModel.logout() }) { Text("로그아웃") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("취소") } },
        )
    }
}
