package com.example.climbingteam.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Refresh
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
import com.example.climbingteam.data.LocationWeather
import com.example.climbingteam.ui.components.*
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.WeatherViewModel

@Composable
fun CompareScreen(
    viewModel: WeatherViewModel,
    onLocationClick: (Int) -> Unit
) {
    val searchQueries by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val selectedLocations by viewModel.selectedLocations.collectAsState()
    val weatherData by viewModel.weatherData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val bestIndex by viewModel.bestLocationIndex.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClimbingColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 80.dp)
        ) {
            // Header with mountain gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1A3A5C),
                                Color(0xFF0F2744),
                                ClimbingColors.background
                            )
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 48.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Landscape,
                            contentDescription = null,
                            tint = ClimbingColors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "MeteoMonta\u00f1a",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refrescar",
                            tint = ClimbingColors.textSecondary
                        )
                    }
                }
            }

            // Comparing header
            val activeCount = weatherData.count { it != null }
            if (activeCount > 0) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "COMPARANDO $activeCount RUTAS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClimbingColors.textTertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // 3 Search bars + Weather cards
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (slot in 0..2) {
                    val weather = weatherData[slot]
                    val slotLabel = when (slot) {
                        0 -> "Lugar 1"
                        1 -> "Lugar 2"
                        else -> "Lugar 3"
                    }

                    AnimatedContent(
                        targetState = weather,
                        transitionSpec = {
                            if (targetState != null) {
                                (slideInVertically(
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
                                    initialOffsetY = { it / 2 }
                                ) + fadeIn(tween(300))) togetherWith
                                (slideOutVertically { -it / 2 } + fadeOut(tween(200)))
                            } else {
                                (fadeIn(tween(200))) togetherWith (fadeOut(tween(200)))
                            }
                        },
                        label = "slotAnim$slot"
                    ) { currentWeather ->
                    if (currentWeather == null) {
                        // Search bar
                        LocationSearchBar(
                            query = searchQueries[slot],
                            onQueryChange = { viewModel.updateSearchQuery(slot, it) },
                            results = searchResults[slot],
                            isSearching = isSearching[slot],
                            onLocationSelected = { viewModel.selectLocation(slot, it) },
                            onClear = { viewModel.clearSlot(slot) },
                            placeholder = "Buscar $slotLabel..."
                        )
                    } else {
                        // Weather card (con onClear integrado)
                        WeatherComparisonCard(
                            weather = currentWeather,
                            isBest = bestIndex == slot,
                            isFavorite = favorites.any { it.id == currentWeather.location.id },
                            onFavoriteToggle = { viewModel.toggleFavorite(currentWeather.location) },
                            onClear = { viewModel.clearSlot(slot) },
                            onClick = { onLocationClick(slot) }
                        )
                    }
                    } // end AnimatedContent
                }
            }

            // ── Comparison analysis panel ──────────────────
            val validWeathers = weatherData.filterNotNull()
            AnimatedVisibility(
                visible = validWeathers.size >= 2,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    initialOffsetY = { it }
                ) + fadeIn(tween(400)),
                exit = fadeOut(tween(200))
            ) {
                if (validWeathers.size >= 2) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Spacer(Modifier.height(16.dp))
                        ClimbingComparisonPanel(
                            weatherData = weatherData,
                            viewModel = viewModel,
                            bestIndex = bestIndex
                        )
                    }
                }
            }

            // Loading overlay
            if (isLoading) {
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ClimbingColors.cardBackground)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = ClimbingColors.primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Cargando datos meteorol\u00f3gicos...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClimbingColors.textSecondary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Comparison Panel ──────────────────────────────────────

private val slotColors = listOf(
    Color(0xFF58A6FF),  // Azul
    Color(0xFF7C3AED),  // Violeta
    Color(0xFFFF6B6B)   // Coral
)

private val categoryLabels = listOf("Temp", "Viento", "Lluvia", "Humedad")
private val categoryIcons = listOf("🌡", "💨", "🌧", "💧")

