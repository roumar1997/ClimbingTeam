package com.example.climbingteam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climbingteam.api.OpenMeteoApi
import com.example.climbingteam.data.*
import com.example.climbingteam.ui.components.ConditionBadge
import com.example.climbingteam.ui.components.ReviewSection
import com.example.climbingteam.ui.components.WeatherTag
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.WeatherViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DetailScreen(
    viewModel: WeatherViewModel,
    slotIndex: Int = 0,
    overrideWeather: LocationWeather? = null,
    onBack: () -> Unit,
    onViewProfile: (String) -> Unit = {}
) {
    val weatherData by viewModel.weatherData.collectAsState()
    val weather = overrideWeather ?: weatherData[slotIndex]

    if (weather == null) {
        Box(
            Modifier
                .fillMaxSize()
                .background(ClimbingColors.background),
            contentAlignment = Alignment.Center
        ) {
            Text("Sin datos", color = ClimbingColors.textSecondary)
        }
        return
    }

    val current = weather.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClimbingColors.background)
            .verticalScroll(scrollState)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A3A5C), Color(0xFF0F2744), ClimbingColors.background)
                    )
                )
                .padding(top = 44.dp, bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column {
                // Back + title
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
                        weather.location.name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.weight(1f))
                    ConditionBadge(weather.climbingCondition)
                }

                Spacer(Modifier.height(8.dp))

                // Elevation + description
                if (weather.elevation != null) {
                    Text(
                        "${weather.elevation.toInt()} m",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary,
                        modifier = Modifier.padding(start = 48.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Big temperature
                Row(
                    modifier = Modifier.padding(start = 48.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        "${current?.temperature?.toInt() ?: "--"}",
                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 72.sp),
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "\u00b0C",
                        style = MaterialTheme.typography.headlineMedium,
                        color = ClimbingColors.textSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        getWeatherEmoji(current?.weatherCode),
                        fontSize = 48.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Weather description
                Text(
                    getWeatherDescription(current?.weatherCode),
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClimbingColors.textSecondary,
                    modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                )

                Spacer(Modifier.height(12.dp))

                // Tags
                Row(
                    modifier = Modifier.padding(start = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val wind = current?.windSpeed ?: 0.0
                    val windTag = when {
                        wind < 10 -> "Viento suave"
                        wind < 25 -> "Viento moderado"
                        wind < 40 -> "Viento fuerte"
                        else -> "Viento muy fuerte"
                    }
                    val windColor = when {
                        wind < 15 -> ClimbingColors.optimo
                        wind < 30 -> ClimbingColors.aceptable
                        else -> ClimbingColors.adverso
                    }
                    WeatherTag(windTag, windColor)

                    val humidity = current?.humidity ?: 50.0
                    val humTag = when {
                        humidity < 60 -> "Humedad baja"
                        humidity < 80 -> "Humedad media"
                        else -> "Humedad alta"
                    }
                    WeatherTag(humTag, if (humidity < 75) ClimbingColors.optimo else ClimbingColors.aceptable)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // --- Hourly Forecast ---
        Text(
            "PR\u00d3XIMAS HORAS",
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        // Filter hourly points to show next 24h from current time
        val now = LocalDateTime.now()
        val upcomingHourly = weather.hourlyForecast.filter { hp ->
            try {
                val hpTime = LocalDateTime.parse(hp.time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                hpTime.isAfter(now.minusHours(1))
            } catch (e: Exception) {
                false
            }
        }.take(24)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            upcomingHourly.forEachIndexed { idx, hp ->
                HourlyCard(hourly = hp, isNow = idx == 0)
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Current Conditions Grid ---
        Text(
            "CONDICIONES ACTUALES",
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        // 2x2 grid
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConditionGridItem(
                    icon = Icons.Default.Air,
                    label = "Viento",
                    value = "${current?.windSpeed?.toInt() ?: "--"} km/h",
                    sublabel = directionToCardinal(current?.windDirection),
                    modifier = Modifier.weight(1f)
                )
                ConditionGridItem(
                    icon = Icons.Default.WaterDrop,
                    label = "Humedad",
                    value = "${current?.humidity?.toInt() ?: "--"}%",
                    sublabel = null,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConditionGridItem(
                    icon = Icons.Default.Thermostat,
                    label = "Sens. t\u00e9rmica",
                    value = "${current?.apparentTemperature?.toInt() ?: "--"}\u00b0C",
                    sublabel = null,
                    modifier = Modifier.weight(1f)
                )
                ConditionGridItem(
                    icon = Icons.Default.Umbrella,
                    label = "Precipitaci\u00f3n",
                    value = "${current?.precipitation ?: 0.0} mm",
                    sublabel = null,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ConditionGridItem(
                    icon = Icons.Default.Speed,
                    label = "R\u00e1fagas",
                    value = "${current?.windGusts?.toInt() ?: "--"} km/h",
                    sublabel = null,
                    modifier = Modifier.weight(1f)
                )
                ConditionGridItem(
                    icon = Icons.Default.Cloud,
                    label = "Lluvia prob.",
                    value = run {
                        val nextHour = upcomingHourly.firstOrNull()
                        "${nextHour?.precipProbability?.toInt() ?: "--"}%"
                    },
                    sublabel = null,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Climbing Recommendation ---
        val recommendation = OpenMeteoApi.generateClimbingRecommendation(weather)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when (weather.climbingCondition) {
                        ClimbingCondition.OPTIMO -> ClimbingColors.optimo.copy(alpha = 0.1f)
                        ClimbingCondition.ACEPTABLE -> ClimbingColors.aceptable.copy(alpha = 0.1f)
                        ClimbingCondition.ADVERSO -> ClimbingColors.adverso.copy(alpha = 0.1f)
                    }
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Recomendaci\u00f3n para escalada",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(weather.climbingCondition.colorHex),
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.textSecondary,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // --- Reviews Section ---
        ReviewSection(
            locationId = weather.location.id,
            locationName = weather.location.name,
            modifier = Modifier.padding(horizontal = 16.dp),
            onViewProfile = onViewProfile
        )

        Spacer(Modifier.height(24.dp))

        // --- Daily forecast ---
        Text(
            "PREVISI\u00d3N DIARIA",
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            weather.dailyForecast.forEach { daily ->
                DailyForecastRow(daily)
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun HourlyCard(hourly: HourlyPoint, isNow: Boolean) {
    val time = try {
        val dt = LocalDateTime.parse(hourly.time, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        if (isNow) "Ahora" else dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        hourly.time.takeLast(5)
    }

    Card(
        modifier = Modifier.width(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNow) ClimbingColors.primary.copy(alpha = 0.15f) else ClimbingColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = if (isNow) ClimbingColors.primary else ClimbingColors.textTertiary,
                fontWeight = if (isNow) FontWeight.Bold else FontWeight.Normal
            )
            Spacer(Modifier.height(6.dp))
            Text(
                getWeatherEmoji(hourly.weatherCode),
                fontSize = 20.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "${hourly.temperature?.toInt() ?: "--"}\u00b0",
                style = MaterialTheme.typography.titleMedium,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${hourly.windSpeed?.toInt() ?: "--"} km/h",
                style = MaterialTheme.typography.labelSmall,
                color = ClimbingColors.textTertiary,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
private fun ConditionGridItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    sublabel: String?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = ClimbingColors.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ClimbingColors.textTertiary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            if (sublabel != null) {
                Text(
                    sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary
                )
            }
        }
    }
}

@Composable
private fun DailyForecastRow(daily: DailyPoint) {
    val dayName = try {
        val date = java.time.LocalDate.parse(daily.date)
        val today = java.time.LocalDate.now()
        when {
            date == today -> "Hoy"
            date == today.plusDays(1) -> "Ma\u00f1ana"
            else -> date.format(DateTimeFormatter.ofPattern("EEE d", Locale("es")))
                .replaceFirstChar { it.uppercase() }
        }
    } catch (e: Exception) {
        daily.date
    }

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                dayName,
                style = MaterialTheme.typography.bodyLarge,
                color = ClimbingColors.textPrimary,
                modifier = Modifier.width(80.dp)
            )
            Text(
                getWeatherEmoji(daily.weatherCode),
                fontSize = 18.sp
            )
            Spacer(Modifier.width(12.dp))
            Text(
                "${daily.precipProbMax?.toInt() ?: 0}%",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF4FC3F7),
                modifier = Modifier.width(36.dp)
            )
            Spacer(Modifier.weight(1f))
            Text(
                "${daily.tempMin?.toInt() ?: "--"}\u00b0",
                style = MaterialTheme.typography.bodyMedium,
                color = ClimbingColors.textTertiary
            )
            // Temperature bar
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF4FC3F7), Color(0xFFFFA726))
                        )
                    )
            )
            Text(
                "${daily.tempMax?.toInt() ?: "--"}\u00b0",
                style = MaterialTheme.typography.bodyMedium,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun directionToCardinal(degrees: Double?): String? {
    if (degrees == null) return null
    return when {
        degrees < 22.5 || degrees >= 337.5 -> "Norte (N)"
        degrees < 67.5 -> "Noreste (NE)"
        degrees < 112.5 -> "Este (E)"
        degrees < 157.5 -> "Sureste (SE)"
        degrees < 202.5 -> "Sur (S)"
        degrees < 247.5 -> "Suroeste (SW)"
        degrees < 292.5 -> "Oeste (W)"
        else -> "Noroeste (NW)"
    }
}
