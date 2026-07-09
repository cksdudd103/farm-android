package com.smartfarm.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartfarm.app.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = Constants.PREFS_NAME)

/**
 * Persists the configurable backend base URL and lightweight session info
 * (user id/name/email/role) using Jetpack DataStore. The Flask backend uses
 * cookie-based sessions (Flask-Login), so the actual auth token is the
 * session cookie held by OkHttp's CookieJar (see NetworkModule); this class
 * only remembers "who is logged in" for the UI layer and for restoring the
 * session across app restarts.
 */
class PreferencesManager(private val context: Context) {

    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_NAME = stringPreferencesKey("user_name")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_ROLE = stringPreferencesKey("user_role")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
    }

    val baseUrlFlow: Flow<String> = context.dataStore.data.map {
        it[Keys.BASE_URL] ?: Constants.DEFAULT_BASE_URL
    }

    suspend fun setBaseUrl(url: String) {
        context.dataStore.edit { it[Keys.BASE_URL] = url }
    }

    val loggedInUserFlow: Flow<SessionUser?> = context.dataStore.data.map { prefs ->
        val id = prefs[Keys.USER_ID] ?: return@map null
        SessionUser(
            id = id.toIntOrNull() ?: return@map null,
            name = prefs[Keys.USER_NAME].orEmpty(),
            email = prefs[Keys.USER_EMAIL].orEmpty(),
            role = prefs[Keys.USER_ROLE] ?: Constants.ROLE_FARMER,
        )
    }

    suspend fun saveSession(id: Int, name: String, email: String, role: String) {
        context.dataStore.edit {
            it[Keys.USER_ID] = id.toString()
            it[Keys.USER_NAME] = name
            it[Keys.USER_EMAIL] = email
            it[Keys.USER_ROLE] = role
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.remove(Keys.USER_ID)
            it.remove(Keys.USER_NAME)
            it.remove(Keys.USER_EMAIL)
            it.remove(Keys.USER_ROLE)
        }
    }

    /**
     * "로그인 상태 유지" (keep me logged in) preference. When true, the saved
     * session (see [saveSession]) is left in DataStore across app restarts so
     * the user is auto-logged in on next launch. When false, [AppViewModel]
     * clears any saved session at startup so the login screen is shown again.
     */
    val rememberMeFlow: Flow<Boolean> = context.dataStore.data.map { it[Keys.REMEMBER_ME] ?: false }

    suspend fun setRememberMe(remember: Boolean) {
        context.dataStore.edit { it[Keys.REMEMBER_ME] = remember }
    }
}

data class SessionUser(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
) {
    val isAdmin: Boolean get() = role == "admin"
}
