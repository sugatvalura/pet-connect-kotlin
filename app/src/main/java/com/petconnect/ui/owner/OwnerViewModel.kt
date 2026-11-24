package com.petconnect.ui.owner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.navigation.toRoute
import com.petconnect.domain.model.Listing
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.repository.ListingRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.usecase.CompatibilityCalculator
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.domain.util.Geo
import com.petconnect.ui.common.stateInVm
import com.petconnect.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

sealed interface OwnerUiState {
    data object Loading : OwnerUiState
    data object NotFound : OwnerUiState
    data class Ready(val owner: Owner, val pets: List<PetWithContext>, val listings: List<Listing>) : OwnerUiState
}

@HiltViewModel
class OwnerViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeViewer: ObserveViewerUseCase,
    pets: PetRepository,
    listings: ListingRepository,
    location: LocationRepository,
    compatibility: CompatibilityCalculator,
    val dates: Dates,
) : ViewModel() {
    private val ownerId = OwnerId(savedState.toRoute<Route.OwnerProfile>().ownerId)

    val state: StateFlow<OwnerUiState> = combine(
        observeViewer(), pets.pets, pets.owners, listings.listings, location.location,
    ) { viewer, allPets, owners, allListings, loc ->
        val owner = owners.firstOrNull { it.id == ownerId } ?: return@combine OwnerUiState.NotFound
        val theirPets = allPets.filter { it.isOwnedBy(ownerId) }.map { pet ->
            val d = Geo.distanceKm(loc.location.coordinates, pet.location.coordinates)
            PetWithContext(pet, owner, d, compatibility(viewer.activePet, pet, d))
        }
        OwnerUiState.Ready(owner, theirPets, allListings.filter { it.ownerId == ownerId })
    }.stateInVm(this, OwnerUiState.Loading)
}
