package com.petconnect.data.repository

import com.petconnect.data.local.IdGenerator
import com.petconnect.data.local.JsonStore
import com.petconnect.data.seed.DemoRelocator
import com.petconnect.data.seed.SeedListings
import com.petconnect.domain.model.Listing
import com.petconnect.domain.model.NewListing
import com.petconnect.domain.repository.ListingRepository
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalListingRepository @Inject constructor(
    private val store: JsonStore,
    private val location: LocationRepository,
    private val ids: IdGenerator,
    private val dates: Dates,
) : ListingRepository {
    private val serializer = ListSerializer(Listing.serializer())
    private val seed = { SeedListings.all }

    override val listings: Flow<List<Listing>> =
        combine(store.observe(JsonStore.Keys.LISTINGS, serializer, seed), location.location) { stored, user ->
            DemoRelocator.relocate(stored, user.location, Listing::offsetKm) { l, loc -> l.copy(location = loc) }
                .sortedByDescending { it.createdAt }
        }

    override suspend fun create(input: NewListing): Listing {
        val listing = Listing(
            id = ids.next("listing"),
            ownerId = input.ownerId,
            kind = input.kind,
            title = input.title.trim(),
            photos = input.photos,
            species = input.species,
            breed = input.breed.trim(),
            gender = input.gender,
            dateOfBirth = input.dateOfBirth,
            location = input.location,
            price = input.price,
            description = input.description.trim(),
            vaccination = input.vaccination,
            createdAt = dates.nowIso(),
        )
        store.update(JsonStore.Keys.LISTINGS, serializer, seed) { listOf(listing) + it }
        return listing
    }

    override suspend fun delete(id: String) {
        store.update(JsonStore.Keys.LISTINGS, serializer, seed) { list -> list.filterNot { it.id == id } }
    }
}
