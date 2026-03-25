package com.example.climbingteam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climbingteam.data.ClimbingCondition
import com.example.climbingteam.data.DailyPoint
import com.example.climbingteam.data.SectorResult
import com.example.climbingteam.data.getWeatherDescription
import com.example.climbingteam.data.getWeatherEmoji
import com.example.climbingteam.ui.components.ConditionBadge
import com.example.climbingteam.ui.theme.ClimbingColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SectorDetailScreen(
    result: SectorResult,
    onBack: () -> Unit
) {
    val sector = result.sector
    val scrollState = rememberScrollState()

    // Dominant condition color for the header gradient
    val condColor = when (result.bestCondition) {
        ClimbingCondition.OPTIMO   -> Color(0xFF1B4332)
        ClimbingCondition.ACEPTABLE -> Color(0xFF1A2E05).copy(alpha = 0.8f)
        ClimbingCondition.ADVERSO  -> Color(0xFF3B1519)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClimbingColors.background)
            .verticalScroll(scrollState)
    ) {
        // ── Header ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(condColor, ClimbingColors.background)
                    )
                )
                .padding(top = 44.dp, bottom = 20.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column {
                // Back + title row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = ClimbingColors.textPrimary
                        )
                    }
                    Text(
                        sector.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    ConditionBadge(result.bestCondition)
                }

                Spacer(Modifier.height(8.dp))

                // Location info
                Text(
                    "${sector.ccaa} · ${sector.ubicacion}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.textSecondary,
                    modifier = Modifier.padding(start = 48.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Info chips row
                Row(
                    modifier = Modifier.padding(start = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectorInfoChip(text = sector.estilo, icon = Icons.Default.Terrain)
                    SectorInfoChip(text = sector.roca, icon = Icons.Default.Landscape)
                    if (result.distanceKm != null) {
                        SectorInfoChip(
                            text = "${"%.0f".format(result.distanceKm)} km",
                            icon = Icons.Default.NearMe
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Daily forecast ─────────────────────────────────────────
        if (result.dailyForecast.isNotEmpty()) {
            Text(
                "PREVISIÓN DIARIA",
                style = MaterialTheme.typography.labelSmall,
                color = ClimbingColors.textTertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                result.dailyForecast.forEachIndexed { i, day ->
                    val condition = result.conditions.getOrNull(i) ?: ClimbingCondition.ADVERSO
                    SectorDayCard(day = day, condition = condition)
                }
            }
        } else {
            // No GPS / no forecast
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📍", fontSize = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Sin coordenadas GPS",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "No hay previsión meteorológica disponible\npara este sector.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SectorDayCard(day: DailyPoint, condition: ClimbingCondition) {
    val dayName = try {
        val date = LocalDate.parse(day.date)
        val today = LocalDate.now()
        when {
            date == today            -> "Hoy"
            date == today.plusDays(1) -> "Mañana"
            else -> date.format(DateTimeFormatter.ofPattern("EEE d MMM", Locale("es")))
                .replaceFirstChar { it.uppercase() }
        }
    } catch (_: Exception) { day.date }

    val condColor = when (condition) {
        ClimbingCondition.OPTIMO    -> ClimbingColors.optimo
        ClimbingCondition.ACEPTABLE -> ClimbingColors.aceptable
        ClimbingCondition.ADVERSO   -> ClimbingColors.adverso
    }

    val wind = day.windSpeedMax ?: 0.0
    val isWindy = wind > 30.0
    val precip = day.precipSum ?: 0.0
    val precipProb = day.precipProbMax?.toInt() ?: 0

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ClimbingColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top row: day name + condition badge
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    dayName,
                    style = MaterialTheme.typography.titleSmall,
                    color = ClimbingColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                ConditionBadge(condition)
            }

            Spacer(Modifier.height(10.dp))

            // Middle row: emoji + temp min/max bar + stats
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Weather emoji
                Text(
                    getWeatherEmoji(day.weatherCode),
                    fontSize = 28.sp
                )

                Spacer(Modifier.width(12.dp))

                // Temp range
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${day.tempMin?.toInt() ?: "--"}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClimbingColors.textTertiary
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF4FC3F7), Color(0xFFFFA726))
                                    )
                                )
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "${day.tempMax?.toInt() ?: "--"}°",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        getWeatherDescription(day.weatherCode),
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Bottom row: wind, precip, humidity indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                StatChip(
                    icon = "💨",
                    value = "${wind.toInt()} km/h",
                    highlight = isWindy,
                    highlightColor = Color(0xFF90CAF9)
                )
                StatChip(
                    icon = "🌧",
                    value = "$precipProb%  ${"%.1f".format(precip)}mm",
                    highlight = precip > 1.5 || precipProb > 70,
                    highlightColor = ClimbingColors.adverso
                )
                // Left/right border accent
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(28.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(condColor.copy(alpha = 0.7f))
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    icon: String,
    value: String,
    highlight: Boolean,
    highlightColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (highlight) highlightColor.copy(alpha = 0.12f)
                else ClimbingColors.surfaceVariant.copy(alpha = 0.5f)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(icon, fontSize = 11.sp)
        Text(
            value,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlight) highlightColor else ClimbingColors.textTertiary
        )
    }
}

@Composable
private fun SectorInfoChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ClimbingColors.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            icon, null,
            tint = ClimbingColors.textTertiary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
