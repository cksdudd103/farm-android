package com.smartfarm.app.ui.screens.diagnose

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.smartfarm.app.SmartFarmApp
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.*
import com.smartfarm.app.util.ImageFileUtils
import java.io.File

/** AI 진단 screen: pick/capture a crop photo and POST it to /api/diagnoses. */
@Composable
fun DiagnoseScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: DiagnoseViewModel = viewModel(factory = DiagnoseViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()

    var cropName by remember { mutableStateOf("") }
    var pickedFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingCameraFile != null) pickedFile = pendingCameraFile
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val file = File(context.cacheDir, "diagnose_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
            pickedFile = file
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val (file, uri) = ImageFileUtils.createImageFile(context)
            pendingCameraFile = file
            cameraLauncher.launch(uri)
        }
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader(title = "AI 진단", subtitle = "작물 사진을 촬영하면 병해충을 진단해드립니다")

        SectionCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (pickedFile != null) {
                    AsyncImage(model = pickedFile, contentDescription = "촬영한 사진", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(48.dp), tint = FarmGreen40)
                        Text("작물 사진을 추가하세요", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row {
                OutlinedButton(
                    onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("카메라")
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("갤러리")
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = cropName,
                onValueChange = { cropName = it },
                label = { Text("작물명 (선택)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { pickedFile?.let { viewModel.diagnose(cropName, it) } },
                enabled = pickedFile != null && !state.isDiagnosing,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isDiagnosing) {
                    CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("AI 진단 시작")
                }
            }
            if (state.diagnoseError != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.diagnoseError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
        }

        state.lastResult?.let { result ->
            SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("진단 결과", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(result.diseaseName ?: "정상", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    StatusChip(result.severity ?: "정상", severityColor(result.severity))
                }
                if (result.confidence != null) {
                    Spacer(Modifier.height(4.dp))
                    Text("신뢰도: ${(result.confidence * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium)
                }
                if (!result.advice.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(result.advice, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        SectionCard(modifier = Modifier.padding(16.dp)) {
            Text("진단 이력", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            when {
                state.isLoading -> LoadingState(Modifier.height(120.dp))
                state.history.isEmpty() -> Text("진단 이력이 없습니다.", style = MaterialTheme.typography.bodyMedium)
                else -> state.history.forEach { d ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        val imageUrl = com.smartfarm.app.data.remote.NetworkModule.imageUrl(container.currentBaseUrl(), d.image)
                        if (imageUrl != null) {
                            AsyncImage(model = imageUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                            Spacer(Modifier.width(10.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(d.diseaseName ?: "정상", fontWeight = FontWeight.Medium)
                            Text(d.createdAt ?: "-", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusChip(d.severity ?: "정상", severityColor(d.severity))
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun severityColor(severity: String?) = when (severity) {
    "주의" -> FarmAmber50
    "경고" -> SeverityWarning
    "위험" -> SeverityDanger
    else -> FarmGreen50
}
