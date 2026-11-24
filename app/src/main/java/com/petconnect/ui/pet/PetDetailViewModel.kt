package com.petconnect.ui.pet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.petconnect.domain.model.CompatibilityResult
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetId
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.usecase.AvailabilityPrivacyPolicy
import com.petconnect.domain.usecase.CompatibilityCalculator
import com.petconnect.domain.usecase.DecideOnPetUseCase
import com.petconnect.domain.usecase.DecisionResult
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.domain.util.Geo
import com.petconnect.ui.common.stateInVm
import com.petconnect.ui.discover.MatchCelebration
import com.petconnect.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface PetDetailUiState {
    data object Loading : PetDetailUiState
    data object NotFound : PetDetailUiState
    data class Ready(
        val pet: Pet,
        val owner: Owner?,
        val isOwnPet: Boolean,
        val distanceKm: Double,
        val compatibility: CompatibilityResult,
        val activePet: Pet?,
        val existingMatch: Match?,
        val canSeeAvailability: Boolean,
    ) : PetDetailUiState
}

@HiltViewModel
class PetDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeViewer: ObserveViewerUseCase,
    pets: PetRepository,
    matches: MatchRepository,
    location: LocationRepository,
    compatibility: CompatibilityCalculator,
    private val decide: DecideOnPetUseCase,
    val dates: Dates,
) : ViewModel() {
    private val petId = PetId(savedState.toRoute<Route.PetDetail>().petId)

    val state: StateFlow<PetDetailUiState> = combine(
        observeViewer(),
        pets.pets,
        pets.owners,
        matches.matches,
        location.location,
    ) { viewer, allPets, owners, allMatches, loc ->
        val pet = allPets.firstOrNull { it.id == petId } ?: return@combine PetDetailUiState.NotFound
        val mine = viewer.myPets.map { it.id }.toSet()
        val match = allMatches.firstOrNull { m -> m.involves(pet.id) && m.petIds.any { it in mine } }
        val distance = Geo.distanceKm(loc.location.coordinates, pet.location.coordinates)
        PetDetailUiState.Ready(
            pet = pet,
            owner = owners.firstOrNull { it.id == pet.ownerId },
            isOwnPet = pet.isOwnedBy(viewer.ownerId),
            distanceKm = distance,
            compatibility = compatibility(viewer.activePet, pet, distance),
            activePet = viewer.activePet,
            existingMatch = match,
            canSeeAvailability = AvailabilityPrivacyPolicy.canView(pet, viewer.ownerId, isMatched = match != null),
        )
    }.stateInVm(this, PetDetailUiState.Loading)

    private val _deciding = MutableStateFlow(false)
    val deciding: StateFlow<Boolean> = _deciding.asStateFlow()

    private val _celebration = MutableStateFlow<MatchCelebration?>(null)
    val celebration: StateFlow<MatchCelebration?> = _celebration.asStateFlow()

    /** Emits once after a "Skip" so the screen can pop back. */
    private val _closed = MutableStateFlow(false)
    val closed: StateFlow<Boolean> = _closed.asStateFlow()

    fun decide(decision: InterestDecision) {
        val ready = state.value as? PetDetailUiState.Ready ?: return
        viewModelScope.launch {
            _deciding.value = true
            val result = decide(ready.activePet, ready.pet, decision)
            _deciding.value = false
            when {
                result is DecisionResult.Matched ->
                    _celebration.value = MatchCelebration(result.match.id, result.myPet, result.theirPet)
                decision == InterestDecision.SKIPPED -> _closed.value = true
            }
        }
    }

    fun dismissCelebration() {
        _celebration.value = null
    }
}
