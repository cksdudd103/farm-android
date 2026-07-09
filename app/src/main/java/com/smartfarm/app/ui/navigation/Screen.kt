package com.smartfarm.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/** All top-level & sub destinations in the app. */
sealed class Screen(val route: String, val labelKo: String, val icon: ImageVector? = null) {
    data object Login : Screen("login", "로그인")

    // Bottom-nav / drawer primary destinations
    data object Dashboard : Screen("dashboard", "대시보드", Icons.Filled.Dashboard)
    data object Crops : Screen("crops", "작물", Icons.Filled.Grass)
    data object Journal : Screen("journal", "일지", Icons.Filled.Book)
    data object More : Screen("more", "더보기", Icons.Filled.MoreHoriz)

    // Screens reachable from 더보기 / drawer
    data object Tasks : Screen("tasks", "작업 일정", Icons.Filled.Checklist)
    data object Inventory : Screen("inventory", "재고 관리", Icons.Filled.Inventory)
    data object Diagnose : Screen("diagnose", "AI 진단", Icons.Filled.CameraAlt)
    data object Market : Screen("market", "농산물 시세", Icons.Filled.TrendingUp)
    data object Weather : Screen("weather", "날씨 예보", Icons.Filled.WbSunny)
    data object Pesticide : Screen("pesticide", "농약 정보", Icons.Filled.Science)
    data object Support : Screen("support", "정부 지원사업", Icons.Filled.AccountBalance)
    data object Rda : Screen("rda", "농업진흥청 새소식", Icons.Filled.Newspaper)
    data object Community : Screen("community", "커뮤니티", Icons.Filled.Forum)
    data object Shipment : Screen("shipment", "출하 관리", Icons.Filled.LocalShipping)
    data object Safety : Screen("safety", "농작업 안전", Icons.Filled.HealthAndSafety)
    data object Pricing : Screen("pricing", "요금제", Icons.Filled.CardMembership)
    data object Profile : Screen("profile", "회원 등급/프로필", Icons.Filled.Person)
    data object Links : Screen("links", "농업 관련 사이트", Icons.Filled.Link)
    data object Settings : Screen("settings", "설정", Icons.Filled.Settings)

    // Admin-only
    data object AdminUsers : Screen("admin_users", "사용자 관리", Icons.Filled.ManageAccounts)
    data object AdminPricing : Screen("admin_pricing", "요금제 관리", Icons.Filled.PriceChange)
    data object AdminGrades : Screen("admin_grades", "등급 관리", Icons.Filled.MilitaryTech)
}

/** Destinations shown in the bottom navigation bar (mobile) and as primary tabs. */
val bottomNavItems = listOf(Screen.Dashboard, Screen.Crops, Screen.Journal, Screen.More)

/** Destinations shown in the 더보기 (More) sheet on mobile, and in the drawer/top bar on desktop/tablet. */
fun moreMenuItems(isAdmin: Boolean): List<Screen> = buildList {
    add(Screen.Tasks)
    add(Screen.Inventory)
    add(Screen.Diagnose)
    add(Screen.Market)
    add(Screen.Weather)
    add(Screen.Pesticide)
    add(Screen.Support)
    add(Screen.Rda)
    add(Screen.Community)
    add(Screen.Shipment)
    add(Screen.Safety)
    add(Screen.Pricing)
    add(Screen.Profile)
    add(Screen.Links)
    add(Screen.Settings)
    if (isAdmin) {
        add(Screen.AdminUsers)
        add(Screen.AdminPricing)
        add(Screen.AdminGrades)
    }
}

/** All destinations shown in the desktop/tablet drawer, grouped for section headers. */
fun drawerSections(isAdmin: Boolean): List<Pair<String, List<Screen>>> = buildList {
    add("메인" to listOf(Screen.Dashboard, Screen.Crops, Screen.Journal, Screen.Tasks))
    add(
        "영농 관리" to listOf(
            Screen.Inventory, Screen.Diagnose, Screen.Shipment, Screen.Safety
        )
    )
    add(
        "정보" to listOf(
            Screen.Market, Screen.Weather, Screen.Pesticide, Screen.Support, Screen.Rda, Screen.Links
        )
    )
    add("커뮤니티" to listOf(Screen.Community))
    add("내 계정" to listOf(Screen.Pricing, Screen.Profile, Screen.Settings))
    if (isAdmin) {
        add("관리자" to listOf(Screen.AdminUsers, Screen.AdminPricing, Screen.AdminGrades))
    }
}
