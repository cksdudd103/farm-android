package com.smartfarm.app.ui.screens.more

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartfarm.app.ui.components.ScreenHeader
import com.smartfarm.app.ui.navigation.Screen
import com.smartfarm.app.ui.navigation.moreMenuItems
import com.smartfarm.app.ui.theme.FarmGreen40
import com.smartfarm.app.ui.theme.FarmGreen95

@Composable
fun MoreScreen(isAdmin: Boolean, onNavigate: (Screen) -> Unit) {
    val items = moreMenuItems(isAdmin)

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "더보기", subtitle = "전체 메뉴")
        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
            items(items, key = { it.route }) { screen ->
                ListItem(
                    headlineContent = { Text(screen.labelKo, fontWeight = FontWeight.Medium) },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(FarmGreen95, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            screen.icon?.let { Icon(it, contentDescription = null, tint = FarmGreen40) }
                        }
                    },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                    modifier = Modifier.clickable { onNavigate(screen) },
                )
                HorizontalDivider()
            }
        }
    }
}
