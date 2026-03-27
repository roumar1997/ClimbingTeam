package com.example.climbingteam.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.climbingteam.ui.theme.ClimbingColors

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    COMPARE("compare",   "Comparar",  Icons.Default.CompareArrows),
    FAVORITES("favorites","Favoritos", Icons.Default.Favorite),
    SECTORS("sectors",   "Sectores",  Icons.Default.Terrain),
    MAP("map",           "Mapa",      Icons.Default.Map),
    MESSAGES("messages", "Mensajes",  Icons.Default.Chat),
    SETTINGS("settings", "Ajustes",   Icons.Default.Settings)
}

@Composable
fun ClimbingBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Column {
        // Thin accent line at the top of the bar
        HorizontalDivider(
            thickness = 1.dp,
            color = ClimbingColors.divider
        )
        NavigationBar(
            containerColor = ClimbingColors.bottomNavBackground,
            tonalElevation = 0.dp
        ) {
            BottomNavItem.entries.forEach { item ->
                val selected = currentRoute == item.route

                // Springy scale on selection
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.15f else 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "navIconScale_${item.route}"
                )

                NavigationBarItem(
                    selected = selected,
                    onClick  = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier
                                .size(23.dp)
                                .scale(scale)
                        )
                    },
                    label = {
                        Text(
                            text       = item.label,
                            style      = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor   = ClimbingColors.bottomNavSelected,
                        selectedTextColor   = ClimbingColors.bottomNavSelected,
                        unselectedIconColor = ClimbingColors.bottomNavUnselected,
                        unselectedTextColor = ClimbingColors.bottomNavUnselected,
                        // Pill-shaped indicator behind selected icon
                        indicatorColor      = ClimbingColors.bottomNavSelected.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}
