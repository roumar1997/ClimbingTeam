package com.example.climbingteam.composables

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.climbingteam.composables.specifics.ScreenLogin
import com.example.climbingteam.ui.components.ClimbingBottomNav
import com.example.climbingteam.ui.screens.*
import com.example.climbingteam.ui.theme.ClimbingTeamTheme
import com.example.climbingteam.viewmodels.AuthViewModel
import com.example.climbingteam.viewmodels.WeatherViewModel

@Composable
fun HostNavigator(vm: AuthViewModel, weatherVm: WeatherViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "compare"

    val isLoggedIn = vm.user.collectAsState().value != null
    val startDest = if (isLoggedIn) "compare" else "login"

    // Routes that show bottom nav
    val bottomNavRoutes = setOf("compare", "favorites", "map", "settings")
    val showBottomNav = currentRoute in bottomNavRoutes

    ClimbingTeamTheme {
        Scaffold(
            bottomBar = {
                if (showBottomNav) {
                    ClimbingBottomNav(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            if (route != currentRoute) {
                                navController.navigate(route) {
                                    popUpTo("compare") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDest,
                modifier = if (showBottomNav) Modifier.padding(innerPadding) else Modifier
            ) {
                composable("login") {
                    ScreenLogin(navController = navController, vm = vm)
                }

                composable("compare") {
                    CompareScreen(
                        viewModel = weatherVm,
                        onLocationClick = { slotIndex ->
                            navController.navigate("detail/$slotIndex")
                        }
                    )
                }

                composable("detail/{slotIndex}") { backStackEntry ->
                    val slotIndex = backStackEntry.arguments?.getString("slotIndex")?.toIntOrNull() ?: 0
                    DetailScreen(
                        viewModel = weatherVm,
                        slotIndex = slotIndex,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("favorites") {
                    FavoritesScreen(
                        viewModel = weatherVm,
                        onLoadFavorite = { saved, slot ->
                            weatherVm.selectFromFavorite(slot, saved)
                            navController.navigate("compare") {
                                popUpTo("compare") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("map") {
                    MapScreen()
                }

                composable("settings") {
                    SettingsScreen(
                        authViewModel = vm,
                        weatherViewModel = weatherVm,
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}
