package com.example.climbingteam.composables

import android.widget.Button
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Preview (showBackground = true)
@Composable
fun prev(){
 simpleButton(mod = Modifier, color = Color.Red, elevation = 10)
}



@Composable
fun simpleButton(
    mod: Modifier,
    isEnabled: Boolean = true,
    onClick: () -> Unit = {},
    color: Color,
    elevation: Int,
    paddingH: Dp = 16.dp ,
    paddingv: Dp = 16.dp
)
{
    ElevatedButton(
        onClick = onClick,
        modifier = mod,
        enabled = isEnabled,
        colors = ButtonDefaults.elevatedButtonColors(containerColor = color),
        elevation = ButtonDefaults.elevatedButtonElevation(defaultElevation = elevation.dp),
        contentPadding = PaddingValues(horizontal = paddingH , vertical = paddingv ),

    )
    {


    }
}