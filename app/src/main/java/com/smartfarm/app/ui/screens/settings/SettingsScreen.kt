package com.smartfarm.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
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
import com.smartfarm.app.BuildConfig
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.ui.components.ScreenHeader
import com.smartfarm.app.ui.components.SectionCard
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.util.Constants
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val container: AppContainer) : ViewModel() {
    val baseUrl: StateFlow<String> get() = container.baseUrl

    var savedMessageVisible by mutableStateOf(false)
        private set

    fun saveBaseUrl(url: String) {
        val withScheme = if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            "http://$url"
        }
        val normalized = if (withScheme.endsWith("/")) withScheme else "$withScheme/"
        viewModelScope.launch {
            container.preferencesManager.setBaseUrl(normalized)
            savedMessageVisible = true
            delay(2000)
            savedMessageVisible = false
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(container) as T
        }
    }
}

/**
 * 설정 (Settings) screen. Lets the user point the app at their own Flask
 * backend server without rebuilding the APK - useful when installing on a
 * phone and connecting to a server on a different LAN/IP than the one baked
 * into BuildConfig.BASE_URL.
 *
 * When [requireInitialSetup] is true, this screen is shown as a mandatory
 * first-launch step (no valid server address configured yet) instead of the
 * regular login/settings navigation, and highlights that a server address
 * must be entered before continuing.
 */
@Composable
fun SettingsScreen(requireInitialSetup: Boolean = false) {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
    val currentBaseUrl by viewModel.baseUrl.collectAsState()
    var urlInput by remember(currentBaseUrl) {
        mutableStateOf(if (Constants.isBaseUrlConfigured(currentBaseUrl)) currentBaseUrl else "")
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(
            title = "설정",
            subtitle = if (requireInitialSetup) {
                "사용할 백엔드 서버 주소를 입력해야 앱을 시작할 수 있습니다"
            } else {
                "백엔드 서버 주소와 앱 정보를 확인하세요"
            },
        )

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Dns, contentDescription = null, tint = FarmGreen40)
                    Spacer(Modifier.width(8.dp))
                    Text("백엔드 서버 주소", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "사용 중인 Flask 서버 주소를 입력하세요. 예: http://192.168.0.10:5000/\n" +
                        "PC 에뮬레이터에서 같은 PC의 서버를 사용하는 경우 http://10.0.2.2:5000/ 을 입력하세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("서버 주소 (Base URL)") },
                    placeholder = { Text(Constants.BASE_URL_PLACEHOLDER) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    supportingText = {
                        Text(
                            "http:// 를 생략하면 자동으로 붙여 저장됩니다. 예: 192.168.0.10:5000",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.saveBaseUrl(urlInput) },
                    enabled = urlInput.isNotBlank() && !urlInput.contains("서버주소"),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("저장")
                }
                if (viewModel.savedMessageVisible) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = FarmGreen40,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "저장되었습니다. 다음 요청부터 새 서버 주소로 연결됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = FarmGreen40,
                        )
                    }
                }
            }

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = FarmGreen40)
                    Spacer(Modifier.width(8.dp))
                    Text("앱 정보", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
                Text("앱 이름: 스마트영농", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "버전: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
