package com.example.climbingteam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.climbingteam.composables.HostNavigator
import com.example.climbingteam.viewmodels.AuthViewModel
import com.example.climbingteam.viewmodels.SectorViewModel
import com.example.climbingteam.viewmodels.WeatherViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    private val authViewModel by lazy { AuthViewModel() }
    private val weatherViewModel by lazy { WeatherViewModel(application) }
    private val sectorViewModel by lazy { SectorViewModel(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        enableEdgeToEdge()

        setContent {
            HostNavigator(authViewModel, weatherViewModel, sectorViewModel)
        }
    }
}
