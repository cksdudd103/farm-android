package com.smartfarm.app.util

import com.smartfarm.app.BuildConfig

/**
 * App-wide constants.
 *
 * Backend base URL resolution order:
 * 1. Runtime override saved via PreferencesManager / DataStore, set from the
 *    설정 (Settings) screen - see PreferencesManager.baseUrlFlow.
 * 2. BuildConfig.BASE_URL, defined in app/build.gradle.kts. This ships empty
 *    by default so the app prompts the user for their server address on
 *    first launch instead of pointing at a hardcoded/local address.
 */
object Constants {
    const val DEFAULT_BASE_URL: String = BuildConfig.BASE_URL

    /** Placeholder shown in the server address field until the user enters a real one. */
    const val BASE_URL_PLACEHOLDER = "http://서버주소:5000/"

    const val ROLE_ADMIN = "admin"
    const val ROLE_FARMER = "farmer"

    const val PREFS_NAME = "smart_farm_prefs"

    /** Returns true only when [url] looks like a real, user-provided server address. */
    fun isBaseUrlConfigured(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) return false
        if (trimmed.contains("서버주소")) return false
        return true
    }
}
