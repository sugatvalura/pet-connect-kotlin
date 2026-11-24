package com.petconnect.ui.nearby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.NotificationRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.usecase.GetNearbyPetsUseCase
import com.petconnect.domain.usecase.NearbyFeed
import com.petconnect.domain.usecase.ObserveViewerUseCase
import com.petconnect.domain.util.Dates
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NearbyUiState(
    val loading: Boolean = true,
    val feed: NearbyFeed? = null,
    val hasOwnPets: Boolean = true,
    val matchCount: Int = 0,
    val locationLabel: String = "",
    val isPreciseLocation: Boolean = false,
    val unreadNotifications: Int = 0,
    val pageSize: Int = PAGE_SIZE,
) {
    val all: List<PetWithContext> get() = feed?.all.orEmpty()
    val visible: List<PetWithContext> get() = all.take(pageSize)
    val hasMore: Boolean get() = pageSize < all.size
    val topMatch: PetWithContext? get() = all.firstOrNull()
    val availableNow: List<PetWithContext> get() = all.filter { it.pet.availableForBreeding }
    val filters: PetFilters get() = feed?.filters ?: PetFilters.DEFAULT

    companion object { const val PAGE_SIZE = 5 }
}

@HiltViewModel
class NearbyViewModel @Inject constructor(
    getNearbyPets: GetNearbyPetsUseCase,
    observeViewer: ObserveViewerUseCase,
    matches: MatchRepository,
    notifications: NotificationRepository,
    private val location: LocationRepository,
    private val preferences: PreferencesRepository,
    val dates: Dates,
) : ViewModel() {

    private val pageSize = MutableStateFlow(NearbyUiState.PAGE_SIZE)
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val myPetIds = observeViewer().map { v -> v.myPets.map { it.id }.toSet() }

    private val matchCount = combine(matches.matches, myPetIds) { all, mine ->
        all.count { m -> m.petIds.any { it in mine } }
    }

    val state: StateFlow<NearbyUiState> = combine(
        getNearbyPets(),
        myPetIds,
        matchCount,
        combine(location.location, notifications.notifications) { loc, notes -> loc to notes.count { !it.read } },
        pageSize,
    ) { feed, mine, matchCount, (loc, unread), size ->
        NearbyUiState(
            loading = false,
            feed = feed,
            hasOwnPets = mine.isNotEmpty(),
            matchCount = matchCount,
            locationLabel = loc.location.label,
            isPreciseLocation = loc.isPrecise,
            unreadNotifications = unread,
            pageSize = size,
        )
    }.stateInVm(this, NearbyUiState())

    fun setFilters(filters: PetFilters) {
        pageSize.value = NearbyUiState.PAGE_SIZE // reset pagination when filters change
        preferences.setFilters(filters)
    }

    fun resetFilters() = setFilters(PetFilters.DEFAULT)

    fun loadMore() = pageSize.update { it + NearbyUiState.PAGE_SIZE }

    fun refresh() {
        viewModelScope.launch {
            _refreshing.value = true
            pageSize.value = NearbyUiState.PAGE_SIZE
            location.refresh()
            _refreshing.value = false
        }
    }
}
