package com.example.climbingteam.composables

import android.widget.Button
import android.widget.EditText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.climbingteam.composables.generals.simpleButton
import com.example.climbingteam.ui.theme.fonts
import com.example.climbingteam.ui.theme.fontsObjects
import com.example.climbingteam.ui.theme.header_large
import com.example.climbingteam.ui.theme.header_medium
import com.example.climbingteam.ui.theme.text_medium

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun prev()
{
    Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)
    {
       TextHeader("hola como estas 1")
       //Spacer(modifier = Modifier.height(16.dp))
       TextParrafo("hola como estas 1")
    }

}

@Composable
fun TextHeader( txt : String, textSize : TextUnit = header_large *2)
{
    Text( text = txt,
          fontFamily =  FontFamily( Font( fonts.get("header")!!)),
            fontSize = textSize
        )
}
@Composable
fun TextParrafo( txt : String, textSize : TextUnit = text_medium)
{
    Text( text = txt,
        fontFamily = FontFamily(Font(fonts.get("text")!!)),
        fontSize = textSize
        )
}

