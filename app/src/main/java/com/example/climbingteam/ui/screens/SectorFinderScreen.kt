package com.example.climbingteam.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.climbingteam.data.*
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.DisciplineSort
import com.example.climbingteam.viewmodels.SectorViewModel

@SuppressLint("MissingPermission")
@Composable
fun SectorFinderScreen(
    viewModel: SectorViewModel,
    onSectorClick: (SectorResult) -> Unit = {}
) {
    val context = LocalContext.current

    val results by viewModel.sortedResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val loadingTotal by viewModel.loadingTotal.collectAsState()
    val selectedRocas by viewModel.selectedRocas.collectAsState()
    val selectedEstilos by viewModel.selectedEstilos.collectAsState()
    val maxDistanceKm by viewModel.maxDistanceKm.collectAsState()
    val forecastDays by viewModel.forecastDays.collectAsState()
    val conditionFilter by viewModel.conditionFilter.collectAsState()
    val userLat by viewModel.userLat.collectAsState()
    val disciplineSort by viewModel.disciplineSort.collectAsState()

    var filtersExpanded by remember { mutableStateOf(true) }

    // Location
    @SuppressLint("MissingPermission")
    fun getLocation() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val loc = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: Exception) { null }
        if (loc != null) viewModel.setUserLocation(loc.latitude, loc.longitude)
    }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) getLocation()
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
        if (hasPerm) getLocation()
        else permLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClimbingColors.background)
    ) {
        // ── Header ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A3A5C), Color(0xFF0F2744))))
                .padding(top = 48.dp, bottom = 12.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Terrain, null,
                            tint = ClimbingColors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Buscador de sectores",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${results.size} sectores · España",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Location badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (userLat != null)
                            ClimbingColors.optimo.copy(alpha = 0.15f)
                        else
                            ClimbingColors.surfaceVariant
                    ) {
                        Row(
                            Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.MyLocation, null,
                                tint = if (userLat != null) ClimbingColors.optimo else ClimbingColors.textTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (userLat != null) "Ubicación OK" else "Sin ubicación",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (userLat != null) ClimbingColors.optimo else ClimbingColors.textTertiary
                            )
                        }
                    }

                    // Filter toggle
                    IconButton(
                        onClick = { filtersExpanded = !filtersExpanded },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (filtersExpanded) Icons.Default.ExpandLess else Icons.Default.FilterList,
                            null,
                            tint = ClimbingColors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // ── Filters (collapsible) ────────────────────────────────────────────
        AnimatedVisibility(
            visible = filtersExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ClimbingColors.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Estilo
                FilterRow(label = "Estilo") {
                    listOf("Vía", "Bloque").forEach { estilo ->
                        SmallFilterChip(
                            label = estilo,
                            selected = estilo in selectedEstilos,
                            onClick = { viewModel.toggleEstilo(estilo) }
                        )
                    }
                }

                // Roca
                FilterRow(label = "Roca") {
                    viewModel.availableRocas.forEach { roca ->
                        SmallFilterChip(
                            label = roca.take(9),
                            selected = roca in selectedRocas,
                            onClick = { viewModel.toggleRoca(roca) }
                        )
                    }
                }

                // Distancia
                FilterRow(label = "Distancia") {
                    listOf(50.0, 100.0, 150.0, 200.0, 250.0, 300.0, 350.0, 400.0, 450.0, 500.0, null).forEach { km ->
                        SmallFilterChip(
                            label = if (km != null) "${km.toInt()} km" else "∞",
                            selected = maxDistanceKm == km,
                            onClick = { viewModel.setMaxDistance(if (maxDistanceKm == km) null else km) }
                        )
                    }
                    if (userLat == null) {
                        Text(
                            "(necesita ubicación)",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClimbingColors.textTertiary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }

                // Días de previsión
                FilterRow(label = "Días") {
                    listOf(1, 3, 5, 7, 14).forEach { d ->
                        SmallFilterChip(
                            label = "${d}d",
                            selected = forecastDays == d,
                            onClick = { viewModel.setForecastDays(d) }
                        )
                    }
                }

                // Condiciones
                FilterRow(label = "Condición") {
                    SmallFilterChip(
                        label = "Todas",
                        selected = conditionFilter == null,
                        onClick = { viewModel.setConditionFilter(null) }
                    )
                    SmallFilterChip(
                        label = "Óptimo",
                        selected = conditionFilter == ClimbingCondition.OPTIMO,
                        selectedColor = ClimbingColors.optimo,
                        onClick = { viewModel.setConditionFilter(ClimbingCondition.OPTIMO) }
                    )
                    SmallFilterChip(
                        label = "Aceptable+",
                        selected = conditionFilter == ClimbingCondition.ACEPTABLE,
                        selectedColor = ClimbingColors.aceptable,
                        onClick = { viewModel.setConditionFilter(ClimbingCondition.ACEPTABLE) }
                    )
                }

                // Ordenar por disciplina
                FilterRow(label = "Ordenar") {
                    DisciplineSort.values().forEach { disc ->
                        SmallFilterChip(
                            label = "${disc.icon} ${disc.label}",
                            selected = disciplineSort == disc,
                            selectedColor = Color(0xFF7C3AED),
                            onClick = { viewModel.setDisciplineSort(disc) }
                        )
                    }
                }

                // Clear filters button
                val hasFilters = selectedRocas.isNotEmpty() || selectedEstilos.isNotEmpty() ||
                        maxDistanceKm != null || conditionFilter != null
                if (hasFilters) {
                    TextButton(
                        onClick = { viewModel.clearFilters() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Limpiar filtros", fontSize = 12.sp)
                    }
                }
            }
        }

        // ── Loading bar ──────────────────────────────────────────────────────
        if (isLoading) {
            LinearProgressIndicator(
                progress = { if (loadingTotal > 0) loadingProgress.toFloat() / loadingTotal else 0f },
                modifier = Modifier.fillMaxWidth(),
                color = ClimbingColors.primary,
                trackColor = ClimbingColors.surfaceVariant
            )
        } else {
            Divider(color = ClimbingColors.divider, thickness = 1.dp)
        }

        // ── Results ──────────────────────────────────────────────────────────
        if (isLoading && results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = ClimbingColors.primary)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Consultando el tiempo para $loadingProgress/$loadingTotal sectores...",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textSecondary
                    )
                }
            }
        } else {
            // Section headers
            val withWeather = results.filter { it.dailyForecast.isNotEmpty() }
            val withoutWeather = results.filter { it.dailyForecast.isEmpty() }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (withWeather.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Con datos meteorológicos",
                            count = withWeather.size
                        )
                    }
                    itemsIndexed(withWeather, key = { idx, _ -> "w_$idx" }) { _, result ->
                        SectorCard(
                            result = result,
                            forecastDays = forecastDays,
                            disciplineSort = disciplineSort,
                            disciplineScore = viewModel.getBestDisciplineScore(result, disciplineSort),
                            onClick = { onSectorClick(result) }
                        )
                    }
                }

                if (withoutWeather.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Sin coordenadas GPS",
                            count = withoutWeather.size
                        )
                    }
                    itemsIndexed(withoutWeather, key = { idx, _ -> "nw_$idx" }) { _, result ->
                        SectorCard(
                            result = result,
                            forecastDays = forecastDays,
                            disciplineSort = disciplineSort,
                            disciplineScore = 0,
                            onClick = { onSectorClick(result) }
                        )
                    }
                }

                if (results.isEmpty() && !isLoading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🧗", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Sin sectores que coincidan",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ClimbingColors.textSecondary
                                )
                                Text(
                                    "Prueba con otros filtros",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClimbingColors.textTertiary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Section header ───────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
        Text(
            "$count",
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary
        )
    }
}

