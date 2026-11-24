package com.petconnect.domain.usecase

import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.repository.PreferencesRepository
import com.petconnect.domain.util.Dates
import com.petconnect.domain.util.Geo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

data class NearbyFeed(
    /** Filtered and sorted best-match first. */
    val all: List<PetWithContext>,
    /** [all] minus pets the active pet already swiped on — the swipe deck. */
    val undecided: List<PetWithContext>,
    val availableBreeds: List<String>,
    /** Everyone else's pets before filters. */
    val totalNearby: Int,
    val filters: PetFilters,
)

/**
 * The nearby feed: everyone else's pets, measured, scored relative to the
 * viewer's active pet, filtered, sorted best-first.
 */
class GetNearbyPetsUseCase @Inject constructor(
    private val petRepository: PetRepository,
    private val matchRepository: MatchRepository,
    private val chatRepository: ChatRepository,
    private val locationRepository: LocationRepository,
    private val preferences: PreferencesRepository,
    private val observeViewer: ObserveViewerUseCase,
    private val compatibility: CompatibilityCalculator,
    private val dates: Dates,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<NearbyFeed> {
        val viewer = observeViewer()
        val decided = viewer.flatMapLatest { v ->
            v.activePet?.let { matchRepository.decidedPetIds(it.id) } ?: flowOf(emptySet())
        }
        val scored = combine(
            petRepository.pets,
            petRepository.owners,
            viewer,
            locationRepository.location,
            chatRepository.blockedOwners,
        ) { pets, owners, v, location, blocked ->
            val ownersById = owners.associateBy { it.id }
            val others = pets.filter { !it.isOwnedBy(v.ownerId) && it.ownerId !in blocked }
            val decorated = others.map { pet ->
                val distance = Geo.distanceKm(location.location.coordinates, pet.location.coordinates)
                PetWithContext(pet, ownersById[pet.ownerId], distance, compatibility(v.activePet, pet, distance))
            }
            decorated to pets.map { it.breed }.distinct().sorted()
        }

        return combine(scored, preferences.filters, decided) { (decorated, breeds), filters, decidedIds ->
            val filtered = decorated
                .filter { it.matches(filters) }
                .sortedByDescending { it.compatibility.score }
            NearbyFeed(
                all = filtered,
                undecided = filtered.filter { it.pet.id !in decidedIds },
                availableBreeds = breeds,
                totalNearby = decorated.size,
                filters = filters,
            )
        }
    }

    private fun PetWithContext.matches(f: PetFilters): Boolean {
        if (distanceKm > f.maxDistanceKm) return false
        if (f.species != null && pet.species != f.species) return false
        if (f.gender != null && pet.gender != f.gender) return false
        if (f.breed != null && !pet.breed.contains(f.breed, ignoreCase = true)) return false
        val years = dates.ageInYears(pet.dateOfBirth)
        if (years < f.minAgeYears || years > f.maxAgeYears) return false
        if (f.availableForBreedingOnly && !pet.availableForBreeding) return false
        return true
    }
}
