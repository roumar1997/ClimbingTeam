package com.example.climbingteam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.climbingteam.data.Conversation
import com.example.climbingteam.data.UserProfile
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    viewModel: ChatViewModel,
    onOpenChat: (convId: String, otherName: String) -> Unit,
    onOpenNewChat: ((otherUserId: String, otherProfile: UserProfile) -> Unit)? = null
) {
    val conversations by viewModel.conversations.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val myId = viewModel.currentUserId ?: ""

    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(ClimbingColors.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(ClimbingColors.headerGradientTop, ClimbingColors.headerGradientMid, ClimbingColors.background)))
                .padding(top = 48.dp, bottom = 12.dp)
                .padding(horizontal = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Chat, null,
                        tint = ClimbingColors.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            "Mensajes",
                            style = MaterialTheme.typography.headlineSmall,
                            color = ClimbingColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${conversations.size} conversaciones",
                            style = MaterialTheme.typography.bodySmall,
                            color = ClimbingColors.textTertiary
                        )
                    }
                }

                // Search toggle button
                IconButton(
                    onClick = {
                        showSearch = !showSearch
                        if (!showSearch) {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }
                    }
                ) {
                    Icon(
                        if (showSearch) Icons.Default.Close else Icons.Default.PersonSearch,
                        null,
                        tint = ClimbingColors.primary
                    )
                }
            }
        }

        // Search bar
        if (showSearch) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { q ->
                    searchQuery = q
                    if (q.length >= 2) viewModel.searchUsers(q)
                    else viewModel.clearSearch()
                },
                placeholder = { Text("Buscar escalador por nombre...", color = ClimbingColors.textTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = ClimbingColors.primary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ClimbingColors.surfaceVariant,
                    unfocusedContainerColor = ClimbingColors.surfaceVariant,
                    focusedBorderColor = ClimbingColors.primary,
                    unfocusedBorderColor = ClimbingColors.searchBarBorder,
                    cursorColor = ClimbingColors.primary,
                    focusedTextColor = ClimbingColors.textPrimary,
                    unfocusedTextColor = ClimbingColors.textPrimary
                )
            )

            // Search results
            if (isSearching) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = ClimbingColors.primary, strokeWidth = 2.dp)
                }
            } else if (searchResults.isNotEmpty()) {
                Text(
                    "RESULTADOS",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClimbingColors.textTertiary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
                searchResults.forEach { profile ->
                    UserSearchItem(
                        profile = profile,
                        onClick = {
                            onOpenNewChat?.invoke(profile.userId, profile)
                            showSearch = false
                            searchQuery = ""
                            viewModel.clearSearch()
                        }
                    )
                    HorizontalDivider(color = ClimbingColors.divider, modifier = Modifier.padding(start = 80.dp))
                }
                HorizontalDivider(color = ClimbingColors.divider, thickness = 2.dp)
            } else if (searchQuery.length >= 2) {
                Box(
                    Modifier.fillMaxWidth().padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No se encontraron usuarios",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary
                    )
                }
            }
        }

        HorizontalDivider(color = ClimbingColors.divider, thickness = 1.dp)

        if (conversations.isEmpty() && !showSearch) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("\uD83D\uDCAC", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Sin mensajes a\u00fan",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Pulsa \uD83D\uDD0D arriba para buscar escaladores\no entra en el perfil de un escalador desde sus rese\u00f1as",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(conversations, key = { it.id }) { conv ->
                    ConversationItem(
                        conversation = conv,
                        myId = myId,
                        onClick = { onOpenChat(conv.id, conv.otherName(myId)) }
                    )
                    HorizontalDivider(
                        color = ClimbingColors.divider,
                        modifier = Modifier.padding(start = 80.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserSearchItem(
    profile: UserProfile,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(ClimbingColors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (profile.photoUrl.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(profile.photoUrl).crossfade(true).build(),
                    contentDescription = profile.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    (profile.displayName.ifEmpty { profile.email }).firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleSmall,
                    color = ClimbingColors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {
            Text(
                profile.displayName.ifEmpty { profile.email.substringBefore("@") },
                style = MaterialTheme.typography.titleSmall,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (profile.favoriteSector.isNotEmpty() || profile.maxClimbingGrade.isNotEmpty()) {
                Text(
                    listOfNotNull(
                        profile.favoriteSector.ifEmpty { null },
                        profile.maxClimbingGrade.ifEmpty { null }
                    ).joinToString(" \u00b7 "),
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Icon(
            Icons.Default.Chat, null,
            tint = ClimbingColors.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ConversationItem(
    conversation: Conversation,
    myId: String,
    onClick: () -> Unit
) {
    val otherName = conversation.otherName(myId)
    val otherPhoto = conversation.otherPhoto(myId)
    val timeFmt = remember(conversation.lastTime) {
        val now = Calendar.getInstance()
        val msgCal = Calendar.getInstance().apply { time = conversation.lastTime.toDate() }
        if (now.get(Calendar.DATE) == msgCal.get(Calendar.DATE)) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(conversation.lastTime.toDate())
        } else {
            SimpleDateFormat("d MMM", Locale("es")).format(conversation.lastTime.toDate())
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(ClimbingColors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (otherPhoto.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(otherPhoto).crossfade(true).build(),
                    contentDescription = otherName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    otherName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClimbingColors.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                otherName,
                style = MaterialTheme.typography.titleSmall,
                color = ClimbingColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
            if (conversation.lastMsg.isNotEmpty()) {
                Text(
                    conversation.lastMsg,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClimbingColors.textTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Text(
            timeFmt,
            style = MaterialTheme.typography.labelSmall,
            color = ClimbingColors.textTertiary
        )
    }
}
