package com.example.climbingteam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.climbingteam.data.GeoLocation
import com.example.climbingteam.ui.theme.ClimbingColors

@Composable
fun LocationSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<GeoLocation>,
    isSearching: Boolean,
    onLocationSelected: (GeoLocation) -> Unit,
    onClear: () -> Unit,
    placeholder: String = "Buscar ciudad o pueblo...",
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.textTertiary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = ClimbingColors.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Limpiar",
                            tint = ClimbingColors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = ClimbingColors.textPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ClimbingColors.searchBar,
                unfocusedContainerColor = ClimbingColors.searchBar,
                focusedBorderColor = ClimbingColors.primary,
                unfocusedBorderColor = ClimbingColors.searchBarBorder,
                cursorColor = ClimbingColors.primary,
                focusedTextColor = ClimbingColors.textPrimary,
                unfocusedTextColor = ClimbingColors.textPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { isFocused = it.isFocused }
        )

        AnimatedVisibility(visible = isFocused && results.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ClimbingColors.cardBackground)
                    .border(1.dp, ClimbingColors.searchBarBorder, RoundedCornerShape(12.dp))
            ) {
                items(results) { location ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLocationSelected(location)
                                focusManager.clearFocus()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = ClimbingColors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                location.name,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ClimbingColors.textPrimary
                            )
                            val subtitle = listOfNotNull(location.province, location.region, location.country)
                                .distinct().take(2).joinToString(", ")
                            if (subtitle.isNotEmpty()) {
                                Text(
                                    subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ClimbingColors.textTertiary
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = ClimbingColors.divider)
                }
            }
        }

        if (isFocused && isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ClimbingColors.cardBackground)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = ClimbingColors.primary,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
