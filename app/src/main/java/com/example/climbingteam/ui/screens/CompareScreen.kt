package com.example.climbingteam.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
    val forecastDays by viewModel.forecastDays.collectAsState()

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
                    .height(140.dp)
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.Top
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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

                    Spacer(Modifier.height(8.dp))

                    // Forecast days selector
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Previsi\u00f3n:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClimbingColors.textTertiary
                        )
                        listOf(3, 7, 10, 14).forEach { days ->
                            FilterChip(
                                selected = forecastDays == days,
                                onClick = { viewModel.setForecastDays(days) },
                                label = {
                                    Text(
                                        "${days}d",
                                        fontSize = 11.sp,
                                        fontWeight = if (forecastDays == days) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ClimbingColors.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = ClimbingColors.primary,
                                    containerColor = ClimbingColors.surfaceVariant,
                                    labelColor = ClimbingColors.textTertiary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = ClimbingColors.primary.copy(alpha = 0.3f),
                                    enabled = true,
                                    selected = forecastDays == days
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
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

            // Best location recommendation
            val bestWeather = bestIndex?.let { weatherData[it] }
            AnimatedVisibility(
                visible = bestWeather != null,
                enter = slideInVertically(
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    initialOffsetY = { it }
                ) + fadeIn(tween(400)),
                exit = fadeOut(tween(200))
            ) {
                if (bestWeather != null) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        BestLocationBanner(
                            locationName = bestWeather.location.name,
                            modifier = Modifier.padding(horizontal = 16.dp)
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
