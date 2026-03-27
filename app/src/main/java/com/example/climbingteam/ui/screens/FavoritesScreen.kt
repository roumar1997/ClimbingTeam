package com.example.climbingteam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climbingteam.data.SavedLocation
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.WeatherViewModel

@Composable
fun FavoritesScreen(
    viewModel: WeatherViewModel,
    onLoadFavorite: (SavedLocation, Int) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClimbingColors.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(ClimbingColors.headerGradientTop, ClimbingColors.headerGradientMid, ClimbingColors.background)
                    )
                )
                .padding(top = 52.dp, bottom = 20.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE91E63).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint     = Color(0xFFFF4F8B),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Favoritos",
                        style      = MaterialTheme.typography.headlineSmall,
                        color      = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${favorites.size} lugar${if (favorites.size == 1) "" else "es"} guardado${if (favorites.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                }
            }
        }

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Big illustrated icon
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(ClimbingColors.cardBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🏔", fontSize = 48.sp)
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(
                        "Sin favoritos aún",
                        style      = MaterialTheme.typography.titleLarge,
                        color      = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Guarda tus zonas favoritas desde\nla pestaña Comparar pulsando ❤️",
                        style      = MaterialTheme.typography.bodyMedium,
                        color      = ClimbingColors.textTertiary,
                        lineHeight = 20.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(favorites) { location ->
                    FavoriteLocationCard(
                        location = location,
                        onLoad = { slot -> onLoadFavorite(location, slot) },
                        onRemove = { viewModel.removeFavorite(location.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteLocationCard(
    location: SavedLocation,
    onLoad: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var showSlotPicker by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = ClimbingColors.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        location.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        location.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary
                    )
                    if (location.elevation != null) {
                        Text(
                            "${location.elevation.toInt()} m",
                            style = MaterialTheme.typography.labelSmall,
                            color = ClimbingColors.textTertiary
                        )
                    }
                }

                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = ClimbingColors.adverso,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Load buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (slot in 0..2) {
                    OutlinedButton(
                        onClick = { onLoad(slot) },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ClimbingColors.primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, ClimbingColors.primary.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "Lugar ${slot + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