// ── Sector card ───────────────────────────────────────────────────────────────
@Composable
private fun SectorCard(
    result: SectorResult,
    forecastDays: Int,
    disciplineSort: DisciplineSort = DisciplineSort.GENERAL,
    disciplineScore: Int = 0,
    onClick: () -> Unit = {}
) {
    val sector = result.sector
    val hasWeather = result.dailyForecast.isNotEmpty()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = ClimbingColors.cardBackground,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Name row
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sector.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${sector.ccaa} · ${sector.ubicacion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right side: score + condition badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Discipline score (only show when not GENERAL and has weather)
                    if (hasWeather && disciplineSort != DisciplineSort.GENERAL) {
                        val scoreColor = when {
                            disciplineScore >= 75 -> ClimbingColors.optimo
                            disciplineScore >= 45 -> Color(0xFFFFA726)
                            else                  -> Color(0xFFEF5350)
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(scoreColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "$disciplineScore",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = scoreColor,
                                    lineHeight = 14.sp
                                )
                                Text(
                                    disciplineSort.icon,
                                    fontSize = 8.sp,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }

                    // Best condition badge (only if has weather)
                    if (hasWeather) {
                        ConditionBadge(condition = result.bestCondition)
                    }
                }
            }

            // Badges row: estilo, roca, distance, windy, no-gps
            val hasWindyDays = result.dailyForecast.any { (it.windSpeedMax ?: 0.0) > 30.0 }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoBadge(text = sector.estilo, color = ClimbingColors.primary.copy(alpha = 0.15f), textColor = ClimbingColors.primary)
                InfoBadge(text = sector.roca, color = ClimbingColors.surfaceVariant, textColor = ClimbingColors.textSecondary)
                if (result.distanceKm != null) {
                    InfoBadge(
                        text = "${"%.0f".format(result.distanceKm)} km",
                        color = ClimbingColors.tagBackgroundWind,
                        textColor = ClimbingColors.accent
                    )
                }
                if (hasWindyDays) {
                    val maxWind = result.dailyForecast.maxOfOrNull { it.windSpeedMax ?: 0.0 } ?: 0.0
                    InfoBadge(
                        text = "💨 ${"%.0f".format(maxWind)} km/h",
                        color = Color(0xFF1A2A3A),
                        textColor = Color(0xFF90CAF9)
                    )
                }
                if (!hasWeather) {
                    InfoBadge(
                        text = "Sin GPS",
                        color = Color(0xFF2A1A1A),
                        textColor = ClimbingColors.textTertiary
                    )
                }
            }

            // Weather day strip
            if (hasWeather && result.conditions.isNotEmpty()) {
                WeatherDayStrip(
                    daily = result.dailyForecast,
                    conditions = result.conditions
                )
            }
        }
    }
}

