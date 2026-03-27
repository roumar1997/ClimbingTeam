package com.example.climbingteam.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.climbingteam.R

// ─────────────────────────────────────────────────────────────────────────────
// ClimbingColors — backed by Compose mutableStateOf so every composable that
// reads a color automatically recomposes when the theme changes.
// ─────────────────────────────────────────────────────────────────────────────

object ClimbingColors {

    // ── Surfaces & backgrounds ────────────────────────────────────────────────
    var background          by mutableStateOf(Color(0xFF0D1117)); private set
    var surface             by mutableStateOf(Color(0xFF161B22)); private set
    var surfaceVariant      by mutableStateOf(Color(0xFF1C2333)); private set
    var cardBackground      by mutableStateOf(Color(0xFF1E2636)); private set
    var cardBackgroundLight by mutableStateOf(Color(0xFF243044)); private set

    // ── Brand / accent ────────────────────────────────────────────────────────
    var primary             by mutableStateOf(Color(0xFF58A6FF)); private set
    var primaryVariant      by mutableStateOf(Color(0xFF388BFD)); private set
    var secondary           by mutableStateOf(Color(0xFF3FB950)); private set
    var accent              by mutableStateOf(Color(0xFF79C0FF)); private set

    // ── Typography ────────────────────────────────────────────────────────────
    var textPrimary         by mutableStateOf(Color(0xFFE6EDF3)); private set
    var textSecondary       by mutableStateOf(Color(0xFF8B949E)); private set
    var textTertiary        by mutableStateOf(Color(0xFF6E7681)); private set

    // ── Status ────────────────────────────────────────────────────────────────
    var optimo              by mutableStateOf(Color(0xFF3FB950)); private set
    var aceptable           by mutableStateOf(Color(0xFFFFA657)); private set
    var adverso             by mutableStateOf(Color(0xFFF85149)); private set

    // ── Borders & decorative ──────────────────────────────────────────────────
    var divider             by mutableStateOf(Color(0xFF21262D)); private set
    var searchBar           by mutableStateOf(Color(0xFF0D1117)); private set
    var searchBarBorder     by mutableStateOf(Color(0xFF30363D)); private set

    // ── Bottom nav ────────────────────────────────────────────────────────────
    var bottomNavBackground by mutableStateOf(Color(0xFF161B22)); private set
    var bottomNavSelected   by mutableStateOf(Color(0xFF58A6FF)); private set
    var bottomNavUnselected by mutableStateOf(Color(0xFF6E7681)); private set

    // ── Tag backgrounds ───────────────────────────────────────────────────────
    var tagBackground       by mutableStateOf(Color(0xFF1F3A2E)); private set
    var tagBackgroundWind   by mutableStateOf(Color(0xFF1A2A40)); private set

    // ── Screen header gradient ────────────────────────────────────────────────
    // Used in every screen's top banner — adapts automatically to dark/light.
    var headerGradientTop   by mutableStateOf(Color(0xFF112240)); private set
    var headerGradientMid   by mutableStateOf(Color(0xFF0A1628)); private set

    // ─────────────────────────────────────────────────────────────────────────
    // Palette application helpers
    // ─────────────────────────────────────────────────────────────────────────

    internal fun applyDark() {
        background          = Color(0xFF0D1117)
        surface             = Color(0xFF161B22)
        surfaceVariant      = Color(0xFF1C2333)
        cardBackground      = Color(0xFF1E2636)
        cardBackgroundLight = Color(0xFF243044)
        primary             = Color(0xFF58A6FF)
        primaryVariant      = Color(0xFF388BFD)
        secondary           = Color(0xFF3FB950)
        accent              = Color(0xFF79C0FF)
        textPrimary         = Color(0xFFE6EDF3)
        textSecondary       = Color(0xFF8B949E)
        textTertiary        = Color(0xFF6E7681)
        optimo              = Color(0xFF3FB950)
        aceptable           = Color(0xFFFFA657)
        adverso             = Color(0xFFF85149)
        divider             = Color(0xFF21262D)
        searchBar           = Color(0xFF0D1117)
        searchBarBorder     = Color(0xFF30363D)
        bottomNavBackground = Color(0xFF161B22)
        bottomNavSelected   = Color(0xFF58A6FF)
        bottomNavUnselected = Color(0xFF6E7681)
        tagBackground       = Color(0xFF1F3A2E)
        tagBackgroundWind   = Color(0xFF1A2A40)
        headerGradientTop   = Color(0xFF112240)
        headerGradientMid   = Color(0xFF0A1628)
    }

