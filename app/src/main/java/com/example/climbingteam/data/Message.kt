package com.example.climbingteam.data

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val from: String = "",
    val text: String = "",
    val time: Timestamp = Timestamp.now(),
    val read: Boolean = false
)

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val names: Map<String, String> = emptyMap(),
    val photos: Map<String, String> = emptyMap(),
    val lastMsg: String = "",
    val lastTime: Timestamp = Timestamp.now()
) {
    fun otherUserId(myId: String): String = participants.firstOrNull { it != myId } ?: ""
    fun otherName(myId: String): String = names[otherUserId(myId)] ?: "Escalador"
    fun otherPhoto(myId: String): String = photos[otherUserId(myId)] ?: ""
}
