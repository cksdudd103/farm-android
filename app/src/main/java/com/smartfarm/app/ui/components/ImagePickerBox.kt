package com.smartfarm.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.smartfarm.app.util.ImageFileUtils
import java.io.File

/**
 * A tappable box that lets the user pick a photo from the camera or gallery.
 * Invokes [onImagePicked] with the resulting local [File] once selected.
 */
@Composable
fun ImagePickerBox(
    currentImageUrl: String? = null,
    onImagePicked: (File) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var pickedFile by remember { mutableStateOf<File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraFile by remember { mutableStateOf<File?>(null) }
    var showSourceDialog by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && pendingCameraFile != null) {
            pickedFile = pendingCameraFile
            onImagePicked(pendingCameraFile!!)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val file = File(context.cacheDir, "picked_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output -> input.copyTo(output) }
            }
            pickedFile = file
            onImagePicked(file)
        }
    }
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val (file, uri) = ImageFileUtils.createImageFile(context)
            pendingCameraFile = file
            pendingCameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { showSourceDialog = true },
        contentAlignment = Alignment.Center,
    ) {
        val displayModel: Any? = pickedFile ?: currentImageUrl
        if (displayModel != null) {
            AsyncImage(
                model = displayModel,
                contentDescription = "선택한 이미지",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.AddAPhoto, contentDescription = null)
                Spacer(Modifier.height(4.dp))
                Text("사진 추가", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (showSourceDialog) {
        AlertDialog(
            onDismissRequest = { showSourceDialog = false },
            title = { Text("사진 선택") },
            text = { Text("카메라로 촬영하거나 갤러리에서 선택하세요.") },
            confirmButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                }) { Text("카메라") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showSourceDialog = false
                    galleryLauncher.launch("image/*")
                }) { Text("갤러리") }
            },
        )
    }
}
