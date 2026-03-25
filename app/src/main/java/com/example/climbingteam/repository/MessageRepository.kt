package com.example.climbingteam.repository

import com.example.climbingteam.data.ChatMessage
import com.example.climbingteam.data.Conversation
import com.example.climbingteam.data.UserProfile
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

object MessageRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun currentUserId(): String? = auth.currentUser?.uid

    /** Deterministic conversation ID from two user IDs */
    fun conversationId(uid1: String, uid2: String): String =
        listOf(uid1, uid2).sorted().joinToString("_")

    /**
     * Get or create a conversation between the current user and another user.
     * Returns the conversation ID.
     */
    suspend fun getOrCreateConversation(otherUserId: String, otherProfile: UserProfile?): String {
        val myId = currentUserId() ?: return ""
        val convId = conversationId(myId, otherUserId)
        val myProfile = ProfileRepository.getProfile(myId)

        val docRef = db.collection("conversations").document(convId)
        val doc = docRef.get().await()
        if (!doc.exists()) {
            val conv = mapOf(
                "participants" to listOf(myId, otherUserId),
                "names" to mapOf(
                    myId to (myProfile?.displayName?.ifEmpty { myProfile.email } ?: "Yo"),
                    otherUserId to (otherProfile?.displayName?.ifEmpty { otherProfile.email } ?: "Escalador")
                ),
                "photos" to mapOf(
                    myId to (myProfile?.photoUrl ?: ""),
                    otherUserId to (otherProfile?.photoUrl ?: "")
                ),
                "lastMsg" to "",
                "lastTime" to Timestamp.now()
            )
            docRef.set(conv).await()
        }
        return convId
    }

    /** Real-time stream of messages in a conversation */
    fun getMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id = doc.id,
                            from = doc.getString("from") ?: "",
                            text = doc.getString("text") ?: "",
                            time = doc.getTimestamp("time") ?: Timestamp.now(),
                            read = doc.getBoolean("read") ?: false
                        )
                    } catch (_: Exception) { null }
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    /** Send a message */
    suspend fun sendMessage(conversationId: String, text: String) {
        val myId = currentUserId() ?: return
        val msg = mapOf(
            "from" to myId,
            "text" to text.trim(),
            "time" to Timestamp.now(),
            "read" to false
        )
        db.collection("conversations").document(conversationId)
            .collection("messages").add(msg).await()
        db.collection("conversations").document(conversationId)
            .update("lastMsg", text.trim(), "lastTime", Timestamp.now()).await()
    }

    /** Real-time stream of all conversations for the current user */
    fun getConversations(): Flow<List<Conversation>> {
        val myId = currentUserId() ?: return flowOf(emptyList())
        return callbackFlow {
            val listener = db.collection("conversations")
                .whereArrayContains("participants", myId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    @Suppress("UNCHECKED_CAST")
                    val convs = snapshot?.documents?.mapNotNull { doc ->
                        try {
                            val participants = (doc.get("participants") as? List<*>)
                                ?.filterIsInstance<String>() ?: emptyList()
                            val names = (doc.get("names") as? Map<*, *>)
                                ?.mapKeys { it.key.toString() }
                                ?.mapValues { it.value.toString() } ?: emptyMap()
                            val photos = (doc.get("photos") as? Map<*, *>)
                                ?.mapKeys { it.key.toString() }
                                ?.mapValues { it.value.toString() } ?: emptyMap()
                            Conversation(
                                id = doc.id,
                                participants = participants,
                                names = names,
                                photos = photos,
                                lastMsg = doc.getString("lastMsg") ?: "",
                                lastTime = doc.getTimestamp("lastTime") ?: Timestamp.now()
                            )
                        } catch (_: Exception) { null }
                    }?.sortedByDescending { it.lastTime.toDate() } ?: emptyList()
                    trySend(convs)
                }
            awaitClose { listener.remove() }
        }
    }
}
