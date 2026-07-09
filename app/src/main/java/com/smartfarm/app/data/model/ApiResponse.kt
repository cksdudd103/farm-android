package com.smartfarm.app.data.model

/** Generic wrapper matching Flask's `{"ok": true/false, "data": ..., "msg": ...}` responses. */
data class ApiResponse<T>(
    val ok: Boolean,
    val data: T? = null,
    val msg: String? = null,
)

/** Wrapper for endpoints that nest a User under "user" (login/register/me). */
data class AuthResponse(
    val ok: Boolean,
    val user: User? = null,
    val msg: String? = null,
)

/** Wrapper for /api/market which nests both "data" and "date". */
data class MarketResponse(
    val ok: Boolean,
    val data: List<MarketItem> = emptyList(),
    val date: String? = null,
)

/** Wrapper for /api/weather which nests "region" alongside "data". */
data class WeatherResponse(
    val ok: Boolean,
    val region: String? = null,
    val data: List<WeatherDay> = emptyList(),
)

/** Wrapper for /api/dashboard/summary. */
data class DashboardResponse(
    val ok: Boolean,
    val data: DashboardSummary? = null,
)
