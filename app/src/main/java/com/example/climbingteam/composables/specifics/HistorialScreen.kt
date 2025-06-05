package com.example.climbingteam.composables.specifics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.climbingteam.repository.ConsultaRepository
import com.example.climbingteam.ui.Styles
import com.example.climbingteam.viewmodels.AuthViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

@Composable
fun HistorialScreen(
    navController: NavController,
    vm: AuthViewModel = viewModel()
) {
    val user by vm.user.collectAsState()
    val userId = user?.uid
    var consultas by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(userId) {
        userId?.let {
            ConsultaRepository.obtenerUltimasConsultas(
                userId = it,
                onSuccess = { data -> consultas = data },
                onFailure = { error -> println("Error al cargar historial: $error") }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Styles.color_main)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Historial de consultas", style = Styles.textStyleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(consultas) { item ->
                    ElevatedCard {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Sector: ${item["sector"] ?: "--"}", style = Styles.textStyleMedium)
                            Text("Temperatura: ${item["temperatura"] ?: "--"}", style = Styles.textStyleMedium)
                            Text("Humedad: ${item["humedad"] ?: "--"}", style = Styles.textStyleMedium)
                            Text("Viento: ${item["viento"] ?: "--"}", style = Styles.textStyleMedium)
                            Text("Sensación térmica: ${item["sensacion_termica"] ?: "--"}", style = Styles.textStyleMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            ElevatedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Volver")
            }
        }
    }
}
