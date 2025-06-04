package com.example.climbingteam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.climbingteam.Api.jsonApi
import com.example.climbingteam.composables.HostNavigator
import com.example.climbingteam.composables.specifics.ScreenLogin
import com.example.climbingteam.composables.specifics.ScreenMain
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


