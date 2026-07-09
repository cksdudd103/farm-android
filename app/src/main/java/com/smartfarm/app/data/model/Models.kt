package com.smartfarm.app.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val name: String,
    val email: String,
    val role: String, // admin / farmer
    val phone: String? = null,
    @SerializedName("farm_name") val farmName: String? = null,
    val region: String? = null,
    @SerializedName("is_active_user") val isActiveUser: Boolean = true,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("plan_code") val planCode: String? = null,
    @SerializedName("plan_name") val planName: String? = null,
    @SerializedName("subscription_status") val subscriptionStatus: String? = null,
    @SerializedName("subscription_expiry") val subscriptionExpiry: String? = null,
    @SerializedName("billing_cycle") val billingCycle: String? = null,
    @SerializedName("is_waived") val isWaived: Boolean = false,
    @SerializedName("grade_id") val gradeId: Int? = null,
    @SerializedName("grade_code") val gradeCode: String? = null,
    @SerializedName("grade_name") val gradeName: String? = null,
    @SerializedName("grade_discount") val gradeDiscount: Int = 0,
) {
    val isAdmin: Boolean get() = role == "admin"
}

data class Plan(
    val id: Int,
    val code: String,
    val name: String,
    @SerializedName("price_monthly") val priceMonthly: Int = 0,
    @SerializedName("price_annual") val priceAnnual: Int = 0,
    val features: List<String> = emptyList(),
    @SerializedName("is_active") val isActive: Boolean = true,
    @SerializedName("display_order") val displayOrder: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class Grade(
    val id: Int,
    val code: String,
    val name: String,
    @SerializedName("discount_percent") val discountPercent: Int = 0,
    @SerializedName("min_spend") val minSpend: Int = 0,
    val color: String? = null,
    @SerializedName("display_order") val displayOrder: Int = 0,
    val description: String? = null,
)

data class Subscription(
    @SerializedName("plan_id") val planId: Int? = null,
    @SerializedName("plan_code") val planCode: String? = null,
    @SerializedName("plan_name") val planName: String? = null,
    val status: String? = null,
    @SerializedName("expiry_date") val expiryDate: String? = null,
    @SerializedName("billing_cycle") val billingCycle: String? = null,
    @SerializedName("is_waived") val isWaived: Boolean = false,
)

data class Crop(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val variety: String? = null,
    @SerializedName("field_location") val fieldLocation: String? = null,
    val area: Double? = null,
    @SerializedName("planting_date") val plantingDate: String? = null,
    @SerializedName("expected_harvest_date") val expectedHarvestDate: String? = null,
    val status: String = "재배중", // 재배중/수확완료/휴경
    val memo: String? = null,
    val image: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class Journal(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("crop_id") val cropId: Int? = null,
    @SerializedName("crop_name") val cropName: String? = null,
    val date: String,
    @SerializedName("work_type") val workType: String? = null,
    val weather: String? = null,
    val content: String? = null,
    val image: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class Task(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("crop_id") val cropId: Int? = null,
    @SerializedName("crop_name") val cropName: String? = null,
    val title: String,
    val memo: String? = null,
    @SerializedName("due_date") val dueDate: String? = null,
    val priority: String = "보통", // 높음/보통/낮음
    val status: String = "예정", // 예정/진행중/완료
    @SerializedName("created_at") val createdAt: String? = null,
)

data class InventoryItem(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val name: String,
    val category: String? = null, // 종자/비료/농약/농자재/기타
    val quantity: Double = 0.0,
    val unit: String? = null,
    val location: String? = null,
    @SerializedName("expiry_date") val expiryDate: String? = null,
    val memo: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class Shipment(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("crop_id") val cropId: Int? = null,
    @SerializedName("crop_name") val cropName: String? = null,
    val buyer: String? = null,
    val quantity: Double? = null,
    val unit: String? = null,
    @SerializedName("unit_price") val unitPrice: Double? = null,
    @SerializedName("total_price") val totalPrice: Double? = null,
    @SerializedName("shipment_date") val shipmentDate: String? = null,
    val status: String = "예정", // 예정/출하완료/정산완료
    val memo: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class Post(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("author_name") val authorName: String? = null,
    val category: String = "자유", // 자유/질문/판매/정보공유
    val title: String,
    val content: String? = null,
    val image: String? = null,
    val views: Int = 0,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class Diagnosis(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("crop_name") val cropName: String? = null,
    val image: String? = null,
    @SerializedName("disease_name") val diseaseName: String? = null,
    val confidence: Double? = null,
    val severity: String? = null, // 정상/주의/경고/위험
    val advice: String? = null,
    @SerializedName("created_at") val createdAt: String? = null,
)

data class RdaNotice(
    val id: Int,
    val title: String,
    val content: String? = null,
    val category: String = "공지",
    @SerializedName("notice_date") val noticeDate: String? = null,
    @SerializedName("source_url") val sourceUrl: String? = null,
)

data class MarketItem(
    val name: String,
    val unit: String,
    val price: Int,
    @SerializedName("change_pct") val changePct: Double,
    val trend: String, // up/down/flat
)

data class WeatherDay(
    val date: String,
    val day: String,
    val condition: String,
    @SerializedName("temp_max") val tempMax: Int,
    @SerializedName("temp_min") val tempMin: Int,
    val humidity: Int,
    @SerializedName("rain_prob") val rainProb: Int,
)

data class PesticideInfo(
    val name: String,
    val type: String, // 살충제/살균제/제초제
    val target: String,
    val crops: String,
    @SerializedName("safety_period") val safetyPeriod: String,
    val dilution: String,
)

data class SupportProgram(
    val title: String,
    val agency: String,
    val period: String,
    val target: String,
    val content: String,
    val status: String,
)

data class SafetyGuide(
    val category: String,
    val title: String,
    val content: String,
)

data class DashboardSummary(
    @SerializedName("total_crops") val totalCrops: Int,
    @SerializedName("growing_crops") val growingCrops: Int,
    @SerializedName("pending_tasks") val pendingTasks: Int,
    @SerializedName("today_tasks") val todayTasks: Int,
    @SerializedName("low_stock") val lowStock: Int,
    @SerializedName("total_shipment_amount") val totalShipmentAmount: Double,
    @SerializedName("recent_journals") val recentJournals: List<Journal> = emptyList(),
    @SerializedName("upcoming_tasks") val upcomingTasks: List<Task> = emptyList(),
    @SerializedName("chart_labels") val chartLabels: List<String> = emptyList(),
    @SerializedName("chart_counts") val chartCounts: List<Int> = emptyList(),
    @SerializedName("crop_status_counts") val cropStatusCounts: Map<String, Int> = emptyMap(),
)
