package com.petconnect.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetId
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.usecase.DecideOnPetUseCase
import com.petconnect.domain.usecase.DecisionResult
import com.petconnect.domain.usecase.GetNearbyPetsUseCase
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiscoverUiState(
    val loading: Boolean = true,
    val myPets: List<Pet> = emptyList(),
    val activePet: Pet? = null,
    val deck: List<PetWithContext> = emptyList(),
)

/** One-shot celebration shown after a mutual match. */
data class MatchCelebration(val matchId: String, val myPet: Pet, val theirPet: Pet)

@HiltViewModel
class DiscoverViewModel @Inject constructor(
    getNearbyPets: GetNearbyPetsUseCase,
    observeViewer: ObserveViewerUseCase,
    private val decide: DecideOnPetUseCase,
    private val matches: MatchRepository,
    private val preferences: PreferencesRepository,
    val dates: Dates,
) : ViewModel() {

    val state: StateFlow<DiscoverUiState> = combine(getNearbyPets(), observeViewer()) { feed, viewer ->
        DiscoverUiState(
            loading = false,
            myPets = viewer.myPets,
            activePet = viewer.activePet,
            deck = feed.undecided,
        )
    }.stateInVm(this, DiscoverUiState())

    private val _celebration = MutableStateFlow<MatchCelebration?>(null)
    val celebration: StateFlow<MatchCelebration?> = _celebration.asStateFlow()

    fun onDecision(candidate: Pet, decision: InterestDecision) {
        val myPet = state.value.activePet
        viewModelScope.launch {
            val result = decide(myPet, candidate, decision)
            if (result is DecisionResult.Matched) {
                _celebration.value = MatchCelebration(result.match.id, result.myPet, result.theirPet)
            }
        }
    }

    fun dismissCelebration() {
        _celebration.value = null
    }

    fun setActivePet(id: PetId) {
        viewModelScope.launch { preferences.setActivePetId(id) }
    }

    /** "Start over" once the deck is empty. */
    fun resetDeck() {
        val pet = state.value.activePet ?: return
        viewModelScope.launch { matches.resetDecisions(pet.id) }
    }
}
