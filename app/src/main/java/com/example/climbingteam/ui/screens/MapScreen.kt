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

@SuppressLint("MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationStatus by remember { mutableStateOf("Sin ubicación") }
    var hasLocation by remember { mutableStateOf(false) }
    var radarFrames by remember { mutableStateOf<List<RadarFrame>>(emptyList()) }
    var currentFrameIdx by remember { mutableIntStateOf(0) }
    var radarOverlay by remember { mutableStateOf<TilesOverlay?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var radarHost by remember { mutableStateOf("") }
    var timestampText by remember { mutableStateOf("Cargando radar...") }
    val scope = rememberCoroutineScope()
    val timelineScrollState = rememberScrollState()

    // CartoDB Dark Matter base URLs
    val cartoUrls = remember {
        arrayOf(
            "https://a.basemaps.cartocdn.com",
            "https://b.basemaps.cartocdn.com",
            "https://c.basemaps.cartocdn.com",
            "https://d.basemaps.cartocdn.com"
        )
    }

    // Configure OSMDroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().apply {
            userAgentValue = "ClimbingTeams/1.0"
            osmdroidTileCache = context.cacheDir
        }
    }

    // Load RainViewer data
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
                    frames.add(RadarFrame(path = f.getString("path"), time = f.getLong("time"), isNowcast = false))
                }
                // Also load nowcast frames if available
                try {
                    val nowcast = data.getJSONObject("radar").getJSONArray("nowcast")
                    for (i in 0 until nowcast.length()) {
                        val f = nowcast.getJSONObject(i)
                        frames.add(RadarFrame(path = f.getString("path"), time = f.getLong("time"), isNowcast = true))
                    }
                } catch (_: Exception) { }

                radarHost = host
                radarFrames = frames
                currentFrameIdx = frames.indexOfLast { !it.isNowcast }.coerceAtLeast(0)
                Log.d("RadarMap", "Loaded ${frames.size} frames (${frames.count { it.isNowcast }} nowcast) from $host")
            } catch (e: Exception) {
                Log.e("RadarMap", "Error loading radar data", e)
                withContext(Dispatchers.Main) { timestampText = "Error al cargar radar" }
            }
        }
    }

    // Animation loop
    LaunchedEffect(isPlaying, radarFrames) {
        if (isPlaying && radarFrames.isNotEmpty()) {
            while (isPlaying) {
                delay(700)
                currentFrameIdx = (currentFrameIdx + 1) % radarFrames.size
            }
        }
    }

    // Scroll timeline to current frame
    LaunchedEffect(currentFrameIdx, radarFrames) {
        if (radarFrames.isNotEmpty()) {
            val frameWidth = 52 // dp approx
            val target = (currentFrameIdx * frameWidth).coerceAtLeast(0)
            timelineScrollState.animateScrollTo(target)
        }
    }

    // Update radar overlay when frame changes
    LaunchedEffect(currentFrameIdx, radarFrames, radarHost) {
        if (radarFrames.isEmpty() || radarHost.isEmpty()) return@LaunchedEffect
        val frame = radarFrames[currentFrameIdx]
        val isLast = currentFrameIdx == radarFrames.indexOfLast { !it.isNowcast }

        // Update timestamp
        val date = java.util.Date(frame.time * 1000)
        val sdf = java.text.SimpleDateFormat("d MMM · HH:mm", java.util.Locale("es"))
        timestampText = when {
            frame.isNowcast -> "📡 Predicción: " + sdf.format(date)
            isLast -> "🔴 Ahora: " + sdf.format(date)
            else -> sdf.format(date)
        }

        val mv = mapView ?: return@LaunchedEffect
        withContext(Dispatchers.Main) {
            radarOverlay?.let { mv.overlays.remove(it) }

            val radarTileSource = object : OnlineTileSourceBase(
                "RainViewer_${frame.time}", 0, 7, 256, ".png", arrayOf(radarHost)
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    val z = MapTileIndex.getZoom(pMapTileIndex)
                    val x = MapTileIndex.getX(pMapTileIndex)
                    val y = MapTileIndex.getY(pMapTileIndex)
                    return "$radarHost${frame.path}/256/$z/$x/$y/6/1_1.png"
                }
            }

            val tileProvider = MapTileProviderBasic(context, radarTileSource)
            val overlay = TilesOverlay(tileProvider, context)
            overlay.loadingBackgroundColor = AndroidColor.TRANSPARENT
            overlay.loadingLineColor = AndroidColor.TRANSPARENT
            // 75% opacity via ColorMatrix
            overlay.setColorFilter(
                android.graphics.ColorMatrixColorFilter(
                    floatArrayOf(
                        1f, 0f, 0f, 0f, 0f,
                        0f, 1f, 0f, 0f, 0f,
                        0f, 0f, 1f, 0f, 0f,
                        0f, 0f, 0f, 0.75f, 0f
                    )
                )
            )
            radarOverlay = overlay
            mv.overlays.add(overlay)
            mv.invalidate()
        }
    }

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

    // ── Full-screen map layout (Windy style) ──────────────────────────────
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0D1117))) {

        // ── Base map ──────────────────────────────────────────────────────
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    // CartoDB Dark Matter tile source
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
                    // No color filter - CartoDB Dark Matter is already Windy-like
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // ── Edge vignette (depth effect like Windy) ───────────────────────
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    colors = listOf(Color.Transparent, Color(0x1A0D1117), Color(0x330D1117)),
                    radius = 1200f
                )
            )
        )

        // ── Floating header (gradient fade from top) ──────────────────────
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

                // Location badge
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

        // ── Precipitation legend (top right, floating) ────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 100.dp, end = 12.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xCC0D1117))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "mm/h",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = ClimbingColors.textTertiary
            )
            Spacer(Modifier.height(4.dp))
            // Vertical gradient legend (Windy color scheme)
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFFFF2222),
                                Color(0xFFFF8C00),
                                Color(0xFFFFFF00),
                                Color(0xFF00C800),
                                Color(0xFF0096FF),
                                Color(0xFF96D2FA)
                            )
                        )
                    )
            )
            Spacer(Modifier.height(2.dp))
            Text("H", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color(0xFFFF2222))
            Text("L", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = Color(0xFF96D2FA))
        }

        // ── Location FAB (bottom right) ────────────────────────────────────
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

        // ── Bottom Windy-style timeline control ───────────────────────────
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
            // Timeline scrubber row
            if (radarFrames.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(timelineScrollState)
                        .padding(horizontal = 60.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    radarFrames.forEachIndexed { idx, frame ->
                        val isCurrent = idx == currentFrameIdx
                        val timeFmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(frame.time * 1000))

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(50.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isCurrent) ClimbingColors.primary.copy(alpha = 0.15f)
                                    else Color.Transparent
                                )
                                .clickable {
                                    isPlaying = false
                                    currentFrameIdx = idx
                                }
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            // Frame indicator bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (isCurrent) 4.dp else 2.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        when {
                                            isCurrent -> ClimbingColors.primary
                                            frame.isNowcast -> ClimbingColors.optimo.copy(alpha = 0.5f)
                                            else -> ClimbingColors.textTertiary.copy(alpha = 0.4f)
                                        }
                                    )
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                timeFmt,
                                fontSize = 9.sp,
                                color = when {
                                    isCurrent -> ClimbingColors.primary
                                    frame.isNowcast -> ClimbingColors.optimo.copy(alpha = 0.8f)
                                    else -> ClimbingColors.textTertiary
                                },
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                            if (frame.isNowcast && isCurrent) {
                                Text("▶", fontSize = 7.sp, color = ClimbingColors.optimo)
                            }
                        }
                    }
                }
            }

            // Control buttons
            Row(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Prev
                IconButton(
                    onClick = {
                        isPlaying = false
                        currentFrameIdx = if (currentFrameIdx > 0) currentFrameIdx - 1
                        else radarFrames.size - 1
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.SkipPrevious, null, tint = ClimbingColors.textPrimary, modifier = Modifier.size(18.dp))
                }

                // Play/Pause (larger)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.primary)
                        .clickable { isPlaying = !isPlaying },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Next
                IconButton(
                    onClick = {
                        isPlaying = false
                        currentFrameIdx = (currentFrameIdx + 1) % radarFrames.size.coerceAtLeast(1)
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.surfaceVariant.copy(alpha = 0.7f))
                ) {
                    Icon(Icons.Default.SkipNext, null, tint = ClimbingColors.textPrimary, modifier = Modifier.size(18.dp))
                }

                Spacer(Modifier.width(8.dp))

                // Nowcast indicator
                if (radarFrames.any { it.isNowcast }) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(ClimbingColors.optimo.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(6.dp).clip(CircleShape)
                                .background(ClimbingColors.optimo)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Predicción",
                            fontSize = 10.sp,
                            color = ClimbingColors.optimo,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Attribution
            Text(
                "© CARTO · © OpenStreetMap · RainViewer",
                fontSize = 8.sp,
                color = ClimbingColors.textTertiary.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 4.dp)
            )
        }
    }
}

data class RadarFrame(val path: String, val time: Long, val isNowcast: Boolean = false)
