package com.example.climbingteam.composables.specifics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.climbingteam.R
import com.example.climbingteam.composables.generals.TextHeader
import com.example.climbingteam.ui.Mods.backMain
import com.example.climbingteam.ui.Mods.fillMax
import com.example.climbingteam.ui.Styles
import com.example.climbingteam.ui.Styles.header_xlarge

@Preview
@Composable
fun ScreenLogin (){

    Column(fillMax.padding(16.dp).then(backMain)) {
        Row(fillMax.weight(0.5f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom) {
            Image(painter = painterResource(R.drawable.applogo) , contentDescription = "")

        }
        Row(fillMax.weight(0.5f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
            ElevatedCard(onClick = {}) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                    ) {
                    TextHeader("Iniciar sesión", textSize = header_xlarge,)
                    ElevatedButton(onClick = {}, modifier = Modifier.padding(top = 22.dp)) {
                        Image(modifier= Modifier.fillMaxSize(0.1f), painter = painterResource(R.drawable.googlelogo), contentDescription = "inicio sesión")
                    }
                }
            }



        }
    }

}