package com.example.climbingteam.composables.generals

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import com.example.climbingteam.ui.Styles.fonts
import com.example.climbingteam.ui.Styles.header_large
import com.example.climbingteam.ui.Styles.text_medium

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
