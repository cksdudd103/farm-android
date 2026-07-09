package com.smartfarm.app.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Builds Retrofit/OkHttp instances. Because the backend base URL is
 * configurable at runtime (see PreferencesManager.baseUrlFlow), this class
 * exposes [create] to (re)build a client whenever the URL changes, rather
 * than holding a single static singleton.
 */
object NetworkModule {

    private var cookieJar: PersistentCookieJar? = null

    fun cookieJar(context: Context): PersistentCookieJar =
        cookieJar ?: PersistentCookieJar(context.applicationContext).also { cookieJar = it }

    fun gson(): Gson = GsonBuilder().setLenient().create()

    fun okHttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .cookieJar(cookieJar(context))
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun create(context: Context, baseUrl: String): ApiService {
        // Retrofit requires a valid absolute URL at build time. When no server
        // address has been configured yet (fresh install, BuildConfig.BASE_URL
        // ships empty on purpose - see Constants.kt), fall back to a safe dummy
        // host so this never crashes; the UI blocks all screens other than
        // Settings until Constants.isBaseUrlConfigured() is true, so this
        // placeholder client is never actually used for real requests.
        val trimmed = baseUrl.trim()
        val safeUrl = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://localhost/"
        }
        val normalizedUrl = if (safeUrl.endsWith("/")) safeUrl else "$safeUrl/"
        val retrofit = Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient(context))
            .addConverterFactory(GsonConverterFactory.create(gson()))
            .build()
        return retrofit.create(ApiService::class.java)
    }

    /** Builds the absolute URL of an uploaded static image, e.g. "uploads/xxx.jpg". */
    fun imageUrl(baseUrl: String, relativePath: String?): String? {
        if (relativePath.isNullOrBlank()) return null
        val normalizedBase = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return normalizedBase + "static/" + relativePath
    }
}
