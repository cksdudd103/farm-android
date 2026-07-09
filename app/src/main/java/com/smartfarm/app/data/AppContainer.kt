package com.smartfarm.app.data

import android.content.Context
import com.smartfarm.app.data.local.AppDatabase
import com.smartfarm.app.data.local.PreferencesManager
import com.smartfarm.app.data.remote.ApiService
import com.smartfarm.app.data.remote.NetworkModule
import com.smartfarm.app.data.repository.AuthRepository
import com.smartfarm.app.data.repository.FarmRepository
import com.smartfarm.app.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Simple manual DI container (no Hilt/Dagger dependency) shared across the
 * app. Holds the current [ApiService] instance, rebuilt automatically
 * whenever the configurable base URL changes (see PreferencesManager).
 */
class AppContainer(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val preferencesManager = PreferencesManager(context)

    private val _baseUrl = MutableStateFlow(Constants.DEFAULT_BASE_URL)
    val baseUrl: StateFlow<String> = _baseUrl

    private val _apiService = MutableStateFlow(
        NetworkModule.create(context, Constants.DEFAULT_BASE_URL)
    )
    val apiService: StateFlow<ApiService> = _apiService

    val database: AppDatabase by lazy { AppDatabase.getInstance(context) }

    val authRepository by lazy { AuthRepository(this) }
    val farmRepository by lazy { FarmRepository(this) }

    init {
        scope.launch {
            preferencesManager.baseUrlFlow.distinctUntilChanged().collect { url ->
                _baseUrl.value = url
                _apiService.value = NetworkModule.create(context, url)
            }
        }
    }

    fun currentApi(): ApiService = _apiService.value
    fun currentBaseUrl(): String = _baseUrl.value

    companion object {
        @Volatile private var instance: AppContainer? = null

        fun getInstance(context: Context): AppContainer =
            instance ?: synchronized(this) {
                instance ?: AppContainer(context.applicationContext).also { instance = it }
            }
    }
}
