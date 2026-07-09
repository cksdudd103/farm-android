package com.smartfarm.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartfarm.app.ui.navigation.Screen
import com.smartfarm.app.ui.navigation.bottomNavItems
import com.smartfarm.app.ui.navigation.drawerSections

/**
 * Adaptive navigation shell:
 * - Compact width (phones): bottom navigation bar with 대시보드/작물/일지/더보기.
 * - Medium/Expanded width (tablets & desktop-like window sizes): permanent
 *   navigation drawer (사이드 메뉴) plus a top app bar, showing all
 *   destinations grouped by section.
 */
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun AppNavigationScaffold(
    currentRoute: String,
    isAdmin: Boolean,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val activity = androidx.compose.ui.platform.LocalContext.current as? androidx.activity.ComponentActivity
    val windowSizeClass = activity?.let { calculateWindowSizeClass(it) }
    val isCompact = windowSizeClass?.widthSizeClass == WindowWidthSizeClass.Compact || windowSizeClass == null

    val currentScreenLabel = remember(currentRoute) {
        (bottomNavItems + drawerSections(true).flatMap { it.second })
            .distinctBy { it.route }
            .firstOrNull { it.route == currentRoute }?.labelKo ?: "스마트영농"
    }

    if (isCompact) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(currentScreenLabel, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Filled.Logout, contentDescription = "로그아웃")
                        }
                    },
                )
            },
            bottomBar = {
                NavigationBar {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = { onNavigate(screen) },
                            icon = { Icon(screen.icon ?: Icons.Filled.Menu, contentDescription = screen.labelKo) },
                            label = { Text(screen.labelKo) },
                        )
                    }
                }
            },
        ) { padding -> content(padding) }
    } else {
        var drawerOpen by remember { mutableStateOf(true) }
        Row(Modifier.fillMaxSize()) {
            if (drawerOpen) {
                PermanentDrawer(
                    currentRoute = currentRoute,
                    isAdmin = isAdmin,
                    onNavigate = onNavigate,
                    onLogout = onLogout,
                )
            }
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    TopAppBar(
                        title = { Text(currentScreenLabel, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { drawerOpen = !drawerOpen }) {
                                Icon(Icons.Filled.Menu, contentDescription = "메뉴")
                            }
                        },
                        actions = {
                            IconButton(onClick = onLogout) {
                                Icon(Icons.Filled.Logout, contentDescription = "로그아웃")
                            }
                        },
                    )
                },
            ) { padding -> content(padding) }
        }
    }
}

@Composable
private fun PermanentDrawer(
    currentRoute: String,
    isAdmin: Boolean,
    onNavigate: (Screen) -> Unit,
    onLogout: () -> Unit,
) {
    Surface(
        modifier = Modifier.width(260.dp).fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp,
    ) {
        Column(Modifier.fillMaxHeight()) {
            Box(
                Modifier.fillMaxWidth().padding(20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    "🌱 스마트영농",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f)) {
                drawerSections(isAdmin).forEach { (sectionTitle, screens) ->
                    item {
                        Text(
                            sectionTitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 4.dp),
                        )
                    }
                    items(screens) { screen ->
                        DrawerItem(
                            screen = screen,
                            selected = currentRoute == screen.route,
                            onClick = { onNavigate(screen) },
                        )
                    }
                }
            }
            HorizontalDivider()
            NavigationDrawerItem(
                label = { Text("로그아웃") },
                selected = false,
                icon = { Icon(Icons.Filled.Logout, contentDescription = null) },
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun DrawerItem(screen: Screen, selected: Boolean, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(screen.labelKo) },
        selected = selected,
        icon = { Icon(screen.icon ?: Icons.Filled.Menu, contentDescription = null) },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
    )
}
