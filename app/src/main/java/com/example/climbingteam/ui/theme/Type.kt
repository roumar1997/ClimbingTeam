package com.example.climbingteam.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.climbingteam.R


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



