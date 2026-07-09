package com.smartfarm.app.ui.screens.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.*

private val priorities = listOf("높음", "보통", "낮음")
private val statuses = listOf("예정", "진행중", "완료")

@Composable
fun TasksScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: TasksViewModel = viewModel(factory = TasksViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "작업 추가", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "작업 일정", subtitle = "농작업 일정을 관리하세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.tasks.isEmpty() -> EmptyState("등록된 작업이 없습니다.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.tasks, key = { it.id }) { task ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = task.status == "완료",
                                    onCheckedChange = { checked ->
                                        viewModel.updateTaskStatus(task.id, if (checked) "완료" else "예정")
                                    },
                                )
                                Column(Modifier.weight(1f)) {
                                    Text(task.title, fontWeight = FontWeight.Bold)
                                    Text(task.dueDate ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusChip(task.priority, priorityColor(task.priority))
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "삭제")
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
        TaskEditDialog(
            isSaving = state.isSaving,
            onDismiss = { showDialog = false },
            onSave = { title, memo, dueDate, priority ->
                viewModel.createTask(null, title, memo, dueDate, priority, "예정") { ok, _ -> if (ok) showDialog = false }
            },
        )
    }
}

private fun priorityColor(priority: String) = when (priority) {
    "높음" -> SeverityDanger
    "낮음" -> FarmGreen50
    else -> FarmAmber50
}

@Composable
private fun TaskEditDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (title: String, memo: String, dueDate: String, priority: String) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(priorities[1]) }
    var priorityExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("작업 추가") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("작업명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("마감일 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = priorityExpanded, onExpandedChange = { priorityExpanded = it }) {
                    OutlinedTextField(
                        value = priority, onValueChange = {}, readOnly = true, label = { Text("우선순위") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    )
                    ExposedDropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                        priorities.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { priority = p; priorityExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(title, memo, dueDate, priority) }, enabled = !isSaving && title.isNotBlank()) {
                if (isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
