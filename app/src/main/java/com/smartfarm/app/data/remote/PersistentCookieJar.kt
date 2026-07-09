package com.smartfarm.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Persists cookies (the Flask-Login session cookie) to SharedPreferences so
 * the user stays logged in across app restarts, mirroring how a browser
 * would keep the session cookie for the webapp.
 */
class PersistentCookieJar(context: Context) : CookieJar {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cookie_store", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val memoryCache = mutableMapOf<String, MutableList<SerializableCookie>>()

    init {
        val raw = prefs.getString(KEY, null)
        if (raw != null) {
            val type = object : TypeToken<Map<String, MutableList<SerializableCookie>>>() {}.type
            val restored: Map<String, MutableList<SerializableCookie>>? = gson.fromJson(raw, type)
            restored?.forEach { (host, cookies) -> memoryCache[host] = cookies }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        val host = url.host
        val list = memoryCache.getOrPut(host) { mutableListOf() }
        cookies.forEach { cookie ->
            list.removeAll { it.name == cookie.name }
            list.add(SerializableCookie.from(cookie))
        }
        persist()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        val now = System.currentTimeMillis()
        val list = memoryCache[host] ?: return emptyList()
        list.removeAll { it.expiresAt in 1 until now }
        return list.map { it.toCookie(host) }
    }

    fun clear() {
        memoryCache.clear()
        prefs.edit().remove(KEY).apply()
    }

    private fun persist() {
        prefs.edit().putString(KEY, gson.toJson(memoryCache)).apply()
    }

    private data class SerializableCookie(
        val name: String,
        val value: String,
        val expiresAt: Long,
        val path: String,
        val secure: Boolean,
        val httpOnly: Boolean,
    ) {
        fun toCookie(host: String): Cookie {
            val builder = Cookie.Builder()
                .name(name)
                .value(value)
                .path(path)
                .domain(host)
            if (expiresAt > 0) builder.expiresAt(expiresAt)
            if (secure) builder.secure()
            if (httpOnly) builder.httpOnly()
            return builder.build()
        }

        companion object {
            fun from(cookie: Cookie) = SerializableCookie(
                name = cookie.name,
                value = cookie.value,
                expiresAt = cookie.expiresAt,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
            )
        }
    }

    companion object {
        private const val KEY = "cookies_json"
    }
}
