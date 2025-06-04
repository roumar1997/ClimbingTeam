package com.example.climbingteam

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

fun Modifier.drawVerticalScrollbar(scrollState: ScrollState): Modifier = this.then(
    Modifier.drawBehind {
        val scrollbarWidth = 4.dp.toPx()
        val scrollbarHeight = size.height * size.height / scrollState.maxValue.coerceAtLeast(1)
        val scrollbarOffset = size.height * scrollState.value / scrollState.maxValue.coerceAtLeast(1)

        drawRoundRect(
            color = Color.Gray,
            topLeft = Offset(size.width - scrollbarWidth, scrollbarOffset),
            size = Size(scrollbarWidth, scrollbarHeight),
            cornerRadius = CornerRadius(scrollbarWidth / 2, scrollbarWidth / 2)
        )
    }
)