    internal fun applyLight() {
        background          = Color(0xFFF0F5FA)
        surface             = Color(0xFFFFFFFF)
        surfaceVariant      = Color(0xFFE8EEF5)
        cardBackground      = Color(0xFFFFFFFF)
        cardBackgroundLight = Color(0xFFF4F8FF)
        primary             = Color(0xFF1565C0)
        primaryVariant      = Color(0xFF0D47A1)
        secondary           = Color(0xFF2E7D32)
        accent              = Color(0xFF1976D2)
        textPrimary         = Color(0xFF0D1117)
        textSecondary       = Color(0xFF424755)
        textTertiary        = Color(0xFF6B7280)
        optimo              = Color(0xFF2E7D32)
        aceptable           = Color(0xFFBF6000)
        adverso             = Color(0xFFC62828)
        divider             = Color(0xFFDDE3EA)
        searchBar           = Color(0xFFEDF1F7)
        searchBarBorder     = Color(0xFFC8D0DA)
        bottomNavBackground = Color(0xFFFFFFFF)
        bottomNavSelected   = Color(0xFF1565C0)
        bottomNavUnselected = Color(0xFF9EA3AE)
        tagBackground       = Color(0xFFD4EDDA)
        tagBackgroundWind   = Color(0xFFD0E4F7)
        headerGradientTop   = Color(0xFF1565C0)
        headerGradientMid   = Color(0xFF1976D2)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Material3 color schemes
// ─────────────────────────────────────────────────────────────────────────────

private val DarkColorScheme = darkColorScheme(
    primary         = Color(0xFF58A6FF),
    secondary       = Color(0xFF3FB950),
    tertiary        = Color(0xFFFFA657),
    background      = Color(0xFF0D1117),
    surface         = Color(0xFF161B22),
    surfaceVariant  = Color(0xFF1C2333),
    onPrimary       = Color.White,
    onSecondary     = Color.White,
    onTertiary      = Color.White,
    onBackground    = Color(0xFFE6EDF3),
    onSurface       = Color(0xFFE6EDF3),
    outline         = Color(0xFF30363D),
    outlineVariant  = Color(0xFF21262D)
)

private val LightColorScheme = lightColorScheme(
    primary         = Color(0xFF1565C0),
    secondary       = Color(0xFF2E7D32),
    tertiary        = Color(0xFFBF6000),
    background      = Color(0xFFF0F5FA),
    surface         = Color(0xFFFFFFFF),
    surfaceVariant  = Color(0xFFE8EEF5),
    onPrimary       = Color.White,
    onSecondary     = Color.White,
    onTertiary      = Color.White,
    onBackground    = Color(0xFF0D1117),
    onSurface       = Color(0xFF0D1117),
    outline         = Color(0xFFC8D0DA),
    outlineVariant  = Color(0xFFDDE3EA)
)

// ─────────────────────────────────────────────────────────────────────────────
// Typography
// ─────────────────────────────────────────────────────────────────────────────

val RobotoFamily   = FontFamily(Font(R.font.roboto))
val OpenSansFamily = FontFamily(Font(R.font.opensans))

val ClimbingTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RobotoFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = OpenSansFamily,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = OpenSansFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp
    ),
    labelMedium = TextStyle(
        fontFamily = OpenSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    ),
    labelSmall = TextStyle(
        fontFamily = OpenSansFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        letterSpacing = 0.4.sp
    )
)

// ─────────────────────────────────────────────────────────────────────────────
// Theme entry point
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ClimbingTeamTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    SideEffect {
        if (darkTheme) ClimbingColors.applyDark() else ClimbingColors.applyLight()
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = ClimbingTypography,
        content     = content
    )
}
