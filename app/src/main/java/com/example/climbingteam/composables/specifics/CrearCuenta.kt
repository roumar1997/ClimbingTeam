package com.example.climbingteam.composables.specifics

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.climbingteam.R

import com.example.climbingteam.ui.Mods.backMain
import com.example.climbingteam.ui.Mods.fillMax

class CrearCuenta {


    @Preview
    @Composable
    fun ScreenLoading() {
        var usuario = ""
        var nombre = ""
        var correo = ""

        Column(fillMax.padding(16.dp).then(backMain)) {
            Row(
                fillMax.weight(0.2f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Image(painter = painterResource(R.drawable.applogo), contentDescription = "")

            }
            Row(
                modifier = fillMax.weight(0.8f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Top
            ) {
                Column {

                    OutlinedTextField(
                        value = usuario,
                        onValueChange = { nombre = it },
                        label = { Text("usuario", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Contraseña", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.White
                        )
                    )
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("correo", color = Color.White) },
                        textStyle = TextStyle(color = Color.White),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.LightGray,
                            cursorColor = Color.White
                        )
                    )


                    Button(
                        onClick = {

                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Crear cuenta", color = Color(0xFF1976D2))
                    }
                }

            }
        }

    }
}







