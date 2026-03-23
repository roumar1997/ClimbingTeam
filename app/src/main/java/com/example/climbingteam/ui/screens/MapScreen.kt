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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.climbingteam.ui.theme.ClimbingColors
import kotlinx.coroutines.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
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
    var locationStatus by remember { mutableStateOf("Cargando...") }
    var hasLocation by remember { mutableStateOf(false) }
    var radarFrames by remember { mutableStateOf<List<RadarFrame>>(emptyList()) }
    var currentFrameIdx by remember { mutableIntStateOf(0) }
    var radarOverlay by remember { mutableStateOf<TilesOverlay?>(null) }
    var isPlaying by remember { mutableStateOf(true) }
    var radarHost by remember { mutableStateOf("") }
    var timestampText by remember { mutableStateOf("Cargando radar...") }
    val scope = rememberCoroutineScope()

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
                val json = URL("https://api.rainviewer.com/public/weather-maps.json")
                    .readText()
                val data = JSONObject(json)
                val host = data.getString("host")
                val past = data.getJSONObject("radar").getJSONArray("past")
                val frames = mutableListOf<RadarFrame>()
                for (i in 0 until past.length()) {
                    val frame = past.getJSONObject(i)
                    frames.add(RadarFrame(
                        path = frame.getString("path"),
                        time = frame.getLong("time")
                    ))
                }
                radarHost = host
                radarFrames = frames
                currentFrameIdx = frames.size - 1
                Log.d("RadarMap", "Loaded ${frames.size} frames from $host")
            } catch (e: Exception) {
                Log.e("RadarMap", "Error loading radar data", e)
                withContext(Dispatchers.Main) {
                    timestampText = "Error al cargar radar"
                }
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

    // Update radar overlay when frame changes
    LaunchedEffect(currentFrameIdx, radarFrames, radarHost) {
        if (radarFrames.isEmpty() || radarHost.isEmpty()) return@LaunchedEffect
        val frame = radarFrames[currentFrameIdx]
        val isLast = currentFrameIdx == radarFrames.size - 1

        // Update timestamp
        val date = java.util.Date(frame.time * 1000)
        val sdf = java.text.SimpleDateFormat("d MMM HH:mm", java.util.Locale("es"))
        timestampText = (if (isLast) "🔴 Ahora: " else "") + sdf.format(date)

        // Update radar tiles
        val mv = mapView ?: return@LaunchedEffect
        withContext(Dispatchers.Main) {
            // Remove old radar overlay
            radarOverlay?.let { mv.overlays.remove(it) }

            // Create new radar tile source for this frame
            val radarTileSource = object : OnlineTileSourceBase(
                "RainViewer",
                0, 7, 256, ".png",
                arrayOf(radarHost)
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    val z = MapTileIndex.getZoom(pMapTileIndex)
                    val x = MapTileIndex.getX(pMapTileIndex)
                    val y = MapTileIndex.getY(pMapTileIndex)
                    return "$radarHost${frame.path}/256/$z/$x/$y/2/1_1.png"
                }
            }

            val tileProvider = MapTileProviderBasic(context, radarTileSource)
            val overlay = TilesOverlay(tileProvider, context)
            overlay.loadingBackgroundColor = AndroidColor.TRANSPARENT
            overlay.loadingLineColor = AndroidColor.TRANSPARENT
            overlay.setColorFilter(null)

            radarOverlay = overlay
            mv.overlays.add(overlay)
            mv.invalidate()
        }
    }

    fun getAndSendLocation() {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: Exception) { null }

        if (location != null) {
            val gp = GeoPoint(location.latitude, location.longitude)
            mapView?.controller?.apply {
                animateTo(gp)
                setZoom(7.0)
            }
            // Add marker
            mapView?.let { mv ->
                val marker = Marker(mv)
                marker.position = gp
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                marker.title = "Tu ubicación"
                // Remove old user markers
                mv.overlays.removeAll { it is Marker }
                mv.overlays.add(marker)
                mv.invalidate()
            }
            hasLocation = true
            locationStatus = "Tu ubicación"
        } else {
            locationStatus = "Sin ubicación GPS"
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

    Column(
        modifier = Modifier.fillMaxSize().background(ClimbingColors.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A3A5C), Color(0xFF0F2744))))
                .padding(top = 48.dp, bottom = 12.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = ClimbingColors.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Radar de lluvia",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Tiempo real · RainViewer",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (hasLocation) ClimbingColors.optimo.copy(alpha = 0.15f) else ClimbingColors.surfaceVariant
                ) {
                    Row(
                        Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MyLocation, null,
                            tint = if (hasLocation) ClimbingColors.optimo else ClimbingColors.textTertiary,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(locationStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hasLocation) ClimbingColors.optimo else ClimbingColors.textTertiary)
                    }
                }
            }
        }

        // Map
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        controller.setZoom(6.0)
                        controller.setCenter(GeoPoint(40.4, -3.7))
                        // Dark tint on base tiles
                        overlayManager.tilesOverlay.setColorFilter(
                            android.graphics.ColorMatrixColorFilter(
                                floatArrayOf(
                                    -1f, 0f, 0f, 0f, 255f,  // R
                                    0f, -1f, 0f, 0f, 255f,   // G
                                    0f, 0f, -1f, 0f, 255f,   // B
                                    0f, 0f, 0f, 1f, 0f       // A
                                )
                            )
                        )
                        mapView = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Bottom controls overlay
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .background(
                        Color(0xE60D1117),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Previous frame
                IconButton(
                    onClick = { isPlaying = false; currentFrameIdx = if (currentFrameIdx > 0) currentFrameIdx - 1 else radarFrames.size - 1 },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.SkipPrevious, null, tint = ClimbingColors.primary, modifier = Modifier.size(20.dp))
                }

                // Play/Pause
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null,
                        tint = ClimbingColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Next frame
                IconButton(
                    onClick = { isPlaying = false; currentFrameIdx = (currentFrameIdx + 1) % radarFrames.size.coerceAtLeast(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.SkipNext, null, tint = ClimbingColors.primary, modifier = Modifier.size(20.dp))
                }

                // Timestamp
                Text(
                    timestampText,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textPrimary,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Legend
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 8.dp, end = 8.dp)
                    .background(Color(0xE60D1117), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Text("Precipitación", style = MaterialTheme.typography.labelSmall, color = ClimbingColors.textTertiary)
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(6.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF00BFFF),
                                    Color(0xFF00FF7F),
                                    Color(0xFFFFFF00),
                                    Color(0xFFFF8C00),
                                    Color(0xFFFF2222)
                                )
                            ),
                            RoundedCornerShape(3.dp)
                        )
                )
                Spacer(Modifier.height(2.dp))
                Row(Modifier.width(80.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ligera", style = MaterialTheme.typography.labelSmall.copy(fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)), color = ClimbingColors.textTertiary)
                    Text("Intensa", style = MaterialTheme.typography.labelSmall.copy(fontSize = androidx.compose.ui.unit.TextUnit(8f, androidx.compose.ui.unit.TextUnitType.Sp)), color = ClimbingColors.textTertiary)
                }
            }
        }
    }
}

data class RadarFrame(val path: String, val time: Long)
