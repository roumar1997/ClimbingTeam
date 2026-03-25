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
import com.example.climbingteam.repository.ProfileRepository
import com.example.climbingteam.viewmodels.AuthViewModel
import com.example.climbingteam.viewmodels.ChatViewModel
import com.example.climbingteam.viewmodels.SectorViewModel
import com.example.climbingteam.viewmodels.WeatherViewModel
import kotlinx.coroutines.launch

@Composable
fun HostNavigator(
    vm: AuthViewModel,
    weatherVm: WeatherViewModel,
    sectorVm: SectorViewModel,
    chatVm: ChatViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "compare"
    val scope = rememberCoroutineScope()

    val isLoggedIn = vm.user.collectAsState().value != null
    val startDest = if (isLoggedIn) "compare" else "login"

    val bottomNavRoutes = setOf("compare", "favorites", "sectors", "map", "messages", "settings")
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
                        onBack = { navController.popBackStack() },
                        onViewProfile = { userId ->
                            navController.navigate("profile/$userId")
                        }
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

                composable("sectors") {
                    SectorFinderScreen(viewModel = sectorVm)
                }

                composable("map") {
                    MapScreen()
                }

                composable("messages") {
                    ConversationsScreen(
                        viewModel = chatVm,
                        onOpenChat = { convId, otherName ->
                            navController.navigate("chat/$convId/${otherName.encodeUrl()}")
                        },
                        onOpenNewChat = { otherUserId, otherProfile ->
                            scope.launch {
                                val convId = chatVm.startConversation(otherUserId, otherProfile)
                                if (convId.isNotEmpty()) {
                                    val displayName = otherProfile.displayName
                                        .ifEmpty { otherProfile.email.substringBefore("@") }
                                    navController.navigate("chat/$convId/${displayName.encodeUrl()}")
                                }
                            }
                        }
                    )
                }

                composable("chat/{convId}/{otherName}") { backStackEntry ->
                    val convId = backStackEntry.arguments?.getString("convId") ?: ""
                    val otherName = backStackEntry.arguments?.getString("otherName")?.decodeUrl() ?: ""
                    ChatScreen(
                        viewModel = chatVm,
                        conversationId = convId,
                        otherName = otherName,
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("settings") {
                    SettingsScreen(
                        authViewModel = vm,
                        weatherViewModel = weatherVm,
                        onLogout = {
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToProfile = {
                            navController.navigate("profile/me")
                        }
                    )
                }

                composable("profile/{userId}") { backStackEntry ->
                    val userId = backStackEntry.arguments?.getString("userId")
                    ProfileScreen(
                        userId = if (userId == "me") null else userId,
                        onBack = { navController.popBackStack() },
                        onSendMessage = { otherUserId ->
                            scope.launch {
                                // Fetch their profile to get the display name and photo
                                val otherProfile = ProfileRepository.getProfile(otherUserId)
                                val convId = chatVm.startConversation(otherUserId, otherProfile)
                                if (convId.isNotEmpty()) {
                                    val displayName = otherProfile?.displayName
                                        ?.ifEmpty { otherProfile.email.substringBefore("@") }
                                        ?: "Escalador"
                                    navController.navigate("chat/$convId/${displayName.encodeUrl()}")
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

// Simple URL encoding helpers for nav args
private fun String.encodeUrl(): String =
    java.net.URLEncoder.encode(this, "UTF-8")

private fun String.decodeUrl(): String =
    java.net.URLDecoder.decode(this, "UTF-8")
