package com.example.climbingteam.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.climbingteam.data.CLIMBING_GRADES
import com.example.climbingteam.data.ROCK_TYPES
import com.example.climbingteam.data.UserProfile
import com.example.climbingteam.repository.ProfileRepository
import com.example.climbingteam.ui.theme.ClimbingColors
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String? = null,
    onBack: () -> Unit,
    onSendMessage: ((otherUserId: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isMyProfile = userId == null || userId == ProfileRepository.currentUserId()

    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var showSaved by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Editable fields
    var displayName by remember { mutableStateOf("") }
    var favoriteSector by remember { mutableStateOf("") }
    var favoriteRockType by remember { mutableStateOf("") }
    var maxGrade by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }

    var rockTypeExpanded by remember { mutableStateOf(false) }
    var gradeExpanded by remember { mutableStateOf(false) }
    var isUploadingPhoto by remember { mutableStateOf(false) }

    // Photo picker - copies to internal storage
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isUploadingPhoto = true
            errorMsg = null
            scope.launch {
                val savedPath = ProfileRepository.saveProfilePhoto(context, uri)
                if (savedPath != null) {
                    photoUrl = savedPath
                } else {
                    errorMsg = "Error al guardar la foto"
                }
                isUploadingPhoto = false
            }
        }
    }

    // Load profile
    LaunchedEffect(userId) {
        isLoading = true
        val loaded = if (isMyProfile) {
            ProfileRepository.getMyProfile()
        } else {
            ProfileRepository.getProfile(userId!!)
        }
        profile = loaded
        loaded?.let {
            displayName = it.displayName
            favoriteSector = it.favoriteSector
            favoriteRockType = it.favoriteRockType
            maxGrade = it.maxClimbingGrade
            photoUrl = it.photoUrl
        }
        isLoading = false
    }

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
                .padding(top = 44.dp, bottom = 24.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver",
                            tint = ClimbingColors.textPrimary)
                    }
                    Text(
                        if (isMyProfile) "Mi perfil" else "Perfil",
                        style = MaterialTheme.typography.headlineSmall,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Profile photo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.surfaceVariant)
                        .border(3.dp, ClimbingColors.primary.copy(alpha = 0.5f), CircleShape)
                        .then(
                            if (isMyProfile) Modifier.clickable {
                                photoPickerLauncher.launch("image/*")
                            } else Modifier
                        )
                ) {
                    if (photoUrl.isNotEmpty()) {
                        // Build the model: if it's a local path use File, otherwise URL string
                        val imageModel = if (photoUrl.startsWith("/")) {
                            File(photoUrl)
                        } else {
                            photoUrl
                        }
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageModel)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = ClimbingColors.textTertiary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    if (isUploadingPhoto) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    if (isMyProfile && !isUploadingPhoto) {
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(ClimbingColors.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CameraAlt, null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    displayName.ifEmpty { profile?.email?.substringBefore("@") ?: "..." },
                    style = MaterialTheme.typography.titleLarge,
                    color = ClimbingColors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    profile?.email ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary
                )
            }
        }

        if (isLoading) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ClimbingColors.primary)
            }
            return
        }

        // Error message
        if (errorMsg != null) {
            Text(
                errorMsg!!,
                color = ClimbingColors.adverso,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard("Reseñas", "${profile?.reviewCount ?: 0}", Icons.Default.RateReview, Modifier.weight(1f))
            StatCard("Grado máx.", maxGrade.ifEmpty { "-" }, Icons.Default.Terrain, Modifier.weight(1f))
            StatCard("Roca fav.", favoriteRockType.ifEmpty { "-" }, Icons.Default.Landscape, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))

        if (isMyProfile) {
            Text(
                "INFORMACIÓN DEL ESCALADOR",
                style = MaterialTheme.typography.labelSmall,
                color = ClimbingColors.textTertiary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Nombre") },
                        placeholder = { Text("¿Cómo te llamas?", color = ClimbingColors.textTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = ClimbingColors.primary) },
                        colors = profileFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = favoriteSector,
                        onValueChange = { favoriteSector = it },
                        label = { Text("Sector favorito") },
                        placeholder = { Text("Ej: Rodellar, Siurana...", color = ClimbingColors.textTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Place, null, tint = ClimbingColors.primary) },
                        colors = profileFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = rockTypeExpanded,
                        onExpandedChange = { rockTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = favoriteRockType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de roca favorito") },
                            placeholder = { Text("Seleccionar...", color = ClimbingColors.textTertiary) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            leadingIcon = { Icon(Icons.Default.Terrain, null, tint = ClimbingColors.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rockTypeExpanded) },
                            colors = profileFieldColors(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = rockTypeExpanded,
                            onDismissRequest = { rockTypeExpanded = false },
                            modifier = Modifier.background(ClimbingColors.cardBackground)
                        ) {
                            ROCK_TYPES.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type, color = ClimbingColors.textPrimary) },
                                    onClick = { favoriteRockType = type; rockTypeExpanded = false }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = gradeExpanded,
                        onExpandedChange = { gradeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = maxGrade,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grado máximo de escalada") },
                            placeholder = { Text("Seleccionar...", color = ClimbingColors.textTertiary) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            leadingIcon = { Icon(Icons.Default.TrendingUp, null, tint = ClimbingColors.primary) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                            colors = profileFieldColors(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = gradeExpanded,
                            onDismissRequest = { gradeExpanded = false },
                            modifier = Modifier.background(ClimbingColors.cardBackground)
                        ) {
                            CLIMBING_GRADES.forEach { grade ->
                                DropdownMenuItem(
                                    text = { Text(grade, color = ClimbingColors.textPrimary) },
                                    onClick = { maxGrade = grade; gradeExpanded = false }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    isSaving = true
                    errorMsg = null
                    scope.launch {
                        val reviewCount = ProfileRepository.countUserReviews(
                            ProfileRepository.currentUserId() ?: ""
                        )
                        val updated = profile?.copy(
                            displayName = displayName,
                            favoriteSector = favoriteSector,
                            favoriteRockType = favoriteRockType,
                            maxClimbingGrade = maxGrade,
                            photoUrl = photoUrl,
                            reviewCount = reviewCount
                        ) ?: return@launch
                        val ok = ProfileRepository.saveProfile(updated)
                        isSaving = false
                        if (ok) {
                            profile = updated
                            showSaved = true
                        } else {
                            errorMsg = "Error al guardar el perfil"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                enabled = !isSaving,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClimbingColors.primary)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar perfil", fontWeight = FontWeight.Bold)
                }
            }

            AnimatedVisibility(visible = showSaved, enter = fadeIn(), exit = fadeOut()) {
                LaunchedEffect(showSaved) {
                    kotlinx.coroutines.delay(2000)
                    showSaved = false
                }
                Text(
                    "✅ Perfil guardado correctamente",
                    color = ClimbingColors.optimo,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        } else {
            // Send message button for other users' profiles
            val targetUserId = userId
            if (targetUserId != null && onSendMessage != null) {
                Button(
                    onClick = { onSendMessage(targetUserId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ClimbingColors.primary)
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Enviar mensaje", fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(12.dp))
            }

            // View-only profile
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (favoriteSector.isNotEmpty()) {
                        ProfileInfoRow(Icons.Default.Place, "Sector favorito", favoriteSector)
                    }
                    if (favoriteRockType.isNotEmpty()) {
                        ProfileInfoRow(Icons.Default.Terrain, "Roca favorita", favoriteRockType)
                    }
                    if (maxGrade.isNotEmpty()) {
                        ProfileInfoRow(Icons.Default.TrendingUp, "Grado máximo", maxGrade)
                    }
                    if (favoriteSector.isEmpty() && favoriteRockType.isEmpty() && maxGrade.isEmpty()) {
                        Text(
                            "Este usuario aún no ha completado su perfil",
                            color = ClimbingColors.textTertiary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ClimbingColors.cardBackground)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = ClimbingColors.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = ClimbingColors.textTertiary
            )
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = ClimbingColors.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ClimbingColors.textTertiary)
            Text(value, style = MaterialTheme.typography.bodyLarge, color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.Medium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun profileFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = ClimbingColors.surfaceVariant,
    unfocusedContainerColor = ClimbingColors.surfaceVariant,
    focusedBorderColor = ClimbingColors.primary,
    unfocusedBorderColor = ClimbingColors.searchBarBorder,
    cursorColor = ClimbingColors.primary,
    focusedLabelColor = ClimbingColors.primary,
    unfocusedLabelColor = ClimbingColors.textTertiary,
    focusedTextColor = ClimbingColors.textPrimary,
    unfocusedTextColor = ClimbingColors.textPrimary
)
