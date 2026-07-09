package com.smartfarm.app.ui.screens.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.smartfarm.app.data.model.Post
import com.smartfarm.app.ui.components.*
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.White
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

private val categories = listOf("자유", "질문", "판매", "정보공유")

data class CommunityUiState(val isLoading: Boolean = true, val posts: List<Post> = emptyList(), val error: String? = null, val isSaving: Boolean = false)

class CommunityViewModel(private val container: AppContainer) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = container.farmRepository.fetchPosts()) {
                is ApiResult.Success -> _uiState.value = CommunityUiState(isLoading = false, posts = result.data)
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(isLoading = false, error = result.message)
            }
        }
    }

    fun createPost(category: String, title: String, content: String, image: File?, onDone: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            when (val result = container.farmRepository.createPost(category, title, content, image)) {
                is ApiResult.Success -> { _uiState.value = _uiState.value.copy(isSaving = false); load(); onDone(true, null) }
                is ApiResult.Error -> { _uiState.value = _uiState.value.copy(isSaving = false); onDone(false, result.message) }
            }
        }
    }

    fun deletePost(id: Int) {
        viewModelScope.launch { container.farmRepository.deletePost(id); load() }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = CommunityViewModel(container) as T
        }
    }
}

@Composable
fun CommunityScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as SmartFarmApp).container
    val viewModel: CommunityViewModel = viewModel(factory = CommunityViewModel.factory(container))
    val state by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = FarmGreen40) {
                Icon(Icons.Filled.Add, contentDescription = "글쓰기", tint = White)
            }
        },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            ScreenHeader(title = "커뮤니티", subtitle = "농업인들과 정보를 나눠보세요")
            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(message = state.error ?: "", onRetry = { viewModel.load() })
                state.posts.isEmpty() -> EmptyState("작성된 게시글이 없습니다.")
                else -> LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.posts, key = { it.id }) { post ->
                        SectionCard {
                            Row {
                                StatusChip(post.category, FarmGreen40)
                                Spacer(Modifier.width(8.dp))
                                Text("조회 ${post.views}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(post.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            if (!post.content.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(post.content, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(post.authorName ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (showDialog) {
        var category by remember { mutableStateOf(categories.first()) }
        var title by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var categoryExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("글쓰기") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
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
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("내용") }, minLines = 4, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.createPost(category, title, content, null) { ok, _ -> if (ok) showDialog = false } },
                    enabled = !state.isSaving && title.isNotBlank(),
                ) {
                    if (state.isSaving) CircularProgressIndicator(Modifier.height(18.dp), strokeWidth = 2.dp) else Text("등록")
                }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("취소") } },
        )
    }
}
