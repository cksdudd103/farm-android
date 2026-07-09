package com.smartfarm.app.ui.screens.journal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import coil.compose.AsyncImage
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.White
import java.io.File

private val workTypes = listOf("파종", "정식", "관수", "시비", "방제", "제초", "수확", "기타")
private val weatherOptions = listOf("맑음", "흐림", "비", "눈")

@Composable
fun JournalScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: JournalViewModel = viewModel(factory = JournalViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "일지 작성", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "영농 일지", subtitle = "매일의 작업 내용을 기록하세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.journals.isEmpty() -> EmptyState("작성된 일지가 없습니다.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.journals, key = { it.id }) { journal ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                val imageUrl = com.smartfarm.app.data.remote.NetworkModule.imageUrl(container.currentBaseUrl(), journal.image)
                                if (imageUrl != null) {
                                    AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(56.dp))
                                    Spacer(Modifier.width(12.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(journal.date, fontWeight = FontWeight.Bold)
                                        if (!journal.workType.isNullOrBlank()) {
                                            Spacer(Modifier.width(8.dp))
                                            StatusChip(journal.workType, FarmGreen40)
                                        }
                                    }
                                    if (!journal.cropName.isNullOrBlank()) {
                                        Text(journal.cropName, style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (!journal.content.isNullOrBlank()) {
                                        Text(journal.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteJournal(journal.id) }) {
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
        JournalEditDialog(
            isSaving = state.isSaving,
            onDismiss = { showDialog = false },
            onSave = { date, workType, weather, content, image ->
                viewModel.createJournal(null, date, workType, weather, content, image) { ok, _ ->
                    if (ok) showDialog = false
                }
            },
        )
    }
}

@Composable
private fun JournalEditDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (date: String, workType: String, weather: String, content: String, image: File?) -> Unit,
) {
    var date by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf(workTypes.first()) }
    var weather by remember { mutableStateOf(weatherOptions.first()) }
    var content by remember { mutableStateOf("") }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var workTypeExpanded by remember { mutableStateOf(false) }
    var weatherExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("영농 일지 작성") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ImagePickerBox(onImagePicked = { imageFile = it }, modifier = Modifier.fillMaxWidth().height(120.dp))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("날짜 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = workTypeExpanded, onExpandedChange = { workTypeExpanded = it }) {
                    OutlinedTextField(
                        value = workType, onValueChange = {}, readOnly = true, label = { Text("작업 유형") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = workTypeExpanded) },
                    )
                    ExposedDropdownMenu(expanded = workTypeExpanded, onDismissRequest = { workTypeExpanded = false }) {
                        workTypes.forEach { t -> DropdownMenuItem(text = { Text(t) }, onClick = { workType = t; workTypeExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = weatherExpanded, onExpandedChange = { weatherExpanded = it }) {
                    OutlinedTextField(
                        value = weather, onValueChange = {}, readOnly = true, label = { Text("날씨") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = weatherExpanded) },
                    )
                    ExposedDropdownMenu(expanded = weatherExpanded, onDismissRequest = { weatherExpanded = false }) {
                        weatherOptions.forEach { w -> DropdownMenuItem(text = { Text(w) }, onClick = { weather = w; weatherExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("작업 내용") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { onSave(date, workType, weather, content, imageFile) }, enabled = !isSaving && date.isNotBlank()) {
                if (isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
