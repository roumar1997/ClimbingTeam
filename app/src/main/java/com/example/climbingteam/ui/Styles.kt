package com.example.climbingteam.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.example.climbingteam.R

object Styles {

    val color_main = Color(0xFF64B5F6)
    val color_main_ = Color(0xFFFFFF00)

    val color_secondary = Color(0xFFF3F8F8)
    val  color_secondary_ = Color(0xFF000000)

    val color_tertiary = Color(0xFF11E6E6)
    val color_tertiary_ = Color(0xFF8B0000)


    val fonts = mapOf(
        "header" to R.font.roboto,
        "text" to R.font.opensans
    )

    val fontsObjects= mapOf(

        "header" to FontFamily( Font( fonts.get("header")!!)) ,
        "text" to FontFamily(Font(fonts.get("text")!!))
    )

    val header_large = 20.sp
    val header_medium = 18.sp
    val header_short = 15.sp
    val text_large = 14.sp
    val text_medium = 12.sp
    val text_short= 10.sp

}