package com.example.climbingteam.composables.specifics


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.climbingteam.R
import com.example.climbingteam.ui.Mods.backMain
import com.example.climbingteam.ui.Mods.fillMax
import java.util.Collections




@Preview
@Composable
fun ScreenLoading (){

    Column(fillMax.padding(16.dp).then(backMain)) {
        Row(fillMax.weight(0.5f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Bottom) {
            Image(painter = painterResource(R.drawable.applogo) , contentDescription = "")

        }
        Row( modifier = fillMax.weight(0.5f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top) {
            CircularProgressIndicator(
                modifier = Modifier.size(64.dp),
                color = Color.White
            )
        }
    }

}