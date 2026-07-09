package com.smartfarm.app.ui.screens.market

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
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
import com.smartfarm.app.data.model.MarketItem
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmRed50
import com.smartfarm.app.ui.theme.FarmGreen50
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MarketUiState(val isLoading: Boolean = true, val items: List<MarketItem> = emptyList(), val error: String? = null)

class MarketViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(MarketUiState())
    val uiState: StateFlow<MarketUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchMarketPrices()) {
                is ApiResult.Success -> _uiState.value = MarketUiState(isLoading = false, items = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MarketViewModel(container) as T
        }
    }
}

@Composable
fun MarketScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: MarketViewModel = viewModel(factory = MarketViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "농산물 시세", subtitle = "오늘의 농산물 도매가격 동향")
        when {
            state.isLoading -> LoadingState()
            state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
            else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items) { item -> MarketRow(item) }
            }
        }
    }
}

@Composable
private fun MarketRow(item: MarketItem) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(14.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("단위: ${item.unit}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${"%,d".format(item.price)}원", fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (icon, color) = trendIconColor(item.trend)
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    Text("${item.changePct}%", color = color, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

private fun trendIconColor(trend: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> = when (trend) {
    "up" -> Icons.Filled.TrendingUp to FarmRed50
    "down" -> Icons.Filled.TrendingDown to FarmGreen50
    else -> Icons.Filled.TrendingFlat to Color.Gray
}
