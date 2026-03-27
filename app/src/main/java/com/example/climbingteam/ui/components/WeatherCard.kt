package com.example.climbingteam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climbingteam.data.ClimbingCondition
import com.example.climbingteam.data.LocationWeather
import com.example.climbingteam.ui.theme.ClimbingColors

@Composable
fun WeatherComparisonCard(
    weather: LocationWeather,
    isBest: Boolean = false,
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onClear: () -> Unit = {},
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val condition = weather.climbingCondition
    val current   = weather.current
    val condColor = Color(condition.colorHex)

    // Outer container: card + a 3dp left accent bar coloured by condition
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = if (isBest) ClimbingColors.cardBackgroundLight else ClimbingColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBest) 6.dp else 2.dp)
    ) {
        // IntrinsicSize.Min lets the accent bar fill the Column's intrinsic height
        Row(modifier = Modifier
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
        ) {

            // ── Condition accent bar ───────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(condColor, condColor.copy(alpha = 0.3f))
                        )
                    )
            )

            Column(modifier = Modifier.padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 10.dp)) {

                // ── Row 1: location name + condition badge ─────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            weather.location.name,
                            style      = MaterialTheme.typography.titleLarge,
                            color      = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines   = 1
                        )
                        if (weather.elevation != null) {
                            Text(
                                "⛰ ${weather.elevation.toInt()} m",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClimbingColors.textTertiary
                            )
                        }
                    }
                    ConditionBadge(condition)
                }

                Spacer(Modifier.height(14.dp))

                // ── Row 2: weather metrics ─────────────────────────────────
                Row(
                    modifier             = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    WeatherMetric(
                        icon  = Icons.Default.Thermostat,
                        value = current?.temperature?.let { "${it.toInt()}°" } ?: "--",
                        label = "Temp",
                        tint  = ClimbingColors.primary
                    )
                    WeatherMetric(
                        icon  = Icons.Default.Air,
                        value = current?.windSpeed?.let { "${it.toInt()} km/h" } ?: "--",
                        label = "Viento",
                        tint  = ClimbingColors.accent
                    )
                    WeatherMetric(
                        icon  = Icons.Default.WaterDrop,
                        value = current?.humidity?.let { "${it.toInt()}%" } ?: "--",
                        label = "Humedad",
                        tint  = Color(0xFF4FC3F7)
                    )
                    WeatherMetric(
                        icon  = Icons.Default.Umbrella,
                        value = current?.precipitation?.let { "$it mm" } ?: "--",
                        label = "Lluvia",
                        tint  = Color(0xFF81C784)
                    )
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = ClimbingColors.divider, thickness = 0.5.dp)
                Spacer(Modifier.height(6.dp))

                // ── Row 3: favourite + change ──────────────────────────────
                Row(
                    modifier             = Modifier.fillMaxWidth(),
                    verticalAlignment    = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier          = Modifier.clickable(onClick = onFavoriteToggle),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector        = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint               = if (isFavorite) Color(0xFFE91E63) else ClimbingColors.textTertiary,
                            modifier           = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text  = if (isFavorite) "Guardado" else "Guardar",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isFavorite) Color(0xFFE91E63) else ClimbingColors.textTertiary
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onClear)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cambiar lugar",
                            tint     = ClimbingColors.textTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Cambiar",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = ClimbingColors.textTertiary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Condition badge ────────────────────────────────────────────────────────────

@Composable
fun ConditionBadge(condition: ClimbingCondition) {
    val color = Color(condition.colorHex)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(
            text       = condition.label,
            style      = MaterialTheme.typography.labelMedium,
            color      = color,
            fontWeight = FontWeight.Bold
        )
    }
}

// ── Metric cell ───────────────────────────────────────────────────────────────

@Composable
fun WeatherMetric(
    icon: ImageVector,
    value: String,
    label: String,
    tint: Color = ClimbingColors.textSecondary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            value,
            style      = MaterialTheme.typography.titleMedium,
            color      = ClimbingColors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(label, style = MaterialTheme.typography.labelSmall, color = ClimbingColors.textTertiary)
    }
}

// ── "Best location" banner ────────────────────────────────────────────────────

@Composable
fun BestLocationBanner(locationName: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        ClimbingColors.optimo.copy(alpha = 0.18f),
                        ClimbingColors.optimo.copy(alpha = 0.06f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.EmojiEvents, null, tint = ClimbingColors.optimo, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                "Mejor opción: $locationName",
                style      = MaterialTheme.typography.titleMedium,
                color      = ClimbingColors.optimo,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Condiciones ideales para escalada",
                style = MaterialTheme.typography.bodySmall,
                color = ClimbingColors.optimo.copy(alpha = 0.75f)
            )
        }
    }
}

// ── Generic tag pill ──────────────────────────────────────────────────────────

@Composable
fun WeatherTag(text: String, color: Color = ClimbingColors.optimo) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style      = MaterialTheme.typography.labelSmall,
            color      = color,
            fontWeight = FontWeight.Medium,
            fontSize   = 11.sp
        )
    }
}
