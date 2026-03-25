package com.example.climbingteam.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.location.LocationManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.climbingteam.ui.theme.ClimbingColors
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.TilesOverlay
import org.osmdroid.tileprovider.MapTileProviderBasic
import java.net.URL
import org.json.JSONObject

data class RadarFrame(val path: String, val time: Long)

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationStatus by remember { mutableStateOf("Sin ubicación") }
    var hasLocation by remember { mutableStateOf(false) }

    // Radar state
    var radarHost by remember { mutableStateOf("") }
    var radarFrames by remember { mutableStateOf<List<RadarFrame>>(emptyList()) }
    var currentFrameIdx by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var timestampText by remember { mutableStateOf("Cargando radar...") }

    // We keep a map of overlays: frameIndex -> TilesOverlay
    val radarOverlays = remember { mutableMapOf<Int, TilesOverlay>() }
    var owmOverlay by remember { mutableStateOf<TilesOverlay?>(null) }
    var selectedLayer by remember { mutableStateOf("precipitation_new") }
    val scope = rememberCoroutineScope()

    val cartoUrls = remember {
        arrayOf(
            "https://a.basemaps.cartocdn.com",
            "https://b.basemaps.cartocdn.com",
            "https://c.basemaps.cartocdn.com",
            "https://d.basemaps.cartocdn.com"
        )
    }
    val OWM_KEY = "e654fdb6e3791c8f7675db2561ae2b5f"

    val layerOptions = remember {
        listOf(
            "precipitation_new" to "🌧 Lluvia",
            "temp_new"          to "🌡 Temp",
            "clouds_new"        to "☁ Nubes",
            "wind_new"          to "💨 Viento",
            "pressure_new"      to "🌊 Presión"
        )
    }

    // Legend data: list of (color, label) for each OWM layer
    data class LegendStop(val color: Color, val label: String)

    val legendData = remember {
        mapOf(
            "precipitation_new" to listOf(
                LegendStop(Color(0x00000000), "0"),
                LegendStop(Color(0xFF9BF3F0), "0.5"),
                LegendStop(Color(0xFF00B4D8), "1"),
                LegendStop(Color(0xFF0077B6), "2"),
                LegendStop(Color(0xFF2DC653), "4"),
                LegendStop(Color(0xFFFFD60A), "10"),
                LegendStop(Color(0xFFFFA500), "20"),
                LegendStop(Color(0xFFFF4500), "40"),
                LegendStop(Color(0xFFDC143C), "100"),
                LegendStop(Color(0xFF8B008B), "200 mm")
            ),
            "temp_new" to listOf(
                LegendStop(Color(0xFF9915DB), "-40"),
                LegendStop(Color(0xFF4A24DB), "-30"),
                LegendStop(Color(0xFF2050EB), "-20"),
                LegendStop(Color(0xFF21A0E8), "-10"),
                LegendStop(Color(0xFF43D0C8), "0"),
                LegendStop(Color(0xFF6BD86B), "10"),
                LegendStop(Color(0xFFCDE838), "20"),
                LegendStop(Color(0xFFF5B800), "25"),
                LegendStop(Color(0xFFFF6600), "30"),
                LegendStop(Color(0xFFFF0000), "40 °C")
            ),
            "clouds_new" to listOf(
                LegendStop(Color(0x00FFFFFF), "0"),
                LegendStop(Color(0x33FFFFFF), "10"),
                LegendStop(Color(0x55FFFFFF), "25"),
                LegendStop(Color(0x88FFFFFF), "50"),
                LegendStop(Color(0xAAC8C8C8), "75"),
                LegendStop(Color(0xDD969696), "100 %")
            ),
            "wind_new" to listOf(
                LegendStop(Color(0xFFFFFFFF), "1"),
                LegendStop(Color(0xFFAEF1F9), "5"),
                LegendStop(Color(0xFF96C7EC), "15"),
                LegendStop(Color(0xFF6BB3E0), "25"),
                LegendStop(Color(0xFF4E8EC4), "50"),
                LegendStop(Color(0xFF3467A8), "100"),
                LegendStop(Color(0xFFFFE978), "150"),
                LegendStop(Color(0xFFFF6600), "200 m/s")
            ),
            "pressure_new" to listOf(
                LegendStop(Color(0xFF0000CC), "950"),
                LegendStop(Color(0xFF2255CC), "980"),
                LegendStop(Color(0xFF44AACC), "1000"),
                LegendStop(Color(0xFF66DDAA), "1010"),
                LegendStop(Color(0xFFCCEE66), "1020"),
                LegendStop(Color(0xFFFFCC00), "1030"),
                LegendStop(Color(0xFFFF6600), "1040"),
                LegendStop(Color(0xFFFF0000), "1070 hPa")
            )
        )
    }

    // Add/replace OWM background layer (colorful full-coverage weather)
    fun applyWeatherLayer(layer: String) {
        val mv = mapView ?: return
        // Remove old OWM overlay if present
        owmOverlay?.let { mv.overlays.remove(it) }

        val owmTileSource = object : OnlineTileSourceBase(
            "OWM_$layer", 0, 12, 256, ".png",
            arrayOf("https://tile.openweathermap.org")
        ) {
            override fun getTileURLString(pMapTileIndex: Long): String {
                val z = MapTileIndex.getZoom(pMapTileIndex)
                val x = MapTileIndex.getX(pMapTileIndex)
                val y = MapTileIndex.getY(pMapTileIndex)
                return "https://tile.openweathermap.org/map/$layer/$z/$x/$y.png?appid=$OWM_KEY"
            }
        }
        val provider = MapTileProviderBasic(context, owmTileSource)
        val newOverlay = TilesOverlay(provider, context)
        newOverlay.loadingBackgroundColor = AndroidColor.TRANSPARENT
        newOverlay.loadingLineColor = AndroidColor.TRANSPARENT
        // Insert at position 0 (right above base tiles, below radar)
        mv.overlays.add(0, newOverlay)
        owmOverlay = newOverlay
        mv.invalidate()
    }

    // Apply weather layer when selected or map ready
    LaunchedEffect(selectedLayer, mapView) {
        if (mapView != null) {
            withContext(Dispatchers.Main) { applyWeatherLayer(selectedLayer) }
        }
    }

    // Init osmdroid config
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = "ClimbingTeams/1.0"
            osmdroidTileCache = context.cacheDir
        }
    }

    // Load RainViewer API data
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val json = URL("https://api.rainviewer.com/public/weather-maps.json").readText()
                val data = JSONObject(json)
                val host = data.getString("host")
                val frames = mutableListOf<RadarFrame>()
                val past = data.getJSONObject("radar").getJSONArray("past")
                for (i in 0 until past.length()) {
                    val f = past.getJSONObject(i)
                    frames.add(RadarFrame(path = f.getString("path"), time = f.getLong("time")))
                }
                radarHost = host
                radarFrames = frames
                currentFrameIdx = (frames.size - 1).coerceAtLeast(0)
                Log.d("RadarMap", "Loaded ${frames.size} frames from $host")
            } catch (e: Exception) {
                Log.e("RadarMap", "Error loading radar", e)
                withContext(Dispatchers.Main) { timestampText = "Error al cargar radar" }
            }
        }
    }

    // Show the current frame overlay on the map, hide all others
    fun showRadarFrame(idx: Int) {
        val mv = mapView ?: return
        if (radarFrames.isEmpty() || radarHost.isEmpty()) return
        val frame = radarFrames[idx]

        // Update timestamp
        val date = java.util.Date(frame.time * 1000)
        val sdf = java.text.SimpleDateFormat("d MMM · HH:mm", java.util.Locale("es"))
        val isLast = idx == radarFrames.size - 1
        timestampText = if (isLast) "🔴 Ahora: ${sdf.format(date)}" else sdf.format(date)

        // Hide ALL existing radar overlays
        radarOverlays.values.forEach { overlay ->
            overlay.setEnabled(false)
        }

        // Create overlay for this frame if not cached
        if (!radarOverlays.containsKey(idx)) {
            val tileSource = object : OnlineTileSourceBase(
                "RainViewer_$idx", 0, 7, 256, ".png", arrayOf(radarHost)
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    val z = MapTileIndex.getZoom(pMapTileIndex)
                    val x = MapTileIndex.getX(pMapTileIndex)
                    val y = MapTileIndex.getY(pMapTileIndex)
                    return "$radarHost${frame.path}/512/$z/$x/$y/2/1_1.png"
                }
            }
            val provider = MapTileProviderBasic(context, tileSource)
            val overlay = TilesOverlay(provider, context)
            overlay.loadingBackgroundColor = AndroidColor.TRANSPARENT
            overlay.loadingLineColor = AndroidColor.TRANSPARENT
            radarOverlays[idx] = overlay
            mv.overlays.add(overlay)
        }

        // Show the current frame
        radarOverlays[idx]?.setEnabled(true)
        mv.invalidate()
    }

    // When frame index changes, update the visible overlay
    LaunchedEffect(currentFrameIdx, radarFrames, radarHost) {
        if (radarFrames.isNotEmpty() && radarHost.isNotEmpty() && mapView != null) {
            withContext(Dispatchers.Main) {
                showRadarFrame(currentFrameIdx)
            }
        }
    }

    // Animation loop
    LaunchedEffect(isPlaying, radarFrames) {
        if (isPlaying && radarFrames.isNotEmpty()) {
            while (isPlaying) {
                delay(600)
                currentFrameIdx = (currentFrameIdx + 1) % radarFrames.size
            }
        }
    }

    // Pre-cache nearby frames
    LaunchedEffect(currentFrameIdx, radarFrames) {
        if (radarFrames.isNotEmpty() && mapView != null) {
            // Pre-create overlays for next 2 frames
            withContext(Dispatchers.Main) {
                for (offset in 1..2) {
                    val nextIdx = (currentFrameIdx + offset) % radarFrames.size
                    if (!radarOverlays.containsKey(nextIdx)) {
                        val frame = radarFrames[nextIdx]
                        val tileSource = object : OnlineTileSourceBase(
                            "RainViewer_$nextIdx", 0, 7, 256, ".png", arrayOf(radarHost)
                        ) {
                            override fun getTileURLString(pMapTileIndex: Long): String {
                                val z = MapTileIndex.getZoom(pMapTileIndex)
                                val x = MapTileIndex.getX(pMapTileIndex)
                                val y = MapTileIndex.getY(pMapTileIndex)
                                return "$radarHost${frame.path}/512/$z/$x/$y/2/1_1.png"
                            }
                        }
                        val provider = MapTileProviderBasic(context, tileSource)
                        val overlay = TilesOverlay(provider, context)
                        overlay.loadingBackgroundColor = AndroidColor.TRANSPARENT
                        overlay.loadingLineColor = AndroidColor.TRANSPARENT
                        overlay.setEnabled(false)
                        radarOverlays[nextIdx] = overlay
                        mapView?.overlays?.add(overlay)
                    }
                }
            }
        }
    }

    // GPS
    @SuppressLint("MissingPermission")
    fun getAndSendLocation() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: Exception) { null }

        if (location != null) {
            val gp = GeoPoint(location.latitude, location.longitude)
            mapView?.controller?.apply { animateTo(gp); setZoom(7.0) }
            mapView?.let { mv ->
                val marker = Marker(mv)
                marker.position = gp
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.title = "Mi ubicación"
                mv.overlays.removeAll { it is Marker }
                mv.overlays.add(marker)
                mv.invalidate()
            }
            hasLocation = true
            locationStatus = "${String.format("%.2f", location.latitude)}°N"
        } else {
            locationStatus = "Sin GPS"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
            getAndSendLocation()
        else locationStatus = "Sin permiso"
    }

    LaunchedEffect(Unit) {
        val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!hasPerm) permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        ) else getAndSendLocation()
    }

    // ── UI Layout ──────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {

        // ── Base map (CartoDB Dark Matter) ──────────────────
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    val darkTileSource = object : OnlineTileSourceBase(
                        "CartoDB.DarkMatter", 0, 19, 256, ".png", cartoUrls
                    ) {
                        override fun getTileURLString(pMapTileIndex: Long): String {
                            val z = MapTileIndex.getZoom(pMapTileIndex)
                            val x = MapTileIndex.getX(pMapTileIndex)
                            val y = MapTileIndex.getY(pMapTileIndex)
                            val sub = cartoUrls[(x.toInt() + y.toInt()).and(3)]
                            return "$sub/dark_all/$z/$x/$y.png"
                        }
                    }
                    setTileSource(darkTileSource)
                    setMultiTouchControls(true)
                    controller.setZoom(6.0)
                    controller.setCenter(GeoPoint(40.4, -3.7))
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Floating header ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xDD0D1117), Color(0x880D1117), Color.Transparent)
                    )
                )
                .padding(top = 48.dp, bottom = 24.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (radarFrames.isEmpty()) ClimbingColors.textTertiary
                                    else ClimbingColors.adverso
                                )
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Radar · RainViewer",
                            style = MaterialTheme.typography.titleMedium,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        timestampText,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (hasLocation) ClimbingColors.optimo.copy(alpha = 0.15f)
                            else ClimbingColors.surfaceVariant.copy(alpha = 0.6f)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MyLocation, null,
                        tint = if (hasLocation) ClimbingColors.optimo else ClimbingColors.textTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        locationStatus,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (hasLocation) ClimbingColors.optimo else ClimbingColors.textTertiary
                    )
                }
            }
        }

        // ── Layer selector chips ─────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 110.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            layerOptions.forEach { (layerId, label) ->
                val isSelected = selectedLayer == layerId
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) ClimbingColors.primary.copy(alpha = 0.25f)
                            else Color(0xCC161B22)
                        )
                        .border(
                            1.dp,
                            if (isSelected) ClimbingColors.primary.copy(alpha = 0.6f)
                            else Color(0xFF30363D),
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedLayer = layerId }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        label,
                        fontSize = 12.sp,
                        color = if (isSelected) ClimbingColors.primary else ClimbingColors.textSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        // ── Color legend ─────────────────────────────────────
        val currentLegend = legendData[selectedLayer] ?: emptyList()
        if (currentLegend.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xCC0D1117))
                    .border(1.dp, Color(0xFF30363D), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                currentLegend.forEach { stop ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp, 10.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(stop.color)
                                .then(
                                    if (stop.color.alpha < 0.1f)
                                        Modifier.border(0.5.dp, Color(0xFF30363D), RoundedCornerShape(2.dp))
                                    else Modifier
                                )
                        )
                        Text(
                            stop.label,
                            fontSize = 9.sp,
                            color = ClimbingColors.textSecondary,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        // ── Location FAB ───────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xCC161B22))
                .border(1.dp, ClimbingColors.primary.copy(alpha = 0.3f), CircleShape)
                .clickable { getAndSendLocation() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.MyLocation, null,
                tint = if (hasLocation) ClimbingColors.primary else ClimbingColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        // ── Bottom controls ────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color(0xCC0D1117), Color(0xDD0D1117))
                    )
                )
                .padding(bottom = 16.dp, top = 12.dp)
        ) {
            // Play controls
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = {
                        isPlaying = false
                        currentFrameIdx = if (currentFrameIdx > 0) currentFrameIdx - 1
                        else radarFrames.size - 1
                    },
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(ClimbingColors.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.SkipPrevious, null, tint = ClimbingColors.textPrimary, modifier = Modifier.size(18.dp))
                }

                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape)
                        .background(ClimbingColors.primary).clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null, tint = Color.White, modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = {
                        isPlaying = false
                        currentFrameIdx = (currentFrameIdx + 1) % radarFrames.size.coerceAtLeast(1)
                    },
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .background(ClimbingColors.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.SkipNext, null, tint = ClimbingColors.textPrimary, modifier = Modifier.size(18.dp))
                }

                Spacer(Modifier.width(8.dp))

                // Frame counter
                if (radarFrames.isNotEmpty()) {
                    Text(
                        "${currentFrameIdx + 1}/${radarFrames.size}",
                        fontSize = 11.sp,
                        color = ClimbingColors.textTertiary
                    )
                }
            }

            Text(
                "© CARTO · © OSM · RainViewer · OWM",
                fontSize = 8.sp,
                color = ClimbingColors.textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )
        }
    }
}
