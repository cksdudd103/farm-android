package com.smartfarm.app.ui.screens.weather

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import com.smartfarm.app.data.model.WeatherDay
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen90
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WeatherUiState(val isLoading: Boolean = true, val days: List<WeatherDay> = emptyList(), val error: String? = null)

class WeatherViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    fun load(region: String = "전국") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchWeather(region)) {
                is ApiResult.Success -> _uiState.value = WeatherUiState(isLoading = false, days = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = WeatherViewModel(container) as T
        }
    }
}

@Composable
fun WeatherScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: WeatherViewModel = viewModel(factory = WeatherViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "날씨 예보", subtitle = "7일간의 농업 날씨 정보")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            else -> LazyRow(contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.days) { day -> WeatherDayCard(day) }
            }
        }
    }
}

@Composable
private fun WeatherDayCard(day: WeatherDay) {
    Card(
        modifier = Modifier.width(110.dp),
        colors = CardDefaults.cardColors(containerColor = FarmGreen90),
    ) {
        Column(
            Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(day.day, fontWeight = FontWeight.Bold)
            Text(day.date.takeLast(5), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text(weatherEmoji(day.condition), style = MaterialTheme.typography.titleLarge)
            Text(day.condition, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Text("${day.tempMax}° / ${day.tempMin}°", fontWeight = FontWeight.Bold)
            Text("습도 ${day.humidity}%", style = MaterialTheme.typography.bodyMedium)
            Text("강수 ${day.rainProb}%", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun weatherEmoji(condition: String): String = when {
    condition.contains("맑") -> "☀️"
    condition.contains("구름") || condition.contains("흐림") -> "☁️"
    condition.contains("비") -> "🌧️"
    condition.contains("눈") -> "❄️"
    else -> "🌤️"
}
