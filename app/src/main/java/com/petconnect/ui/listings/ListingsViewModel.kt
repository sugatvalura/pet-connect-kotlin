package com.petconnect.ui.listings

import androidx.lifecycle.ViewModel
import com.petconnect.domain.model.Listing
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.repository.ListingRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.util.Dates
import com.petconnect.domain.util.Geo
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class ListingItem(val listing: Listing, val distanceKm: Double)

data class ListingsUiState(
    val loading: Boolean = true,
    /** null = all kinds. */
    val kind: ListingKind? = null,
    val items: List<ListingItem> = emptyList(),
)

@HiltViewModel
class ListingsViewModel @Inject constructor(
    listings: ListingRepository,
    location: LocationRepository,
    val dates: Dates,
) : ViewModel() {
    private val kind = MutableStateFlow<ListingKind?>(null)

    val state: StateFlow<ListingsUiState> = combine(listings.listings, location.location, kind) { all, loc, k ->
        ListingsUiState(
            loading = false,
            kind = k,
            items = all
                .filter { k == null || it.kind == k }
                .map { ListingItem(it, Geo.distanceKm(loc.location.coordinates, it.location.coordinates)) }
                .sortedBy { it.distanceKm },
        )
    }.stateInVm(this, ListingsUiState())

    fun selectKind(k: ListingKind?) {
        kind.value = k
    }
}

/** "₹12,000" or "Free to a good home". */
fun Listing.priceLabel(): String =
    price?.let { "₹" + "%,d".format(it) } ?: "Free to a good home"
