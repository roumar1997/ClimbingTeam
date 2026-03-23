package com.example.climbingteam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.climbingteam.data.ClimbingReview
import com.example.climbingteam.data.ROCK_TYPES
import com.example.climbingteam.repository.ReviewRepository
import com.example.climbingteam.ui.theme.ClimbingColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReviewSection(
    locationId: Long,
    locationName: String,
    modifier: Modifier = Modifier,
    onViewProfile: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showForm by remember { mutableStateOf(false) }
    var reviews by remember { mutableStateOf<List<ClimbingReview>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun loadReviews() {
        scope.launch {
            isLoading = true
            reviews = ReviewRepository.getReviewsForLocation(locationId)
            isLoading = false
        }
    }

    // Load reviews when expanded
    LaunchedEffect(expanded) { if (expanded) loadReviews() }

    Column(modifier = modifier) {
        // Toggle header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.RateReview, null,
                        tint = ClimbingColors.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Rese\u00f1as del sector",
                            style = MaterialTheme.typography.titleMedium,
                            color = ClimbingColors.textPrimary, fontWeight = FontWeight.SemiBold)
                        if (reviews.isNotEmpty()) {
                            val avg = reviews.map { it.rating }.average()
                            Text("${reviews.size} rese\u00f1as \u00b7 ${"%.1f".format(avg)} \u2605",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClimbingColors.textTertiary)
                        }
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = ClimbingColors.textTertiary
                )
            }
        }

        // Expandable content
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Add review button
                OutlinedButton(
                    onClick = { showForm = !showForm },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ClimbingColors.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ClimbingColors.primary.copy(alpha = 0.3f))
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (showForm) "Cancelar" else "Escribir rese\u00f1a")
                }

                // Review form
                AnimatedVisibility(visible = showForm, enter = expandVertically(), exit = shrinkVertically()) {
                    ReviewForm(
                        locationId = locationId,
                        locationName = locationName,
                        onSubmitted = {
                            showForm = false
                            loadReviews()
                        }
                    )
                }

                // Loading
                if (isLoading) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = ClimbingColors.primary, strokeWidth = 2.dp)
                    }
                }

                // Review list
                reviews.forEach { review ->
                    ReviewCard(review, onViewProfile = onViewProfile)
                }

                if (!isLoading && reviews.isEmpty()) {
                    Box(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ClimbingColors.surfaceVariant)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("A\u00fan no hay rese\u00f1as. \u00a1S\u00e9 el primero!",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClimbingColors.textTertiary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewForm(
    locationId: Long,
    locationName: String,
    onSubmitted: () -> Unit
) {
    var sectorName by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(0) }
    var rockType by remember { mutableStateOf("") }
    var rockQuality by remember { mutableIntStateOf(3) }
    var processionary by remember { mutableIntStateOf(0) }
    var beginner by remember { mutableStateOf(false) }
    var intermediate by remember { mutableStateOf(false) }
    var advanced by remember { mutableStateOf(false) }
    var expert by remember { mutableStateOf(false) }
    var comment by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var rockTypeExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Nueva reseña",
                style = MaterialTheme.typography.titleMedium,
                color = ClimbingColors.textPrimary, fontWeight = FontWeight.Bold)

            // ── Sector name ──────────────────────────
            OutlinedTextField(
                value = sectorName,
                onValueChange = { sectorName = it },
                label = { Text("Sector de escalada") },
                placeholder = { Text("Ej: Sector La Visera, Vía del Arco...", color = ClimbingColors.textTertiary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Place, null, tint = ClimbingColors.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ClimbingColors.surfaceVariant,
                    unfocusedContainerColor = ClimbingColors.surfaceVariant,
                    focusedBorderColor = ClimbingColors.primary,
                    unfocusedBorderColor = ClimbingColors.searchBarBorder,
                    cursorColor = ClimbingColors.primary,
                    focusedLabelColor = ClimbingColors.primary,
                    unfocusedLabelColor = ClimbingColors.textTertiary,
                    focusedTextColor = ClimbingColors.textPrimary,
                    unfocusedTextColor = ClimbingColors.textPrimary
                ),
                shape = RoundedCornerShape(10.dp)
            )

            // ── Stars ─────────────────────────────────
            Column {
                Text("Valoraci\u00f3n general", style = MaterialTheme.typography.labelLarge,
                    color = ClimbingColors.textSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..5) {
                        Icon(
                            if (i <= rating) Icons.Default.Star else Icons.Default.StarOutline,
                            contentDescription = "$i estrellas",
                            tint = if (i <= rating) Color(0xFFFFD54F) else ClimbingColors.textTertiary,
                            modifier = Modifier.size(32.dp).clickable { rating = i }
                        )
                    }
                }
            }

            // ── Rock type ─────────────────────────────
            Column {
                Text("Tipo de roca", style = MaterialTheme.typography.labelLarge,
                    color = ClimbingColors.textSecondary)
                Spacer(Modifier.height(6.dp))
                ExposedDropdownMenuBox(expanded = rockTypeExpanded, onExpandedChange = { rockTypeExpanded = it }) {
                    OutlinedTextField(
                        value = rockType,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Seleccionar...", color = ClimbingColors.textTertiary) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ClimbingColors.surfaceVariant,
                            unfocusedContainerColor = ClimbingColors.surfaceVariant,
                            focusedBorderColor = ClimbingColors.primary,
                            unfocusedBorderColor = ClimbingColors.searchBarBorder,
                            focusedTextColor = ClimbingColors.textPrimary,
                            unfocusedTextColor = ClimbingColors.textPrimary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rockTypeExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = rockTypeExpanded,
                        onDismissRequest = { rockTypeExpanded = false },
                        modifier = Modifier.background(ClimbingColors.cardBackground)
                    ) {
                        ROCK_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = ClimbingColors.textPrimary) },
                                onClick = { rockType = type; rockTypeExpanded = false }
                            )
                        }
                    }
                }
            }

            // ── Rock quality ──────────────────────────
            Column {
                Text("Calidad de la roca", style = MaterialTheme.typography.labelLarge,
                    color = ClimbingColors.textSecondary)
                Spacer(Modifier.height(4.dp))
                Text("1 = Muy fr\u00e1gil, 5 = Muy s\u00f3lida",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (q in 1..5) {
                        FilterChip(
                            selected = rockQuality == q,
                            onClick = { rockQuality = q },
                            label = { Text("$q") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ClimbingColors.primary.copy(alpha = 0.2f),
                                selectedLabelColor = ClimbingColors.primary,
                                containerColor = ClimbingColors.surfaceVariant,
                                labelColor = ClimbingColors.textTertiary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color.Transparent,
                                selectedBorderColor = ClimbingColors.primary.copy(alpha = 0.4f),
                                enabled = true, selected = rockQuality == q
                            )
                        )
                    }
                }
            }

            // ── Processionary (solo visible de enero a abril) ──
            val currentMonth = remember { Calendar.getInstance().get(Calendar.MONTH) }
            val isProcessionarySeason = currentMonth in Calendar.JANUARY..Calendar.APRIL
            if (isProcessionarySeason) {
                Column {
                    Text("Presencia de procesionaria", style = MaterialTheme.typography.labelLarge,
                        color = ClimbingColors.textSecondary)
                    Spacer(Modifier.height(4.dp))
                    Text("⚠️ Época de procesionaria (ene - abr)",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.aceptable)
                    Spacer(Modifier.height(8.dp))
                    val procLabels = listOf("No vista", "Poca", "Moderada", "Abundante")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        procLabels.forEachIndexed { i, label ->
                            FilterChip(
                                selected = processionary == i,
                                onClick = { processionary = i },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (i) {
                                        0 -> ClimbingColors.optimo.copy(alpha = 0.2f)
                                        1 -> ClimbingColors.aceptable.copy(alpha = 0.2f)
                                        else -> ClimbingColors.adverso.copy(alpha = 0.2f)
                                    },
                                    selectedLabelColor = when (i) {
                                        0 -> ClimbingColors.optimo
                                        1 -> ClimbingColors.aceptable
                                        else -> ClimbingColors.adverso
                                    },
                                    containerColor = ClimbingColors.surfaceVariant,
                                    labelColor = ClimbingColors.textTertiary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = Color.Transparent,
                                    enabled = true, selected = processionary == i
                                )
                            )
                        }
                    }
                }
            }

            // ── Difficulty levels ─────────────────────
            Column {
                Text("Niveles de dificultad disponibles", style = MaterialTheme.typography.labelLarge,
                    color = ClimbingColors.textSecondary)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip("Iniciaci\u00f3n", beginner) { beginner = it }
                    DifficultyChip("Intermedio", intermediate) { intermediate = it }
                }
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip("Avanzado", advanced) { advanced = it }
                    DifficultyChip("Experto", expert) { expert = it }
                }
            }

            // ── Comment ───────────────────────────────
            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Comentario") },
                placeholder = { Text("Tu experiencia en este sector...", color = ClimbingColors.textTertiary) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ClimbingColors.surfaceVariant,
                    unfocusedContainerColor = ClimbingColors.surfaceVariant,
                    focusedBorderColor = ClimbingColors.primary,
                    unfocusedBorderColor = ClimbingColors.searchBarBorder,
                    cursorColor = ClimbingColors.primary,
                    focusedLabelColor = ClimbingColors.primary,
                    unfocusedLabelColor = ClimbingColors.textTertiary,
                    focusedTextColor = ClimbingColors.textPrimary,
                    unfocusedTextColor = ClimbingColors.textPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                maxLines = 4
            )

            // ── Submit ────────────────────────────────
            val isValid = sectorName.isNotBlank() && rating > 0
            Button(
                onClick = {
                    if (!isValid || isSending) return@Button
                    isSending = true
                    scope.launch {
                        val review = ClimbingReview(
                            locationId = locationId,
                            locationName = locationName,
                            sectorName = sectorName,
                            rating = rating,
                            rockType = rockType,
                            rockQuality = rockQuality,
                            processionary = processionary,
                            hasBeginnerRoutes = beginner,
                            hasIntermediateRoutes = intermediate,
                            hasAdvancedRoutes = advanced,
                            hasExpertRoutes = expert,
                            comment = comment
                        )
                        val ok = ReviewRepository.submitReview(review)
                        isSending = false
                        if (ok) onSubmitted()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                enabled = isValid && !isSending,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClimbingColors.primary)
            ) {
                if (isSending) {
                    CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Publicar rese\u00f1a", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DifficultyChip(label: String, selected: Boolean, onToggle: (Boolean) -> Unit) {
    FilterChip(
        selected = selected,
        onClick = { onToggle(!selected) },
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = ClimbingColors.primary.copy(alpha = 0.2f),
            selectedLabelColor = ClimbingColors.primary,
            containerColor = ClimbingColors.surfaceVariant,
            labelColor = ClimbingColors.textTertiary
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = Color.Transparent,
            selectedBorderColor = ClimbingColors.primary.copy(alpha = 0.4f),
            enabled = true, selected = selected
        ),
        leadingIcon = if (selected) {
            { Icon(Icons.Default.Check, null, Modifier.size(16.dp), tint = ClimbingColors.primary) }
        } else null
    )
}

@Composable
private fun ReviewCard(review: ClimbingReview, onViewProfile: (String) -> Unit = {}) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale("es")) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: user avatar + name + date + stars
            Row(
                Modifier.fillMaxWidth().clickable { onViewProfile(review.userId) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile photo or default icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (review.userPhotoUrl.isNotEmpty()) {
                        val photoModel = if (review.userPhotoUrl.startsWith("/")) {
                            java.io.File(review.userPhotoUrl)
                        } else {
                            review.userPhotoUrl
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de ${review.userName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, null,
                            tint = ClimbingColors.textTertiary, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            review.userName.ifEmpty { review.userEmail.substringBefore("@") },
                            style = MaterialTheme.typography.bodyMedium,
                            color = ClimbingColors.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, null,
                            tint = ClimbingColors.textTertiary, modifier = Modifier.size(14.dp))
                    }
                    Text(dateFormat.format(review.timestamp.toDate()),
                        style = MaterialTheme.typography.labelSmall,
                        color = ClimbingColors.textTertiary)
                }
                // Stars
                Row {
                    for (i in 1..5) {
                        Icon(
                            if (i <= review.rating) Icons.Default.Star else Icons.Default.StarOutline,
                            null, tint = if (i <= review.rating) Color(0xFFFFD54F) else ClimbingColors.textTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Sector name
            if (review.sectorName.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null, tint = ClimbingColors.primary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(review.sectorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ClimbingColors.primary, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Info chips
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (review.rockType.isNotEmpty()) {
                    InfoChip("🪨 ${review.rockType}")
                }
                InfoChip("💪 ${review.rockQualityLabel}")
            }

            // Processionary info — solo mostrar si se rellenó (valor > 0) o en temporada
            val reviewMonth = remember {
                Calendar.getInstance().apply { time = review.timestamp.toDate() }.get(Calendar.MONTH)
            }
            val showProcessionary = review.processionary > 0 || reviewMonth in Calendar.JANUARY..Calendar.APRIL
            if (showProcessionary) {
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    val procColor = when (review.processionary) {
                        0 -> ClimbingColors.optimo
                        1 -> ClimbingColors.aceptable
                        else -> ClimbingColors.adverso
                    }
                    InfoChip("🐛 ${review.processionaryLabel}", procColor)
                }
            }

            if (review.difficultyLevelsText != "Sin especificar") {
                Spacer(Modifier.height(6.dp))
                InfoChip("\uD83E\uDDD7 ${review.difficultyLevelsText}")
            }

            // Comment
            if (review.comment.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClimbingColors.textSecondary,
                    lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun InfoChip(text: String, color: Color = ClimbingColors.textSecondary) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ClimbingColors.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontSize = 11.sp)
    }
}
