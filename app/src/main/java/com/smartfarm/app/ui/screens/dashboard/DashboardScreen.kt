package com.smartfarm.app.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.*

@Composable
fun DashboardScreen(isAdmin: Boolean) {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    when {
        state.isLoading -> LoadingState()
        state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
        else -> {
            val summary = state.summary ?: return
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                ScreenHeader(
                    title = if (isAdmin) "관리자 대시보드" else "대시보드",
                    subtitle = "오늘의 영농 현황을 한눈에 확인하세요",
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false,
                ) {
                    items(dashboardStats(summary)) { stat ->
                        StatCard(title = stat.first, value = stat.second, icon = stat.third, modifier = Modifier.fillMaxWidth())
                    }
                }

                Spacer(Modifier.height(8.dp))

                SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("최근 7일 작업일지 기록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    JournalBarChart(labels = summary.chartLabels, counts = summary.chartCounts)
                }

                SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("작물 상태 분포", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (summary.cropStatusCounts.isEmpty()) {
                        Text("등록된 작물이 없습니다.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        summary.cropStatusCounts.forEach { (status, count) ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(status, style = MaterialTheme.typography.bodyMedium)
                                Text("${count}건", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("다가오는 작업", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (summary.upcomingTasks.isEmpty()) {
                        Text("예정된 작업이 없습니다.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        summary.upcomingTasks.forEach { task ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(task.title, fontWeight = FontWeight.Medium)
                                    Text(task.dueDate ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusChip(task.priority, priorityColor(task.priority))
                            }
                        }
                    }
                }

                SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text("최근 영농 일지", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (summary.recentJournals.isEmpty()) {
                        Text("작성된 일지가 없습니다.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        summary.recentJournals.forEach { journal ->
                            Column(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                Text("${journal.date} · ${journal.workType ?: ""}", fontWeight = FontWeight.Medium)
                                Text(journal.content ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

private fun dashboardStats(summary: com.smartfarm.app.data.model.DashboardSummary) = listOf(
    Triple("재배중 작물", "${summary.growingCrops} / ${summary.totalCrops}", Icons.Filled.Grass),
    Triple("진행중 작업", "${summary.pendingTasks}건", Icons.Filled.Checklist),
    Triple("오늘 할일", "${summary.todayTasks}건", Icons.Filled.Today),
    Triple("재고 부족", "${summary.lowStock}건", Icons.Filled.Warning),
)

private fun priorityColor(priority: String) = when (priority) {
    "높음" -> SeverityDanger
    "낮음" -> FarmGreen50
    else -> FarmAmber50
}
