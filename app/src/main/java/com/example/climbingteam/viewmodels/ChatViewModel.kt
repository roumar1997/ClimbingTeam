package com.example.climbingteam.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.climbingteam.data.ChatMessage
import com.example.climbingteam.data.Conversation
import com.example.climbingteam.data.UserProfile
import com.example.climbingteam.repository.MessageRepository
import com.example.climbingteam.repository.ProfileRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    val currentUserId get() = MessageRepository.currentUserId()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserProfile>>(emptyList())
    val searchResults: StateFlow<List<UserProfile>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            MessageRepository.getConversations().collect { convs ->
                _conversations.value = convs
            }
        }
    }

    /** Start listening to messages in a specific conversation */
    fun openConversation(convId: String) {
        messagesJob?.cancel()
        _messages.value = emptyList()
        messagesJob = viewModelScope.launch {
            MessageRepository.getMessages(convId).collect { msgs ->
                _messages.value = msgs
            }
        }
    }

    fun sendMessage(convId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            MessageRepository.sendMessage(convId, text)
        }
    }

    /** Open or create a conversation with another user, returns conversationId */
    suspend fun startConversation(otherUserId: String, otherProfile: UserProfile?): String {
        return MessageRepository.getOrCreateConversation(otherUserId, otherProfile)
    }

    /** Search users by name to start a conversation */
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            _searchResults.value = ProfileRepository.searchUsers(query)
            _isSearching.value = false
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }
}
