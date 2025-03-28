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
import androidx.compose.ui.unit.dp
import com.example.climbingteam.composables.generals.simpleButton
import com.example.climbingteam.ui.theme.fonts
import com.example.climbingteam.ui.theme.fontsObjects

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun prev()
{
    Column(modifier = Modifier.fillMaxSize(),horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center)
    {
       TextHeader("hola como estas 1")
       TextParrafo("hola como estas 1")
    }

}

@Composable
fun TextHeader( txt : String)
{
    Text( text = txt,
          //  fontFamily =  FontFamily( Font( fonts.get("header")!!))
        )
}
@Composable
fun TextParrafo( txt : String)
{
    Text( text = txt,
        //fontFamily = FontFamily(Font(fonts.get("text")!!))
    )
}