@Composable
private fun ClimbingComparisonPanel(
    weatherData: Array<LocationWeather?>,
    viewModel: WeatherViewModel,
    bestIndex: Int?
) {
    val bestWeather = bestIndex?.let { weatherData[it] }
    val validEntries = weatherData.mapIndexedNotNull { idx, w ->
        if (w != null) Triple(idx, w, viewModel.getClimbingScores(w)) else null
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ClimbingColors.cardBackground)
            .padding(16.dp)
    ) {
        // ── Header: best location banner ──
        if (bestWeather != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ClimbingColors.optimo.copy(alpha = 0.12f))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EmojiEvents, null,
                    tint = ClimbingColors.optimo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Mejor opci\u00f3n: ${bestWeather.location.name}",
                        style = MaterialTheme.typography.titleSmall,
                        color = ClimbingColors.optimo,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Puntuaci\u00f3n: ${viewModel.getTotalScore(bestWeather)}/100",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.optimo.copy(alpha = 0.7f)
                    )
                }
                // Score badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.optimo.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${viewModel.getTotalScore(bestWeather)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ClimbingColors.optimo
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Legend: color per location ──
        Text(
            "COMPARATIVA DE ESCALADA",
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            validEntries.forEach { (idx, w, scores) ->
                val total = viewModel.getTotalScore(w)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(slotColors[idx])
                    )
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            w.location.name,
                            fontSize = 11.sp,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            "$total pts",
                            fontSize = 10.sp,
                            color = ClimbingColors.textTertiary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Bar chart: 4 categories, grouped bars ──
        for (catIdx in 0..3) {
            val catLabel = categoryLabels[catIdx]
            val catIcon = categoryIcons[catIdx]

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category label
                Text(
                    "$catIcon $catLabel",
                    fontSize = 11.sp,
                    color = ClimbingColors.textSecondary,
                    modifier = Modifier.width(70.dp)
                )

                // Bars column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    validEntries.forEach { (idx, w, scores) ->
                        val score = scores[catIdx]
                        val rawValue = when (catIdx) {
                            0 -> "${w.current?.temperature?.toInt() ?: 0}\u00b0C"
                            1 -> "${w.current?.windSpeed?.toInt() ?: 0} km/h"
                            2 -> "${w.current?.precipitation ?: 0.0} mm"
                            3 -> "${w.current?.humidity?.toInt() ?: 0}%"
                            else -> ""
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Animated bar
                            val animatedWidth by animateFloatAsState(
                                targetValue = score / 100f,
                                animationSpec = tween(800, delayMillis = catIdx * 100),
                                label = "bar_${idx}_$catIdx"
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF1C2128))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(animatedWidth.coerceAtLeast(0.02f))
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    slotColors[idx].copy(alpha = 0.6f),
                                                    slotColors[idx]
                                                )
                                            )
                                        )
                                )
                            }
                            // Value label
                            Text(
                                rawValue,
                                fontSize = 10.sp,
                                color = ClimbingColors.textTertiary,
                                modifier = Modifier.width(52.dp).padding(start = 6.dp)
                            )
                        }
                    }
                }
            }

            if (catIdx < 3) {
                Spacer(Modifier.height(4.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Total score summary row ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1C2128))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            validEntries.forEach { (idx, w, _) ->
                val total = viewModel.getTotalScore(w)
                val isBest = bestIndex == idx
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBest) ClimbingColors.optimo.copy(alpha = 0.2f)
                                else slotColors[idx].copy(alpha = 0.15f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$total",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBest) ClimbingColors.optimo else slotColors[idx]
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        w.location.name,
                        fontSize = 10.sp,
                        color = if (isBest) ClimbingColors.optimo else ClimbingColors.textSecondary,
                        fontWeight = if (isBest) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                    if (isBest) {
                        Text(
                            "\u2b50 Mejor",
                            fontSize = 9.sp,
                            color = ClimbingColors.optimo
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ── Scoring explanation ──
        Text(
            "Rango ideal: 12-22\u00b0C \u00b7 Viento <10km/h \u00b7 Sin lluvia \u00b7 Humedad 30-70%",
            fontSize = 9.sp,
            color = ClimbingColors.textTertiary.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
