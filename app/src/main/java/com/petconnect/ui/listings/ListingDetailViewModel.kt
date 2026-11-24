package com.petconnect.ui.listings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.petconnect.domain.model.Listing
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.model.NotificationKind
import com.petconnect.domain.model.Owner
import com.petconnect.domain.repository.ListingRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.NotificationRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.domain.util.Geo
import com.petconnect.ui.common.stateInVm
import com.petconnect.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ListingDetailUiState {
    data object Loading : ListingDetailUiState
    data object NotFound : ListingDetailUiState
    data class Ready(val listing: Listing, val owner: Owner?, val isMine: Boolean, val distanceKm: Double) : ListingDetailUiState
}

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    savedState: SavedStateHandle,
    observeViewer: ObserveViewerUseCase,
    pets: PetRepository,
    location: LocationRepository,
    private val listings: ListingRepository,
    private val notifications: NotificationRepository,
    val dates: Dates,
) : ViewModel() {
    private val listingId = savedState.toRoute<Route.ListingDetail>().listingId

    val state: StateFlow<ListingDetailUiState> = combine(
        observeViewer(), listings.listings, pets.owners, location.location,
    ) { viewer, all, owners, loc ->
        val l = all.firstOrNull { it.id == listingId } ?: return@combine ListingDetailUiState.NotFound
        ListingDetailUiState.Ready(
            listing = l,
            owner = owners.firstOrNull { it.id == l.ownerId },
            isMine = l.ownerId == viewer.ownerId,
            distanceKm = Geo.distanceKm(loc.location.coordinates, l.location.coordinates),
        )
    }.stateInVm(this, ListingDetailUiState.Loading)

    /** One-shot message for a snackbar. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _closed = MutableStateFlow(false)
    val closed: StateFlow<Boolean> = _closed.asStateFlow()

    fun respond() {
        val s = state.value as? ListingDetailUiState.Ready ?: return
        val what = if (s.listing.kind == ListingKind.ADOPT) "the adoption" else "the sale"
        val name = s.owner?.name ?: "The owner"
        viewModelScope.launch {
            notifications.push(
                NotificationKind.LISTING_RESPONSE,
                title = "Enquiry sent",
                body = "$name will get in touch about $what of ${s.listing.title}.",
                route = "listing/${s.listing.id}",
            )
            _message.value = "Enquiry sent — $name will get in touch about $what."
        }
    }

    fun delete() {
        viewModelScope.launch {
            listings.delete(listingId)
            _closed.value = true
        }
    }

    fun messageShown() {
        _message.value = null
    }
}
