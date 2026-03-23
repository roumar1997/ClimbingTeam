package com.example.climbingteam.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
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

@SuppressLint("SetJavaScriptEnabled", "MissingPermission")
@Composable
fun MapScreen() {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var locationStatus by remember { mutableStateOf("Cargando...") }
    var hasLocation by remember { mutableStateOf(false) }

    fun getAndSendLocation(wv: WebView?) {
        if (wv == null) return
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location: Location? = try {
            lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        } catch (_: Exception) { null }

        if (location != null) {
            val lat = location.latitude
            val lon = location.longitude
            wv.post { wv.evaluateJavascript("if(typeof setUserLocation==='function')setUserLocation($lat,$lon);", null) }
            hasLocation = true
            locationStatus = "Tu ubicaci\u00f3n"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
            getAndSendLocation(webViewRef)
        else locationStatus = "Sin permiso"
    }

    LaunchedEffect(Unit) {
        val ok = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (!ok) permissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // Read HTML once
    val radarHtml = remember {
        try {
            context.assets.open("radar.html").bufferedReader().readText()
        } catch (_: Exception) { null }
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
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, tint = ClimbingColors.primary, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Radar de lluvia", style = MaterialTheme.typography.headlineSmall,
                            color = ClimbingColors.textPrimary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(2.dp))
                    Text("Tiempo real \u00b7 Actualiza cada 5 min",
                        style = MaterialTheme.typography.bodySmall, color = ClimbingColors.textTertiary)
                }
                Surface(shape = RoundedCornerShape(20.dp),
                    color = if (hasLocation) ClimbingColors.optimo.copy(alpha = 0.15f) else ClimbingColors.surfaceVariant
                ) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MyLocation, null,
                            tint = if (hasLocation) ClimbingColors.optimo else ClimbingColors.textTertiary,
                            modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(locationStatus, style = MaterialTheme.typography.labelSmall,
                            color = if (hasLocation) ClimbingColors.optimo else ClimbingColors.textTertiary)
                    }
                }
            }
        }

        if (radarHtml != null) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            cacheMode = WebSettings.LOAD_DEFAULT
                            @Suppress("DEPRECATION")
                            allowUniversalAccessFromFileURLs = true
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onConsoleMessage(msg: ConsoleMessage?): Boolean {
                                Log.d("RadarWebView", "${msg?.message()} -- line ${msg?.lineNumber()}")
                                return true
                            }
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                getAndSendLocation(view)
                            }
                            override fun shouldOverrideUrlLoading(v: WebView?, req: WebResourceRequest?) = false
                        }
                        setBackgroundColor(android.graphics.Color.parseColor("#0D1117"))
                        // Load HTML with HTTPS base URL so CDN scripts and fetch() work
                        loadDataWithBaseURL(
                            "https://api.rainviewer.com/",
                            radarHtml,
                            "text/html",
                            "UTF-8",
                            null
                        )
                        webViewRef = this
                    }
                },
                update = { webViewRef = it },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error al cargar radar", color = ClimbingColors.textTertiary)
            }
        }
    }
}
