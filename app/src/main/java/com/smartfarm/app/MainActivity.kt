package com.smartfarm.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.webkit.CookieManager
import android.webkit.PermissionRequest
import android.webkit.SslErrorHandler
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

/**
 * SmartFarm 앱은 실제 배포된 웹앱(React, Render)을 WebView로 표시하는 셸입니다.
 * 웹에서 기능을 수정/배포하면 앱 업데이트 없이 모바일에도 실시간으로 반영됩니다.
 * 이번 개선: WebView 캐시 비활성화, 버전 변경 시 캐시 자동 삭제, 네트워크/서버 에러 UI,
 * HTTPS 강제, 칩하면 권한 등으로 구버전 화면 고착과 보안을 개선했습니다.
 */
class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: View
    private lateinit var errorMessage: TextView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var pendingPermissionRequest: PermissionRequest? = null
    private var hasMainFrameError = false

    private val fileChooserLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val callback = filePathCallback
            filePathCallback = null
            if (callback == null) return@registerForActivityResult
            val data = result.data
            val uris: Array<Uri>? = when {
                result.resultCode != RESULT_OK -> null
                data?.clipData != null -> {
                    val clip = data.clipData!!
                    Array(clip.itemCount) { index -> clip.getItemAt(index).uri }
                }
                data?.data != null -> arrayOf(data.data!!)
                else -> null
            }
            callback.onReceiveValue(uris)
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            val request = pendingPermissionRequest
            pendingPermissionRequest = null
            if (request == null) return@registerForActivityResult
            if (granted && isTrustedOrigin(request.origin)) {
                request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
            } else {
                request.deny()
            }
        }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = FrameLayout(this)
        webView = WebView(this)
        progressBar = ProgressBar(this).apply { isIndeterminate = true }
        errorView = createErrorView()

        container.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        container.addView(
            errorView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT,
            ),
        )
        container.addView(
            progressBar,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                gravity = Gravity.CENTER
            },
        )
        setContentView(container)

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, false)
        }

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = false
            allowContentAccess = true
            mediaPlaybackRequiresUserGesture = true
            userAgentString = "$userAgentString SmartFarmAndroidApp/${BuildConfig.VERSION_NAME}"
        }

        webView.webViewClient = createWebViewClient()
        webView.webChromeClient = createWebChromeClient()

        clearWebCacheIfAppUpdated()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )

        webView.loadUrl("$APP_URL?appVersion=${BuildConfig.VERSION_CODE}")
    }

    private fun clearWebCacheIfAppUpdated() {
        val prefs = getSharedPreferences(WEBVIEW_PREFS, MODE_PRIVATE)
        val cachedVersion = prefs.getInt(WEBVIEW_VERSION_KEY, -1)
        if (cachedVersion != BuildConfig.VERSION_CODE) {
            webView.clearCache(true)
            prefs.edit().putInt(WEBVIEW_VERSION_KEY, BuildConfig.VERSION_CODE).apply()
        }
    }

    private fun createErrorView(): View {
        val retryButton = Button(this).apply {
            text = "다시 시도"
            setOnClickListener {
                hasMainFrameError = false
                errorView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                if (webView.url.isNullOrBlank()) {
                    webView.loadUrl("$APP_URL?appVersion=${BuildConfig.VERSION_CODE}")
                } else {
                    webView.reload()
                }
            }
        }
        errorMessage = TextView(this).apply {
            text = "페이지를 불러오지 못했습니다."
            textSize = 17f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.WHITE)
            visibility = View.GONE
            addView(errorMessage)
            addView(retryButton)
        }
    }

    private fun createWebViewClient() = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val uri = request.url
            if (isTrustedOrigin(uri)) return false
            openExternalUri(uri)
            return true
        }

        override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            hasMainFrameError = false
            errorView.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
        }

        override fun onPageFinished(view: WebView, url: String?) {
            super.onPageFinished(view, url)
            progressBar.visibility = View.GONE
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError,
        ) {
            super.onReceivedError(view, request, error)
            if (request.isForMainFrame) {
                showLoadError("네트워크 연결을 확인한 후 다시 시도해주세요.")
            }
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: WebResourceResponse,
        ) {
            super.onReceivedHttpError(view, request, errorResponse)
            if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                showLoadError("서버가 응답하지 않습니다. 잠시 후 다시 시도해주세요.")
            }
        }

        override fun onReceivedSslError(
            view: WebView,
            handler: SslErrorHandler,
            error: SslError,
        ) {
            handler.cancel()
            val failedUri = runCatching { Uri.parse(error.url) }.getOrNull()
            if (failedUri != null && isTrustedOrigin(failedUri)) {
                showLoadError("보안 연결을 확인할 수 없어 페이지를 열지 않았습니다.")
            }
        }
    }

    private fun createWebChromeClient() = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (!hasMainFrameError) {
                progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }
        }

        override fun onShowFileChooser(
            webView: WebView,
            callback: ValueCallback<Array<Uri>>,
            params: FileChooserParams,
        ): Boolean {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = callback
            val chooserIntent = params.createIntent().apply {
                if (action == Intent.ACTION_GET_CONTENT) {
                    putExtra(
                        Intent.EXTRA_ALLOW_MULTIPLE,
                        params.mode == FileChooserParams.MODE_OPEN_MULTIPLE,
                    )
                    type = "image/*"
                }
            }
            return try {
                fileChooserLauncher.launch(chooserIntent)
                true
            } catch (_: Exception) {
                filePathCallback = null
                callback.onReceiveValue(null)
                false
            }
        }

        override fun onPermissionRequest(request: PermissionRequest) {
            runOnUiThread {
                val onlyCameraRequested =
                    request.resources.isNotEmpty() &&
                        request.resources.all { it == PermissionRequest.RESOURCE_VIDEO_CAPTURE }
                if (!isTrustedOrigin(request.origin) || !onlyCameraRequested) {
                    request.deny()
                    return@runOnUiThread
                }
                if (
                    ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.CAMERA,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    request.grant(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
                } else {
                    pendingPermissionRequest?.deny()
                    pendingPermissionRequest = request
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                }
            }
        }

        override fun onPermissionRequestCanceled(request: PermissionRequest) {
            if (pendingPermissionRequest === request) {
                pendingPermissionRequest = null
            }
        }
    }

    private fun showLoadError(message: String) {
        hasMainFrameError = true
        progressBar.visibility = View.GONE
        errorMessage.text = message
        errorView.visibility = View.VISIBLE
    }

    private fun isTrustedOrigin(uri: Uri): Boolean =
        uri.scheme.equals("https", ignoreCase = true) &&
            uri.host.equals(APP_HOST, ignoreCase = true) &&
            (uri.port == -1 || uri.port == 443)

    private fun openExternalUri(uri: Uri) {
        val externalIntent = when (uri.scheme?.lowercase()) {
            "http", "https", "market" -> Intent(Intent.ACTION_VIEW, uri)
            "intent" -> runCatching {
                Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    component = null
                    selector = null
                }
            }.getOrNull()
            else -> null
        }
        if (externalIntent == null) {
            Toast.makeText(this, "지원하지 않는 링크입니다.", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(externalIntent)
        } catch (_: Exception) {
            val fallbackUrl = externalIntent.getStringExtra("browser_fallback_url")
            val fallbackUri = fallbackUrl?.let(Uri::parse)
            if (fallbackUri != null && fallbackUri.scheme.equals("https", ignoreCase = true)) {
                startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
            } else {
                Toast.makeText(this, "링크를 열 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onDestroy() {
        pendingPermissionRequest?.deny()
        pendingPermissionRequest = null
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
        (webView.parent as? FrameLayout)?.removeView(webView)
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val APP_HOST = "farm-android-pymx.onrender.com"
        private const val APP_URL = "https://$APP_HOST/"
        private const val WEBVIEW_PREFS = "webview_preferences"
        private const val WEBVIEW_VERSION_KEY = "app_version"
    }
}
