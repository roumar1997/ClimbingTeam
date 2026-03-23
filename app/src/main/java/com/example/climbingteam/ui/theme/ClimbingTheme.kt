package com.example.climbingteam.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.climbingteam.R

// Colors - Dark theme inspired by Windy.com
object ClimbingColors {
    val background = Color(0xFF0D1117)
    val surface = Color(0xFF161B22)
    val surfaceVariant = Color(0xFF1C2333)
    val cardBackground = Color(0xFF1E2636)
    val cardBackgroundLight = Color(0xFF243044)

    val primary = Color(0xFF58A6FF)
    val primaryVariant = Color(0xFF388BFD)
    val secondary = Color(0xFF3FB950)
    val accent = Color(0xFF79C0FF)

    val textPrimary = Color(0xFFE6EDF3)
    val textSecondary = Color(0xFF8B949E)
    val textTertiary = Color(0xFF6E7681)

    val optimo = Color(0xFF3FB950)
    val aceptable = Color(0xFFFFA657)
    val adverso = Color(0xFFF85149)

    val divider = Color(0xFF21262D)
    val searchBar = Color(0xFF0D1117)
    val searchBarBorder = Color(0xFF30363D)

    val bottomNavBackground = Color(0xFF161B22)
    val bottomNavSelected = Color(0xFF58A6FF)
    val bottomNavUnselected = Color(0xFF6E7681)

    val tagBackground = Color(0xFF1F3A2E)
    val tagBackgroundWind = Color(0xFF1A2A40)
}

private val DarkColorScheme = darkColorScheme(
    primary = ClimbingColors.primary,
    secondary = ClimbingColors.secondary,
    background = ClimbingColors.background,
    surface = ClimbingColors.surface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ClimbingColors.textPrimary,
    onSurface = ClimbingColors.textPrimary,
    surfaceVariant = ClimbingColors.surfaceVariant,
    outline = ClimbingColors.searchBarBorder
)

val RobotoFamily = FontFamily(Font(R.font.roboto))
val OpenSansFamily = FontFamily(Font(R.font.opensans))

val ClimbingTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp,
        color = ClimbingColors.textPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = ClimbingColors.textPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        color = ClimbingColors.textPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = ClimbingColors.textPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        color = ClimbingColors.textPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 16.sp,
        color = ClimbingColors.textPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 14.sp,
        color = ClimbingColors.textSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 12.sp,
        color = ClimbingColors.textTertiary
    ),
    labelLarge = TextStyle(
        fontFamily = OpenSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        color = ClimbingColors.textPrimary
    ),
    labelSmall = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 11.sp,
        color = ClimbingColors.textTertiary
    )
)

@Composable
fun ClimbingTeamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ClimbingTypography,
        content = content
    )
}
