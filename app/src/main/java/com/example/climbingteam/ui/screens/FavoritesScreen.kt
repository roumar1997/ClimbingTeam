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
                        listOf(Color(0xFF1A3A5C), Color(0xFF0F2744), ClimbingColors.background)
                    )
                )
                .padding(top = 48.dp, bottom = 16.dp)
                .padding(horizontal = 20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Favoritos",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Tus lugares guardados para acceso r\u00e1pido",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary
                )
            }
        }

        if (favorites.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        tint = ClimbingColors.textTertiary,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Sin favoritos a\u00fan",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textSecondary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A\u00f1ade lugares desde la pesta\u00f1a Comparar\npulsando el coraz\u00f3n en cada tarjeta",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary,
                        lineHeight = 18.sp
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
