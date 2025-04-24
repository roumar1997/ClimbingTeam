package com.example.climbingteam.composables.specifics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.climbingteam.R
import com.example.climbingteam.ui.Mods.backMain
import com.example.climbingteam.ui.Mods.fillMax

@Preview
@Composable
fun ScreenLogin (){

    Column(fillMax.padding(16.dp).then(backMain)) {
        Row(fillMax.weight(0.5f)) {
            Image(painter = painterResource(R.drawable.applogo) , contentDescription = "")

        }
        Row(fillMax.weight(0.5f)) {

        }
    }

}