package com.smartfarm.app.ui.screens.crops

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.data.model.Crop
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.*
import java.io.File

private val cropStatuses = listOf("재배중", "수확완료", "휴경")

@Composable
fun CropsScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: CropsViewModel = viewModel(factory = CropsViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingCrop by remember { mutableStateOf<Crop?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { editingCrop = null; showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "작물 추가", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "작물 관리", subtitle = "재배 중인 작물 정보를 관리하세요")

            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.crops.isEmpty() -> EmptyState("등록된 작물이 없습니다.\n우측 하단 버튼으로 작물을 추가해보세요.")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.crops, key = { it.id }) { crop ->
                        CropCard(
                            crop = crop,
                            baseUrl = container.currentBaseUrl(),
                            onEdit = { editingCrop = crop; showDialog = true },
                            onDelete = { viewModel.deleteCrop(crop.id) },
                        )
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        CropEditDialog(
            crop = editingCrop,
            isSaving = state.isSaving,
            onDismiss = { showDialog = false },
            onSave = { name, variety, location, area, plantDate, harvestDate, status, memo, image ->
                if (editingCrop == null) {
                    viewModel.createCrop(name, variety, location, area, plantDate, harvestDate, status, memo, image) { ok, _ ->
                        if (ok) showDialog = false
                    }
                } else {
                    viewModel.updateCrop(editingCrop!!.id, name, variety, location, area, plantDate, harvestDate, status, memo, image) { ok, _ ->
                        if (ok) showDialog = false
                    }
                }
            },
        )
    }
}

@Composable
private fun CropCard(crop: Crop, baseUrl: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val imageUrl = com.smartfarm.app.data.remote.NetworkModule.imageUrl(baseUrl, crop.image)
            AsyncImage(
                model = imageUrl ?: "",
                contentDescription = crop.name,
                modifier = Modifier.size(64.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(crop.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    StatusChip(crop.status, cropStatusColor(crop.status))
                }
                if (!crop.variety.isNullOrBlank()) Text(crop.variety, style = MaterialTheme.typography.bodyMedium)
                if (!crop.fieldLocation.isNullOrBlank()) {
                    Text("📍 ${crop.fieldLocation}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "메뉴")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("수정") }, onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text("삭제") }, onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}

private fun cropStatusColor(status: String) = when (status) {
    "수확완료" -> FarmAmber50
    "휴경" -> FarmEarth40
    else -> FarmGreen50
}

@Composable
private fun CropEditDialog(
    crop: Crop?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, variety: String, location: String, area: String, plantDate: String, harvestDate: String, status: String, memo: String, image: File?) -> Unit,
) {
    var name by remember { mutableStateOf(crop?.name ?: "") }
    var variety by remember { mutableStateOf(crop?.variety ?: "") }
    var location by remember { mutableStateOf(crop?.fieldLocation ?: "") }
    var area by remember { mutableStateOf(crop?.area?.toString() ?: "") }
    var plantDate by remember { mutableStateOf(crop?.plantingDate ?: "") }
    var harvestDate by remember { mutableStateOf(crop?.expectedHarvestDate ?: "") }
    var status by remember { mutableStateOf(crop?.status ?: "재배중") }
    var memo by remember { mutableStateOf(crop?.memo ?: "") }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var statusMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (crop == null) "작물 추가" else "작물 수정") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                ImagePickerBox(
                    currentImageUrl = null,
                    onImagePicked = { imageFile = it },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("작물명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = variety, onValueChange = { variety = it }, label = { Text("품종") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("재배 위치") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = area, onValueChange = { area = it }, label = { Text("재배 면적 (㎡)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = plantDate, onValueChange = { plantDate = it }, label = { Text("정식일 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = harvestDate, onValueChange = { harvestDate = it }, label = { Text("수확 예정일 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = statusMenuExpanded, onExpandedChange = { statusMenuExpanded = it }) {
                    OutlinedTextField(
                        value = status, onValueChange = {}, readOnly = true, label = { Text("상태") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusMenuExpanded) },
                    )
                    ExposedDropdownMenu(expanded = statusMenuExpanded, onDismissRequest = { statusMenuExpanded = false }) {
                        cropStatuses.forEach { s ->
                            DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusMenuExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, variety, location, area, plantDate, harvestDate, status, memo, imageFile) },
                enabled = !isSaving && name.isNotBlank(),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
