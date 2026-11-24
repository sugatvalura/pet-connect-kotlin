package com.petconnect.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetId
import com.petconnect.domain.repository.AuthRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = true,
    val owner: Owner? = null,
    val email: String? = null,
    val myPets: List<Pet> = emptyList(),
    val activePetId: PetId? = null,
    val locationLabel: String = "",
    val isPreciseLocation: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeViewer: ObserveViewerUseCase,
    private val auth: AuthRepository,
    private val pets: PetRepository,
    private val location: LocationRepository,
    private val preferences: PreferencesRepository,
    val dates: Dates,
) : ViewModel() {

    val state: StateFlow<ProfileUiState> = combine(
        observeViewer(),
        pets.owners,
        auth.session,
        location.location,
    ) { viewer, owners, session, loc ->
        ProfileUiState(
            loading = false,
            owner = owners.firstOrNull { it.id == viewer.ownerId },
            email = session?.email,
            myPets = viewer.myPets,
            activePetId = viewer.activePet?.id,
            locationLabel = loc.location.label,
            isPreciseLocation = loc.isPrecise,
        )
    }.stateInVm(this, ProfileUiState())

    fun setActivePet(id: PetId) = launch { preferences.setActivePetId(id) }

    /** Toggling breeding also toggles the availability window, as in the RN app. */
    fun setBreeding(id: PetId, enabled: Boolean) = launch {
        pets.updatePet(id) { it.copy(availableForBreeding = enabled, availability = it.availability.copy(enabled = enabled)) }
    }

    fun removePet(id: PetId) = launch { pets.deletePet(id) }

    fun refreshLocation() = launch { location.refresh() }

    fun signOut() = launch { auth.signOut() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
