

// src/main/kotlin/com/example/climbingteam/composables/specifics/ScreenMain.kt
package com.example.climbingteam.composables.specifics

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.climbingteam.Api.ApiConnector
import com.example.climbingteam.Api.Feature
import com.example.climbingteam.Api.jsonApi
import com.example.climbingteam.Api.ObservacionEstacion
import com.example.climbingteam.R
import com.example.climbingteam.composables.generals.TextParrafo
import com.example.climbingteam.drawVerticalScrollbar
import com.example.climbingteam.ui.Styles
import com.example.climbingteam.viewmodels.AuthViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenMain(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    // Inicializamos el JSON de estaciones al entrar en la pantalla
    val context = LocalContext.current
    jsonApi.initData(context)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // —— 1) Estados para datos de localidad 1 ——
    var temp1      by remember { mutableStateOf("--") }
    var humedad1   by remember { mutableStateOf("--") }
    var vientoVel1 by remember { mutableStateOf("--") }
    var vientoDir1 by remember { mutableStateOf("--") }
    var sensT1     by remember { mutableStateOf("--") }
    var horaObs1   by remember { mutableStateOf("--") }

    // —— 2) Estados para datos de localidad 2 ——
    var temp2      by remember { mutableStateOf("--") }
    var humedad2   by remember { mutableStateOf("--") }
    var vientoVel2 by remember { mutableStateOf("--") }
    var vientoDir2 by remember { mutableStateOf("--") }
    var sensT2     by remember { mutableStateOf("--") }
    var horaObs2   by remember { mutableStateOf("--") }

    // —— 3) Estados para la primera y segunda búsqueda/Dropdown ——
    var expanded1        by remember { mutableStateOf(false) }
    var selectedLocal1   by remember { mutableStateOf("") }
    var selectedFeature1 by remember { mutableStateOf<Feature?>(null) }

    var expanded2        by remember { mutableStateOf(false) }
    var selectedLocal2   by remember { mutableStateOf("") }
    var selectedFeature2 by remember { mutableStateOf<Feature?>(null) }

    // —— 4) Estado para mostrar indicador de “Loading” mientras se obtienen las dos estaciones ——
    var loading by remember { mutableStateOf(false) }

    // *** BOX que pinta TODO el fondo de azul y aplica padding de system bars ***
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Styles.color_main)    //  <-- Pintar TODO el fondo
            .systemBarsPadding()              //  <-- Espacio para status bar + nav bar
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                // ———————————————— DRAWER ORIGINAL ————————————————
                ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Column(Modifier.fillMaxHeight()) {
                        // Cabecera con logo
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(0.15f)
                                .background(Styles.color_main_),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.applogo),
                                contentDescription = ""
                            )
                        }

                        // Botón “Cuenta de usuario”
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(0.1f)
                                .background(Styles.color_tertiary),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(onClick = { /* TODO: navegar a “Mi cuenta” */ }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(15.dp))
                                TextParrafo("Cuenta de usuario", Styles.text_large)
                            }
                        }

                        // Botón “Ajustes”
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(0.1f)
                                .background(Styles.color_tertiary),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(onClick = { /* TODO: navegar a “Ajustes” */ }) {
                                Icon(Icons.Filled.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(15.dp))
                                TextParrafo("Ajustes", Styles.text_large)
                            }
                        }

                        // Botón “Logout”
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .weight(0.65f)
                                .background(Styles.color_tertiary),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(onClick = { vm.logout() }) {
                                Icon(Icons.Filled.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(10.dp))
                                TextParrafo("Logout", Styles.text_large)
                            }
                        }
                    }
                }
            }
        ) {
            // ———————————————— CONTENIDO PRINCIPAL ————————————————
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,  // <-- el Scaffold no pinta fondo blanco
                topBar = {
                    TopAppBar(
                        title = {},
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors()
                            .copy(containerColor = Color.Transparent)
                    )
                }
            ) { innerPadding ->
                // — El contenido va dentro de esta Column —
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    // —— A) Logo central superior ——
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.30f),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Image(
                            painter = painterResource(R.drawable.applogo),
                            contentDescription = ""
                        )
                    }

                    // —— B) Dos dropdowns “Busca localidad 1” y “Busca localidad 2” ——
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.35f)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ======== BUSCA LOCALIDAD 1 ========
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = "Busca localidad 1",
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            TextField(
                                value = selectedLocal1,
                                onValueChange = { s ->
                                    selectedLocal1 = s
                                    if (!expanded1) expanded1 = true
                                },
                                label = { Text("") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(FocusRequester()),
                                trailingIcon = {
                                    androidx.compose.material3.ExposedDropdownMenuDefaults
                                        .TrailingIcon(expanded1)
                                }
                            )
                            DropdownMenu(
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .fillMaxWidth(),
                                expanded = expanded1,
                                onDismissRequest = { expanded1 = false },
                                properties = PopupProperties(focusable = false)
                            ) {
                                val scroll1 = rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(scroll1)
                                        .drawVerticalScrollbar(scroll1)
                                ) {
                                    Column {
                                        jsonApi.estaciones.features.forEach { feature ->
                                            if (feature.properties.NOMBRE
                                                    .lowercase(Locale.ROOT)
                                                    .contains(selectedLocal1.lowercase(Locale.ROOT))
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(feature.properties.NOMBRE) },
                                                    onClick = {
                                                        selectedLocal1 = feature.properties.NOMBRE
                                                        selectedFeature1 = feature
                                                        expanded1 = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // ======== BUSCA LOCALIDAD 2 ========
                        Column(modifier = Modifier.weight(0.5f)) {
                            Text(
                                text = "Busca localidad 2",
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            TextField(
                                value = selectedLocal2,
                                onValueChange = { s ->
                                    selectedLocal2 = s
                                    if (!expanded2) expanded2 = true
                                },
                                label = { Text("") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(FocusRequester()),
                                trailingIcon = {
                                    androidx.compose.material3.ExposedDropdownMenuDefaults
                                        .TrailingIcon(expanded2)
                                }
                            )
                            DropdownMenu(
                                modifier = Modifier
                                    .heightIn(max = 200.dp)
                                    .fillMaxWidth(),
                                expanded = expanded2,
                                onDismissRequest = { expanded2 = false },
                                properties = PopupProperties(focusable = false)
                            ) {
                                val scroll2 = rememberScrollState()
                                Box(
                                    modifier = Modifier
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(scroll2)
                                        .drawVerticalScrollbar(scroll2)
                                ) {
                                    Column {
                                        jsonApi.estaciones.features.forEach { feature ->
                                            if (feature.properties.NOMBRE
                                                    .lowercase(Locale.ROOT)
                                                    .contains(selectedLocal2.lowercase(Locale.ROOT))
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text(feature.properties.NOMBRE) },
                                                    onClick = {
                                                        selectedLocal2 = feature.properties.NOMBRE
                                                        selectedFeature2 = feature
                                                        expanded2 = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // —— C) Botón “Comparar tiempo” centralizado ——
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.10f),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ElevatedButton(
                            onClick = {
                                // —— Al pulsar “Comparar tiempo”, activamos loading y lanzamos corrutina ——
                                loading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    // ————— LOCALIDAD 1 —————
                                    selectedFeature1?.let { f1 ->
                                        val id1 = f1.properties.INDICATIVO
                                        val obs1: ObservacionEstacion? =
                                            jsonApi.consultarObservacionConvencional(id1, ApiConnector.apiKey)

                                        if (obs1 == null) {
                                            launch(Dispatchers.Main) {
                                                temp1 = "--"
                                                humedad1 = "--"
                                                vientoVel1 = "--"
                                                vientoDir1 = "--"
                                                sensT1 = "--"
                                                horaObs1 = "Sin datos"
                                            }
                                        } else {
                                            val t1 = obs1.getTempDouble()
                                                ?.let { t -> String.format("%.1f", t) + " °C" }
                                                ?: "--"
                                            val h1 = obs1.getHumedadDouble()
                                                ?.let { h -> "$h %" }
                                                ?: "--"
                                            val vV1 = obs1.getVelVientoDouble()
                                                ?.let { v -> String.format("%.1f", v) + " km/h" }
                                                ?: "--"
                                            val vD1 = obs1.getDirVientoCardinal()
                                                ?: obs1.getDirVientoGrados()?.let { d -> "${d.toInt()}°" }
                                                ?: "--"
                                            val s1 = obs1.getSensacionTermica()
                                                ?.let { s -> String.format("%.1f", s) + " °C" }
                                                ?: "--"
                                            val ho1 = obs1.getFechaDateTime()
                                                ?.toLocalTime()?.toString() ?: "--"

                                            launch(Dispatchers.Main) {
                                                temp1 = t1
                                                humedad1 = h1
                                                vientoVel1 = vV1
                                                vientoDir1 = vD1
                                                sensT1 = s1
                                                horaObs1 = ho1
                                            }
                                        }
                                    }

                                    // ————— LOCALIDAD 2 —————
                                    selectedFeature2?.let { f2 ->
                                        val id2 = f2.properties.INDICATIVO
                                        val obs2: ObservacionEstacion? =
                                            jsonApi.consultarObservacionConvencional(id2, ApiConnector.apiKey)

                                        if (obs2 == null) {
                                            launch(Dispatchers.Main) {
                                                temp2 = "--"
                                                humedad2 = "--"
                                                vientoVel2 = "--"
                                                vientoDir2 = "--"
                                                sensT2 = "--"
                                                horaObs2 = "Sin datos"
                                            }
                                        } else {
                                            val t2 = obs2.getTempDouble()
                                                ?.let { t -> String.format("%.1f", t) + " °C" }
                                                ?: "--"
                                            val h2 = obs2.getHumedadDouble()
                                                ?.let { h -> "$h %" }
                                                ?: "--"
                                            val vV2 = obs2.getVelVientoDouble()
                                                ?.let { v -> String.format("%.1f", v) + " km/h" }
                                                ?: "--"
                                            val vD2 = obs2.getDirVientoCardinal()
                                                ?: obs2.getDirVientoGrados()?.let { d -> "${d.toInt()}°" }
                                                ?: "--"
                                            val s2 = obs2.getSensacionTermica()
                                                ?.let { s -> String.format("%.1f", s) + " °C" }
                                                ?: "--"
                                            val ho2 = obs2.getFechaDateTime()
                                                ?.toLocalTime()?.toString() ?: "--"

                                            launch(Dispatchers.Main) {
                                                temp2 = t2
                                                humedad2 = h2
                                                vientoVel2 = vV2
                                                vientoDir2 = vD2
                                                sensT2 = s2
                                                horaObs2 = ho2
                                            }
                                        }
                                    }

                                    // —— Tras descargar ambas, desactivamos loading ——
                                    launch(Dispatchers.Main) {
                                        loading = false
                                    }
                                }
                            }
                        ) {
                            Text("Comparar tiempo")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // —— D) Dos tarjetas con los resultados uno al lado del otro ——
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.25f)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // —— TARJETA IZQUIERDA —— (Localidad 1)
                        ElevatedCard(
                            modifier = Modifier
                                .weight(0.48f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                TextParrafo("Hora 1: $horaObs1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Temp 1: $temp1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Humedad 1: $humedad1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Viento 1: $vientoVel1 ($vientoDir1)", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Sens. Térm 1: $sensT1", Styles.text_medium)
                            }
                        }

                        // —— TARJETA DERECHA —— (Localidad 2)
                        ElevatedCard(
                            modifier = Modifier
                                .weight(0.48f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                TextParrafo("Hora 2: $horaObs2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Temp 2: $temp2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Humedad 2: $humedad2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Viento 2: $vientoVel2 ($vientoDir2)", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Sens. Térm 2: $sensT2", Styles.text_medium)
                            }
                        }
                    }

                    // —— E) Relleno final para empujar contenido hacia arriba si sobra espacio ——
                    Spacer(modifier = Modifier.weight(1f))
                }

                // —— Overlay de “Loading” que aparece por encima de todo el contenido mientras loading == true …—
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)), // Semitransparente oscuro
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MainBotbar() {
    // (No hemos modificado esta parte; tu BottomAppBar permanece igual)
}
