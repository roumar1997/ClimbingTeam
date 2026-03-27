package com.example.climbingteam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.climbingteam.composables.HostNavigator
import com.example.climbingteam.viewmodels.AuthViewModel
import com.example.climbingteam.viewmodels.ChatViewModel
import com.example.climbingteam.viewmodels.SectorViewModel
import com.example.climbingteam.viewmodels.ThemeViewModel
import com.example.climbingteam.viewmodels.WeatherViewModel
import com.example.climbingteam.data.SubSectorCatalog
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel    by lazy { AuthViewModel() }
    private val weatherViewModel by lazy { WeatherViewModel(application) }
    private val sectorViewModel  by lazy { SectorViewModel(application) }
    private val chatViewModel    by lazy { ChatViewModel(application) }
    private val themeViewModel   by lazy { ThemeViewModel(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen BEFORE calling super.onCreate so the system
        // shows it immediately while the app initialises.
        installSplashScreen()

        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        SubSectorCatalog.init(this)
        enableEdgeToEdge()
        setContent {
            HostNavigator(
                authViewModel,
                weatherViewModel,
                sectorViewModel,
                chatViewModel,
                themeViewModel
            )
        }
    }
}
