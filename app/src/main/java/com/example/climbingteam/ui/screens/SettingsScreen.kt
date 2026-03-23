package com.example.climbingteam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.AuthViewModel
import com.example.climbingteam.viewmodels.WeatherViewModel

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    weatherViewModel: WeatherViewModel,
    onLogout: () -> Unit,
    onNavigateToProfile: () -> Unit = {}
) {
    val user by authViewModel.user.collectAsState()
    val forecastDays by weatherViewModel.forecastDays.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClimbingColors.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A3A5C), Color(0xFF0F2744), ClimbingColors.background)
                    )
                )
                .padding(top = 48.dp, bottom = 16.dp)
                .padding(horizontal = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        tint = ClimbingColors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Ajustes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // User info card - clickable to profile
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onNavigateToProfile() },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = ClimbingColors.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Mi perfil",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        user?.email ?: "No identificado",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary
                    )
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = ClimbingColors.textTertiary
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Forecast days setting
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "D\u00edas de previsi\u00f3n",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClimbingColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "N\u00famero de d\u00edas que se muestran en la previsi\u00f3n diaria",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 7, 10, 14).forEach { days ->
                        FilterChip(
                            selected = forecastDays == days,
                            onClick = { weatherViewModel.setForecastDays(days) },
                            label = { Text("$days d\u00edas") },
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
                            )
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Info section
        SettingsItem(
            icon = Icons.Default.Info,
            title = "Datos meteorol\u00f3gicos",
            subtitle = "Proporcionados por Open-Meteo (API gratuita)"
        )
        SettingsItem(
            icon = Icons.Default.Landscape,
            title = "MeteoMonta\u00f1a v2.0",
            subtitle = "Aplicaci\u00f3n de meteorolog\u00eda para escalada"
        )

        Spacer(Modifier.height(24.dp))

        // Logout button
        Button(
            onClick = {
                authViewModel.logout()
                onLogout()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClimbingColors.adverso.copy(alpha = 0.15f),
                contentColor = ClimbingColors.adverso
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.Logout,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesi\u00f3n", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = ClimbingColors.textTertiary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClimbingColors.textPrimary
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary
                )
            }
        }
    }
}
