package com.example.climbingteam.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.climbingteam.R
import androidx.compose.ui.text.TextStyle

object Styles {


    val color_main = Color(0xFF64B5F6)
    val color_main_ = Color(0xFF7AABD3)

    val color_secondary = Color(0xFFF3F8F8)
    val  color_secondary_ = Color(0xFF000000)

    val color_tertiary = Color(0xFF90CAF9)
    val color_tertiary_ = Color(0xFF8B0000)


    val fonts = mapOf(
        "header" to R.font.roboto,
        "text" to R.font.opensans
    )

    val fontsObjects= mapOf(

        "header" to FontFamily( Font( fonts.get("header")!!)) ,
        "text" to FontFamily(Font(fonts.get("text")!!))
    )
    val header_xlarge = 25.sp
    val header_large = 20.sp
    val header_medium = 18.sp
    val header_short = 15.sp
    val text_large = 14.sp
    val text_medium = 12.sp
    val text_short= 10.sp






    val textStyleXlarge = TextStyle(
        fontSize = header_xlarge,
        fontFamily = fontsObjects["header"]
    )

    val textStyleLarge = TextStyle(
        fontSize = header_large,
        fontFamily = fontsObjects["header"]
    )

    val textStyleMedium = TextStyle(
        fontSize = text_medium,
        fontFamily = fontsObjects["text"]
    )

    val textStyleSmall = TextStyle(
        fontSize = text_short,
        fontFamily = fontsObjects["text"]
    )

}