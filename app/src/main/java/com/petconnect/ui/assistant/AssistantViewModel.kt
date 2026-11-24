package com.petconnect.ui.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.repository.AssistantReply
import com.petconnect.domain.repository.AssistantRepository
import com.petconnect.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssistantTurn(
    val id: Long,
    val fromUser: Boolean,
    val text: String,
    val blocked: Boolean = false,
    val source: AssistantReply.Source? = null,
)

data class Suggestion(val label: String, val prompt: String)

data class AssistantUiState(
    val turns: List<AssistantTurn> = emptyList(),
    val draft: String = "",
    val thinking: Boolean = false,
) {
    companion object {
        val SUGGESTIONS = listOf(
            Suggestion("👋 Say hi", "Hi!"),
            Suggestion("❤️ Matching", "How does matching work?"),
            Suggestion("🐕 Labradors", "Tell me about Labradors"),
            Suggestion("🐈 Persian cats", "What about Persian cats?"),
            Suggestion("🛡️ Privacy", "Is my location private?"),
        )
    }
}

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val assistant: AssistantRepository,
    private val auth: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(AssistantUiState())
    val state: StateFlow<AssistantUiState> = _state.asStateFlow()
    private var nextId = 0L

    fun onDraft(text: String) = _state.update { it.copy(draft = text) }

    fun send(text: String = _state.value.draft) {
        val message = text.trim()
        if (message.isEmpty() || _state.value.thinking) return
        _state.update {
            it.copy(turns = it.turns + AssistantTurn(nextId++, fromUser = true, text = message), draft = "", thinking = true)
        }
        viewModelScope.launch {
            val userId = auth.session.first()?.ownerId?.value
            val reply = assistant.ask(message, userId)
            _state.update {
                it.copy(
                    thinking = false,
                    turns = it.turns + AssistantTurn(nextId++, false, reply.reply, reply.blocked, reply.source),
                )
            }
        }
    }

    fun reset() = _state.update { AssistantUiState() }
}
