package com.example.climbingteam.composables.generals

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun simpleButton(
    mod: Modifier,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
    color: Color,
    elevation: Int,
    paddingH: Dp = 16.dp,
    paddingv: Dp = 16.dp,
    text : String = ""
) {
    ElevatedButton(
        onClick = onClick,
        modifier = mod,
        enabled = isEnabled,
        colors = ButtonDefaults.elevatedButtonColors(containerColor = color),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = elevation.dp),
        contentPadding = PaddingValues(horizontal = paddingH, vertical = paddingv)
    )
    {
        Text(text = text)

    }
}