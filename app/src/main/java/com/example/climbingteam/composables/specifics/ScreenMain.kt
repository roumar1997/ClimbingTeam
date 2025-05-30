package com.example.climbingteam.composables.specifics

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.climbingteam.Api.jsonApi
import com.example.climbingteam.R
import com.example.climbingteam.composables.generals.TextHeader
import com.example.climbingteam.composables.generals.TextParrafo
import com.example.climbingteam.ui.Mods.backMain
import com.example.climbingteam.ui.Mods.fillMax
import com.example.climbingteam.ui.Styles
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ScreenMain(
    //navController: NavController
){

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet( modifier = Modifier.fillMaxWidth(0.5f)) {
                Column (Modifier.fillMaxHeight()){
                    Row (Modifier.fillMaxSize().weight(0.15f).background(Styles.color_main_), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                        Column (Modifier.fillMaxSize().weight(0.7f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                            Image(painter = painterResource(R.drawable.applogo), contentDescription = "")
                        }
                        Column (Modifier.fillMaxSize().weight(0.3f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center){
                            //TextHeader("Menú", textSize = Styles.header_medium)
                        }
                    }
                    Row (Modifier.fillMaxSize().weight(0.1f).background(Styles.color_tertiary), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                        ElevatedButton(onClick =  {}) {
                            Icon(Icons.Filled.AccountCircle, contentDescription = "")
                            Spacer(modifier = Modifier.width(15.dp))
                            TextParrafo("Cuenta de usuario", Styles.text_large)
                        }
                    }
                    Row (Modifier.fillMaxSize().weight(0.1f).background(Styles.color_tertiary), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically){
                        ElevatedButton(onClick =  {}) {
                            Icon(Icons.Filled.Settings, contentDescription = "")
                            Spacer(modifier = Modifier.width(15.dp))
                            TextParrafo("Ajustes", Styles.text_large)
                        }
                    }
                    Row (Modifier.fillMaxSize().weight(0.65f).background(Styles.color_tertiary)){

                    }

                }
            }
        }
    ) {

        Scaffold(modifier = Modifier.navigationBarsPadding().systemBarsPadding(), containerColor = Styles.color_main, topBar = {TopAppBar(
            title = {},
            navigationIcon = { IconButton(onClick = { scope.launch { drawerState.open() }}) {
                Icon(Icons.Filled.Menu, contentDescription = "")
            }},
            colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = Color.Transparent)
            )} ){ innerPadding->
            Column(fillMax.then(backMain).navigationBarsPadding().systemBarsPadding().padding(innerPadding)) {
                Row(
                    fillMax.weight(0.37f),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.Bottom) {
                    Image(painter = painterResource(R.drawable.applogo) , contentDescription = "")

                }
                Row(
                    fillMax.weight(0.54f)){
                    // tiempo actual de mi ubicacion

                    LazyColumn {
                        items(jsonApi.estaciones.features.size){ index ->
                            Text(jsonApi.estaciones.features.get(index).properties.NOMBRE)


                        }
                    }
                }


                Row(
                    fillMax.weight(0.07f)){


                }
            }



        }

    }







}




//@Preview
@Composable
fun MainBotbar(){
    BottomAppBar (containerColor = Styles.color_tertiary ){
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically)
        {
            Column(fillMax.weight(0.30f), horizontalAlignment = Alignment.CenterHorizontally)
            {
                Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "", modifier = Modifier.fillMaxSize().padding(24.dp))
            }
            Column(fillMax.weight(0.05f),horizontalAlignment = Alignment.CenterHorizontally)
            {
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.DarkGray)
            }
            Column(fillMax.weight(0.30f),horizontalAlignment = Alignment.CenterHorizontally)
            {
                Icon(Icons.Filled.DateRange, contentDescription = "", modifier = Modifier.fillMaxSize().padding(24.dp))
            }
            Column(fillMax.weight(0.05f),horizontalAlignment = Alignment.CenterHorizontally)
            {
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color.DarkGray)
            }
            Column(fillMax.weight(0.30f),horizontalAlignment = Alignment.CenterHorizontally)
            {
                Icon(Icons.Filled.AccountCircle, contentDescription = "", modifier = Modifier.fillMaxSize().padding(24.dp))
            }
        }
    }

}







