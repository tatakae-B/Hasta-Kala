package com.hastakala.shop.viewmodel.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hastakala.shop.data.ai.AIRepository
import com.hastakala.shop.data.ai.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(text: String, language: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val userMsg = ChatMessage(text, true)
            _chatMessages.value = _chatMessages.value + userMsg
            _isLoading.value = true

            try {
                val result = aiRepository.getBusinessResponse(text, language)
                result.onSuccess { responseText ->
                    val aiMsg = ChatMessage(responseText, false)
                    _chatMessages.value = _chatMessages.value + aiMsg
                }.onFailure { e ->
                    // Use the specific exception message passed from AIRepository
                    val errorMsg = e.message ?: "AI Assistant is temporarily unavailable. Please check your internet connection."
                    _chatMessages.value = _chatMessages.value + ChatMessage(errorMsg, false)
                }
            } catch (e: Exception) {
                android.util.Log.e("AIViewModel", "Unexpected error: ${e.message}", e)
                _chatMessages.value = _chatMessages.value + ChatMessage("An unexpected error occurred. Please try again.", false)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }
}