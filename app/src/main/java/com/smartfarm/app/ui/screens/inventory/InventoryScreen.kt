package com.smartfarm.app.ui.screens.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.White

private val categories = listOf("종자", "비료", "농약", "농자재", "기타")

@Composable
fun InventoryScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: InventoryViewModel = viewModel(factory = InventoryViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "재고 추가", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "재고 관리", subtitle = "종자, 비료, 농약 등의 재고를 관리하세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.items.isEmpty() -> EmptyState("등록된 재고가 없습니다.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.items, key = { it.id }) { item ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.name, fontWeight = FontWeight.Bold)
                                        if (!item.category.isNullOrBlank()) {
                                            Spacer(Modifier.width(8.dp))
                                            StatusChip(item.category, FarmGreen40)
                                        }
                                    }
                                    Text("${item.quantity} ${item.unit ?: ""}", style = MaterialTheme.typography.bodyMedium)
                                    if (!item.location.isNullOrBlank()) {
                                        Text("보관 위치: ${item.location}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteItem(item.id) }) {
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
        InventoryEditDialog(
            isSaving = state.isSaving,
            onDismiss = { showDialog = false },
            onSave = { name, category, quantity, unit, location, expiryDate, memo ->
                viewModel.createItem(name, category, quantity, unit, location, expiryDate, memo) { ok, _ -> if (ok) showDialog = false }
            },
        )
    }
}

@Composable
private fun InventoryEditDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, category: String, quantity: Double, unit: String, location: String, expiryDate: String, memo: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.first()) }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var memo by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("재고 추가") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("품목명") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = category, onValueChange = {}, readOnly = true, label = { Text("분류") },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { c -> DropdownMenuItem(text = { Text(c) }, onClick = { category = c; categoryExpanded = false }) }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row {
                    OutlinedTextField(
                        value = quantity, onValueChange = { quantity = it }, label = { Text("수량") }, singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("단위") }, singleLine = true, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("보관 위치") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("유통기한 (YYYY-MM-DD)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = memo, onValueChange = { memo = it }, label = { Text("메모") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, category, quantity.toDoubleOrNull() ?: 0.0, unit, location, expiryDate, memo) },
                enabled = !isSaving && name.isNotBlank(),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("저장")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } },
    )
}