// ── Weather day strip ─────────────────────────────────────────────────────────
@Composable
private fun WeatherDayStrip(daily: List<DailyPoint>, conditions: List<ClimbingCondition>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        daily.forEachIndexed { i, day ->
            val condition = conditions.getOrNull(i) ?: ClimbingCondition.ADVERSO
            val condColor = when (condition) {
                ClimbingCondition.OPTIMO -> ClimbingColors.optimo
                ClimbingCondition.ACEPTABLE -> ClimbingColors.aceptable
                ClimbingCondition.ADVERSO -> ClimbingColors.adverso
            }
            val dateLabel = try {
                val parts = day.date.split("-")
                "${parts[2]}/${parts[1]}"
            } catch (_: Exception) { day.date.takeLast(5) }

            val isWindy = (day.windSpeedMax ?: 0.0) > 30.0
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(condColor.copy(alpha = 0.15f))
                    .border(1.dp, condColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .width(38.dp)
            ) {
                Text(
                    text = getWeatherEmoji(day.weatherCode),
                    fontSize = 14.sp
                )
                Text(
                    text = dateLabel,
                    fontSize = 8.sp,
                    color = ClimbingColors.textTertiary
                )
                if (day.tempMax != null) {
                    Text(
                        text = "${day.tempMax.toInt()}°",
                        fontSize = 9.sp,
                        color = condColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isWindy) {
                    Text(
                        text = "💨${"%.0f".format(day.windSpeedMax)}",
                        fontSize = 8.sp,
                        color = Color(0xFF90CAF9)
                    )
                }
            }
        }
    }
}

// ── Condition badge ───────────────────────────────────────────────────────────
@Composable
private fun ConditionBadge(condition: ClimbingCondition) {
    val (bg, text) = when (condition) {
        ClimbingCondition.OPTIMO -> Pair(ClimbingColors.optimo.copy(alpha = 0.2f), ClimbingColors.optimo)
        ClimbingCondition.ACEPTABLE -> Pair(ClimbingColors.aceptable.copy(alpha = 0.2f), ClimbingColors.aceptable)
        ClimbingCondition.ADVERSO -> Pair(ClimbingColors.adverso.copy(alpha = 0.2f), ClimbingColors.adverso)
    }
    Surface(shape = RoundedCornerShape(20.dp), color = bg) {
        Text(
            condition.label,
            style = MaterialTheme.typography.labelSmall,
            color = text,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// ── Info badge ────────────────────────────────────────────────────────────────
@Composable
private fun InfoBadge(text: String, color: Color, textColor: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            maxLines = 1
        )
    }
}

// ── Filter row helper ─────────────────────────────────────────────────────────
@Composable
private fun FilterRow(
    label: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary,
            modifier = Modifier.width(56.dp)
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

// ── Small filter chip ─────────────────────────────────────────────────────────
@Composable
private fun SmallFilterChip(
    label: String,
    selected: Boolean,
    selectedColor: Color = ClimbingColors.primary,
    onClick: () -> Unit
) {
    val bgColor = if (selected) selectedColor.copy(alpha = 0.2f) else ClimbingColors.surfaceVariant
    val textColor = if (selected) selectedColor else ClimbingColors.textSecondary
    val borderColor = if (selected) selectedColor.copy(alpha = 0.5f) else Color.Transparent

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
