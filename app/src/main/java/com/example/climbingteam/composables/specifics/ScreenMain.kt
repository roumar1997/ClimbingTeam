// src/main/kotlin/com/example/climbingteam/composables/specifics/ScreenMain.kt
package com.example.climbingteam.composables.specifics

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.climbingteam.api.ApiConnector
import com.example.climbingteam.api.Feature
import com.example.climbingteam.api.jsonApi
import com.example.climbingteam.api.ObservacionEstacion
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
    // iniciamos el json al entrar a la pantalla
    val context = LocalContext.current
    jsonApi.initData(context)

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    //estados localidad 1
    var temp1 by remember { mutableStateOf("--") }
    var humedad1 by remember { mutableStateOf("--") }
    var vientoVel1 by remember { mutableStateOf("--") }
    var vientoDir1 by remember { mutableStateOf("--") }
    var sensT1 by remember { mutableStateOf("--") }
    var horaObs1 by remember { mutableStateOf("--") }

    //estados localidad 2
    var temp2 by remember { mutableStateOf("--") }
    var humedad2 by remember { mutableStateOf("--") }
    var vientoVel2 by remember { mutableStateOf("--") }
    var vientoDir2 by remember { mutableStateOf("--") }
    var sensT2 by remember { mutableStateOf("--") }
    var horaObs2 by remember { mutableStateOf("--") }

    //estados para la primera y segunda busqueda
    var expanded1 by remember { mutableStateOf(false) }
    var selectedLocal1 by remember { mutableStateOf("") }
    var selectedFeature1 by remember { mutableStateOf<Feature?>(null) }

    var expanded2 by remember { mutableStateOf(false) }
    var selectedLocal2 by remember { mutableStateOf("") }
    var selectedFeature2 by remember { mutableStateOf<Feature?>(null) }

    //loadin para mientras busca los datos
    var loading by remember { mutableStateOf(false) }

    // scroll para que sea dsplazable
    val scrollState = rememberScrollState()


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Styles.color_main)
            .systemBarsPadding()
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {

                ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.5f)) {
                    Column(Modifier.fillMaxSize()) {
                        // Cabecera con logo
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .background(Styles.color_main_),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(R.drawable.applogo),
                                contentDescription = ""
                            )
                        }

                        //boton cuenta usuario
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Styles.color_tertiary),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(onClick = { /*cuenta de usuario*/ }) {
                                Icon(Icons.Filled.AccountCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(15.dp))
                                TextParrafo("Cuenta de usuario", Styles.text_large)
                            }
                        }

                        // Botón Ajustes
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .background(Styles.color_tertiary),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ElevatedButton(onClick = { /* navegar a “Ajustes” */ }) {
                                Icon(Icons.Filled.Settings, contentDescription = null)
                                Spacer(modifier = Modifier.width(15.dp))
                                TextParrafo("Ajustes", Styles.text_large)
                            }
                        }

                        // Boton Logout
                        Row(
                            Modifier
                                .fillMaxWidth()
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
            //contenido principal
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(innerPadding)
                        .padding(vertical = 16.dp) // espacio arriba/abajo
                ) {
                    //logo supertior
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(R.drawable.applogo),
                            contentDescription = ""
                        )
                    }

                    //dropdowns localidad 1 y localidad 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //localidad 1 buscar
                        Column(modifier = Modifier.weight(1f)) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(FocusRequester()),
                                trailingIcon = {
                                    androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded1
                                    )
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
                                Column(
                                    Modifier
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(scroll1)
                                        .drawVerticalScrollbar(scroll1)
                                ) {
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

                        //espacio entre los dropdown
                        Spacer(modifier = Modifier.width(16.dp))

                        //localidad2
                        Column(modifier = Modifier.weight(1f)) {
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(FocusRequester()),
                                trailingIcon = {
                                    androidx.compose.material3.ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded2
                                    )
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
                                Column(
                                    Modifier
                                        .heightIn(max = 200.dp)
                                        .verticalScroll(scroll2)
                                        .drawVerticalScrollbar(scroll2)
                                ) {
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

                    Spacer(modifier = Modifier.height(24.dp))

                    //boton para comparar el tiempo de las dos localidades
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ElevatedButton(
                            onClick = {
                                //activamos loading y courutinas al darle al comparar tiempo
                                loading = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    //localidad1 conexion apiKey
                                    selectedFeature1?.let { f1 ->
                                        val id1 = f1.properties.INDICATIVO
                                        val obs1: ObservacionEstacion? =
                                            jsonApi.obtenerObservacionHastaExito(
                                                id1,
                                                ApiConnector.apiKey
                                            )

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
                                                ?: obs1.getDirVientoGrados()
                                                    ?.let { d -> "${d.toInt()}°" }
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

                                    //localidad2
                                    selectedFeature2?.let { f2 ->
                                        val id2 = f2.properties.INDICATIVO
                                        val obs2: ObservacionEstacion? =
                                            jsonApi.obtenerObservacionHastaExito(
                                                id2,
                                                ApiConnector.apiKey
                                            )

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
                                                ?: obs2.getDirVientoGrados()
                                                    ?.let { d -> "${d.toInt()}°" }
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

                                    //descargamos ambas y activamos el cargando
                                    launch(Dispatchers.Main) {
                                        loading = false

                                        //guardar consulta en firestore
                                        val userId = vm.user.value?.uid
                                        if (userId != null) {
                                            val datosConsulta = mapOf(
                                                "sector" to "${selectedLocal1} & ${selectedLocal2}",
                                                "temperatura" to "$temp1 / $temp2",
                                                "humedad" to "$humedad1 / $humedad2",
                                                "viento" to "$vientoVel1 $vientoDir1 / $vientoVel2 $vientoDir2",
                                                "sensacion_termica" to "$sensT1 / $sensT2",
                                                "timestamp" to com.google.firebase.Timestamp.now()
                                            )

                                            com.example.climbingteam.repository.ConsultaRepository
                                                .guardarConsulta(datosConsulta)
                                        }
                                    }
                                }
                            }
                        ) {
                            Text("Comparar tiempo")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    //boton historial
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ElevatedButton(onClick = {
                            navController.navigate("historial")
                        }) {
                            Text("Ver historial")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    //las dos tarjetas con los resultados uno al lado de otro
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //izq localidad 1
                        ElevatedCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                TextParrafo("Hora 1: $horaObs1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Temp 1: $temp1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Humedad 1: $humedad1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo(
                                    "Viento 1: $vientoVel1 ($vientoDir1)",
                                    Styles.text_medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Sens. Térm 1: $sensT1", Styles.text_medium)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }

                        //derecha localidad 2
                        ElevatedCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                TextParrafo("Hora 2: $horaObs2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Temp 2: $temp2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Humedad 2: $humedad2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo(
                                    "Viento 2: $vientoVel2 ($vientoDir2)",
                                    Styles.text_medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                TextParrafo("Sens. Térm 2: $sensT2", Styles.text_medium)
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(24.dp))
                }

                //overlay de el loading
                if (loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)), // semitransparente
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
            }
        }
    }
}


