package com.petconnect.ui.chats

import androidx.lifecycle.ViewModel
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.Pet
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class Conversation(
    val match: Match,
    val myPet: Pet,
    val theirPet: Pet,
    val theirOwner: Owner?,
)

data class ChatsUiState(val loading: Boolean = true, val conversations: List<Conversation> = emptyList())

@HiltViewModel
class ChatsViewModel @Inject constructor(
    observeViewer: ObserveViewerUseCase,
    matches: MatchRepository,
    pets: PetRepository,
    chat: ChatRepository,
    val dates: Dates,
) : ViewModel() {

    val state: StateFlow<ChatsUiState> = combine(
        observeViewer(),
        matches.matches,
        pets.pets,
        pets.owners,
        chat.blockedOwners,
    ) { viewer, allMatches, allPets, owners, blocked ->
        val petsById = allPets.associateBy { it.id }
        val ownersById = owners.associateBy { it.id }
        val mine = viewer.myPets.map { it.id }.toSet()
        val conversations = allMatches.mapNotNull { m ->
            val myId = m.petIds.firstOrNull { it in mine } ?: return@mapNotNull null
            val theirId = m.otherPet(mine) ?: return@mapNotNull null
            val theirPet = petsById[theirId] ?: return@mapNotNull null
            if (theirPet.ownerId in blocked) return@mapNotNull null
            Conversation(m, petsById.getValue(myId), theirPet, ownersById[theirPet.ownerId])
        }.sortedByDescending { it.match.lastMessageAt ?: it.match.createdAt }
        ChatsUiState(loading = false, conversations = conversations)
    }.stateInVm(this, ChatsUiState())
}
