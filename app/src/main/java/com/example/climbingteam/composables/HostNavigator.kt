package com.example.climbingteam.composables

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.climbingteam.composables.specifics.ScreenLogin
import com.example.climbingteam.composables.specifics.ScreenMain
import com.example.climbingteam.viewmodels.AuthViewModel


@Composable
fun HostNavigator(vm: AuthViewModel) {

    val navController = rememberNavController()

    NavHost(navController, startDestination = if (vm.user.collectAsState().value != null) "main" else "login") {
        composable("login"){
            ScreenLogin(navController=navController, vm= vm )
        }

        composable("main") {
            ScreenMain(navController= navController, vm=vm)
        }



        // más pantallas..

    }
}