package com.smartfarm.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.smartfarm.app.data.AppContainer
import com.smartfarm.app.data.local.SessionUser
import com.smartfarm.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Top-level app state: current session user + auth actions, shared across all screens. */
class AppViewModel(private val container: AppContainer) : ViewModel() {

    private val _sessionUser = MutableStateFlow<SessionUser?>(null)
    val sessionUser: StateFlow<SessionUser?> = _sessionUser.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _baseUrl = MutableStateFlow(container.baseUrl.value)
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // "로그인 상태 유지" (keep me logged in) check: if the user did not
            // opt in last time, any leftover saved session is cleared before
            // the very first collection below, so the login screen is shown
            // instead of auto-entering the app. If they did opt in, the saved
            // session is left intact and restores the session automatically.
            val savedUser = container.preferencesManager.loggedInUserFlow.first()
            val rememberMe = container.preferencesManager.rememberMeFlow.first()
            if (savedUser != null && !rememberMe) {
                container.preferencesManager.clearSession()
            }
            // Resolve the persisted base URL up front so `isLoading` never
            // flips to false with a stale/default value - avoids a brief
            // flash of the first-run Settings screen for users who already
            // configured a real server address (container.baseUrl is only
            // populated asynchronously on a background dispatcher).
            _baseUrl.value = container.preferencesManager.baseUrlFlow.first()
            launch {
                container.baseUrl.collect { _baseUrl.value = it }
            }
            container.preferencesManager.loggedInUserFlow.collect { user ->
                _sessionUser.value = user
                _isLoading.value = false
            }
        }
    }

    fun login(email: String, password: String, rememberMe: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _errorMessage.value = null
            when (val result = container.authRepository.login(email, password, rememberMe)) {
                is ApiResult.Success -> onResult(true)
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    onResult(false)
                }
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        onResult: (ApiResult<com.smartfarm.app.data.model.User>) -> Unit,
    ) {
        viewModelScope.launch {
            val result = container.authRepository.register(
                name = name,
                email = email,
                password = password,
                phone = "",
                farmName = "",
                region = "",
            )
            onResult(result)
        }
    }

    fun findAccount(query: String, onResult: (ApiResult<String>) -> Unit) {
        viewModelScope.launch {
            onResult(container.authRepository.findAccountPlaceholder(query))
        }
    }

    fun logout() {
        viewModelScope.launch {
            container.authRepository.logout()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AppViewModel(container) as T
            }
        }
    }
}
