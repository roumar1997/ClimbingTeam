package com.example.climbingteam.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climbingteam.data.ChatMessage
import com.example.climbingteam.ui.theme.ClimbingColors
import com.example.climbingteam.viewmodels.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    conversationId: String,
    otherName: String,
    onBack: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val myId = viewModel.currentUserId ?: ""
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(conversationId) {
        viewModel.openConversation(conversationId)
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(ClimbingColors.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A3A5C), Color(0xFF0F2744))))
                .padding(top = 44.dp, bottom = 12.dp)
                .padding(horizontal = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack, "Volver",
                        tint = ClimbingColors.textPrimary
                    )
                }
                // Avatar circle
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(ClimbingColors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        otherName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleSmall,
                        color = ClimbingColors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        otherName,
                        style = MaterialTheme.typography.titleMedium,
                        color = ClimbingColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Escalador",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClimbingColors.textTertiary
                    )
                }
            }
        }

        Divider(color = ClimbingColors.divider)

        // Messages list
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Group messages by date
            var lastDate = ""
            items(messages, key = { it.id.ifEmpty { it.time.toString() } }) { message ->
                val dateStr = SimpleDateFormat("d MMMM yyyy", Locale("es"))
                    .format(message.time.toDate())
                if (dateStr != lastDate) {
                    lastDate = dateStr
                    Box(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            dateStr,
                            fontSize = 11.sp,
                            color = ClimbingColors.textTertiary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ClimbingColors.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
                MessageBubble(message = message, isMe = message.from == myId)
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ClimbingColors.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Escribe un mensaje...", color = ClimbingColors.textTertiary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(conversationId, inputText)
                        inputText = ""
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ClimbingColors.surfaceVariant,
                    unfocusedContainerColor = ClimbingColors.surfaceVariant,
                    focusedBorderColor = ClimbingColors.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = ClimbingColors.textPrimary,
                    unfocusedTextColor = ClimbingColors.textPrimary,
                    cursorColor = ClimbingColors.primary
                )
            )
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank()) ClimbingColors.primary
                        else ClimbingColors.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMessage(conversationId, inputText)
                            inputText = ""
                        }
                    }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send, "Enviar",
                        tint = if (inputText.isNotBlank()) Color.White else ClimbingColors.textTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage, isMe: Boolean) {
    val timeFmt = remember(message.time) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(message.time.toDate())
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 18.dp,
                            topEnd = 18.dp,
                            bottomStart = if (isMe) 18.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 18.dp
                        )
                    )
                    .background(
                        if (isMe) ClimbingColors.primary
                        else ClimbingColors.cardBackground
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isMe) Color.White else ClimbingColors.textPrimary
                )
            }
            Text(
                timeFmt,
                fontSize = 10.sp,
                color = ClimbingColors.textTertiary,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )
        }
    }
}
