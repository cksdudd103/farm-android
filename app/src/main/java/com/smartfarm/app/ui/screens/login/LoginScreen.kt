package com.smartfarm.app.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartfarm.app.ui.AppViewModel
import com.smartfarm.app.util.ApiResult

@Composable
fun LoginScreen(appViewModel: AppViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }
    val errorMessage by appViewModel.errorMessage.collectAsState()

    var showSignUpDialog by remember { mutableStateOf(false) }
    var showFindIdDialog by remember { mutableStateOf(false) }
    var showFindPasswordDialog by remember { mutableStateOf(false) }
    var signUpSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun doLogin(loginEmail: String, loginPassword: String) {
        isLoading = true
        appViewModel.login(loginEmail, loginPassword, rememberMe) { _ -> isLoading = false }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.background,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .widthIn(max = 420.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🌱", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "스마트영농",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                "농업인을 위한 영농 관리 서비스",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(20.dp)) {
                    if (signUpSuccessMessage != null) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                signUpSuccessMessage ?: "",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("이메일") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("비밀번호") },
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                        Text(
                            "로그인 상태 유지",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextButton(onClick = { showFindIdDialog = true }) {
                            Text("아이디 찾기", style = MaterialTheme.typography.bodySmall)
                        }
                        TextButton(onClick = { showFindPasswordDialog = true }) {
                            Text("비밀번호 찾기", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { doLogin(email, password) },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text("로그인")
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(
                        onClick = {
                            appViewModel.clearError()
                            signUpSuccessMessage = null
                            showSignUpDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("아직 계정이 없으신가요? 회원가입")
                    }
                }
            }
        }
    }

    if (showSignUpDialog) {
        SignUpDialog(
            appViewModel = appViewModel,
            onDismiss = { showSignUpDialog = false },
            onSuccess = {
                showSignUpDialog = false
                signUpSuccessMessage = "회원가입이 완료되었습니다. 로그인해주세요."
            },
        )
    }

    if (showFindIdDialog) {
        FindAccountDialog(
            title = "아이디 찾기",
            label = "가입 시 등록한 이름 또는 이메일",
            appViewModel = appViewModel,
            onDismiss = { showFindIdDialog = false },
        )
    }

    if (showFindPasswordDialog) {
        FindAccountDialog(
            title = "비밀번호 찾기",
            label = "가입한 이메일",
            appViewModel = appViewModel,
            onDismiss = { showFindPasswordDialog = false },
        )
    }
}

@Composable
private fun SignUpDialog(
    appViewModel: AppViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordConfirm by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    fun validate(): String? {
        if (name.isBlank() || email.isBlank() || password.isBlank() || passwordConfirm.isBlank()) {
            return "모든 항목을 입력해주세요."
        }
        if (password.length < 6) {
            return "비밀번호는 6자 이상이어야 합니다."
        }
        if (password != passwordConfirm) {
            return "비밀번호가 일치하지 않습니다."
        }
        return null
    }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("회원가입") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("이름") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("이메일") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("비밀번호") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = passwordConfirm,
                    onValueChange = { passwordConfirm = it },
                    label = { Text("비밀번호 확인") },
                    singleLine = true,
                    enabled = !isSubmitting,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorText != null) {
                    Text(
                        errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    val validationError = validate()
                    if (validationError != null) {
                        errorText = validationError
                        return@Button
                    }
                    errorText = null
                    isSubmitting = true
                    appViewModel.register(name.trim(), email.trim(), password) { result ->
                        isSubmitting = false
                        when (result) {
                            is ApiResult.Success -> onSuccess()
                            is ApiResult.Error -> errorText = result.message
                        }
                    }
                },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("가입하기")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("취소") }
        },
    )
}

@Composable
private fun FindAccountDialog(
    title: String,
    label: String,
    appViewModel: AppViewModel,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(label) },
                    singleLine = true,
                    enabled = !isSubmitting && resultMessage == null,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (resultMessage != null) {
                    Text(
                        resultMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (errorText != null) {
                    Text(
                        errorText ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            if (resultMessage != null) {
                TextButton(onClick = onDismiss) { Text("확인") }
            } else {
                Button(
                    enabled = !isSubmitting,
                    onClick = {
                        errorText = null
                        isSubmitting = true
                        appViewModel.findAccount(query.trim()) { result ->
                            isSubmitting = false
                            when (result) {
                                is ApiResult.Success -> resultMessage = result.data
                                is ApiResult.Error -> errorText = result.message
                            }
                        }
                    },
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("확인")
                    }
                }
            }
        },
        dismissButton = {
            if (resultMessage == null) {
                TextButton(onClick = onDismiss, enabled = !isSubmitting) { Text("취소") }
            }
        },
    )
}
