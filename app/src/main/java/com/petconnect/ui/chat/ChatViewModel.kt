package com.petconnect.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.petconnect.domain.model.Message
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.repository.ImageRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.usecase.SendMessageUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.ui.common.stateInVm
import com.petconnect.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data object Unavailable : ChatUiState
    data class Ready(
        val me: OwnerId,
        val theirPet: Pet,
        val theirOwner: Owner,
        val messages: List<Message>,
    ) : ChatUiState
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeViewer: ObserveViewerUseCase,
    matches: MatchRepository,
    pets: PetRepository,
    private val chat: ChatRepository,
    private val sendMessage: SendMessageUseCase,
    private val images: ImageRepository,
    private val location: LocationRepository,
    val dates: Dates,
) : ViewModel() {
    private val matchId = savedState.toRoute<Route.Chat>().matchId

    val state: StateFlow<ChatUiState> = combine(
        observeViewer(),
        matches.matches,
        pets.pets,
        pets.owners,
        chat.messages(matchId),
    ) { viewer, allMatches, allPets, owners, messages ->
        val match = allMatches.firstOrNull { it.id == matchId } ?: return@combine ChatUiState.Unavailable
        val mine = viewer.myPets.map { it.id }.toSet()
        val theirPet = match.otherPet(mine)?.let { id -> allPets.firstOrNull { it.id == id } }
        val theirOwner = theirPet?.let { p -> owners.firstOrNull { it.id == p.ownerId } }
        if (theirPet == null || theirOwner == null) return@combine ChatUiState.Unavailable
        ChatUiState.Ready(viewer.ownerId, theirPet, theirOwner, messages.sortedBy { it.createdAt })
    }.stateInVm(this, ChatUiState.Loading)

    private val _draft = MutableStateFlow("")
    val draft: StateFlow<String> = _draft.asStateFlow()

    private val _closed = MutableStateFlow(false)
    val closed: StateFlow<Boolean> = _closed.asStateFlow()

    fun onDraft(text: String) {
        _draft.value = text
    }

    private fun me(): OwnerId? = (state.value as? ChatUiState.Ready)?.me

    fun sendText() {
        val me = me() ?: return
        val text = _draft.value
        viewModelScope.launch {
            if (sendMessage.text(matchId, me, text)) _draft.value = ""
        }
    }

    fun sendPhoto(uri: String) {
        val me = me() ?: return
        viewModelScope.launch {
            images.import(uri)?.let { sendMessage.image(matchId, me, it) }
        }
    }

    /** Blurred to ~1 km inside the use case — never the exact address. */
    fun shareApproximateLocation() {
        val me = me() ?: return
        viewModelScope.launch {
            sendMessage.approximateLocation(matchId, me, location.location.value.location.coordinates)
        }
    }

    fun reportAndBlock() {
        val owner = (state.value as? ChatUiState.Ready)?.theirOwner ?: return
        viewModelScope.launch {
            chat.blockOwner(owner.id)
            _closed.value = true
        }
    }
}
