package com.example.climbingteam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.climbingteam.api.jsonApi
import com.example.climbingteam.composables.HostNavigator
import com.example.climbingteam.viewmodels.AuthViewModel
import com.google.firebase.FirebaseApp


class MainActivity : ComponentActivity() {
    private val authViewModel by lazy { AuthViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // lanza firebase, checkea el google-config.json y conecta con la app de firebase
        FirebaseApp.initializeApp(this);

        jsonApi.initData(this)

        enableEdgeToEdge()

        setContent {

            HostNavigator(authViewModel)

        }


    }
}


