package com.example.climbingteam.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.climbingteam.ui.theme.ClimbingColors

enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    COMPARE("compare", "Comparar", Icons.Default.CompareArrows),
    FAVORITES("favorites", "Favoritos", Icons.Default.Favorite),
    MAP("map", "Mapa", Icons.Default.Map),
    SETTINGS("settings", "Ajustes", Icons.Default.Settings)
}

@Composable
fun ClimbingBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = ClimbingColors.bottomNavBackground,
        contentColor = ClimbingColors.textPrimary,
        tonalElevation = 0.dp
    ) {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        item.icon,
                        contentDescription = item.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ClimbingColors.bottomNavSelected,
                    selectedTextColor = ClimbingColors.bottomNavSelected,
                    unselectedIconColor = ClimbingColors.bottomNavUnselected,
                    unselectedTextColor = ClimbingColors.bottomNavUnselected,
                    indicatorColor = ClimbingColors.bottomNavSelected.copy(alpha = 0.12f)
                )
            )
        }
    }
}
