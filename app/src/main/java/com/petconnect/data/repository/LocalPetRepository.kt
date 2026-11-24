package com.petconnect.data.repository

import com.petconnect.data.local.IdGenerator
import com.petconnect.data.local.JsonStore
import com.petconnect.data.seed.DemoRelocator
import com.petconnect.data.seed.SeedOwners
import com.petconnect.data.seed.SeedPets
import com.petconnect.domain.model.NewPet
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetId
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalPetRepository @Inject constructor(
    private val store: JsonStore,
    private val location: LocationRepository,
    private val ids: IdGenerator,
    private val dates: Dates,
) : PetRepository {

    private val serializer = ListSerializer(Pet.serializer())
    private val seed = { SeedPets.all }

    override val pets: Flow<List<Pet>> =
        combine(store.observe(JsonStore.Keys.PETS, serializer, seed), location.location) { stored, user ->
            DemoRelocator.relocate(stored, user.location, Pet::offsetKm) { pet, loc -> pet.copy(location = loc) }
        }

    override val owners: Flow<List<Owner>> = flowOf(SeedOwners.all)

    override suspend fun createPet(input: NewPet): Pet {
        val pet = Pet(
            id = PetId(ids.next("pet")),
            ownerId = input.ownerId,
            name = input.name.trim(),
            photos = input.photos,
            species = input.species,
            breed = input.breed.trim(),
            gender = input.gender,
            dateOfBirth = input.dateOfBirth,
            location = input.location,
            bio = input.bio.trim(),
            vaccination = input.vaccination,
            isSpayedOrNeutered = input.isSpayedOrNeutered,
            availableForBreeding = input.availableForBreeding,
            availability = input.availability,
            createdAt = dates.nowIso(),
        )
        store.update(JsonStore.Keys.PETS, serializer, seed) { listOf(pet) + it }
        return pet
    }

    override suspend fun updatePet(id: PetId, transform: (Pet) -> Pet) {
        // Never let a transform change identity or ownership.
        store.update(JsonStore.Keys.PETS, serializer, seed) { list ->
            list.map { if (it.id == id) transform(it).copy(id = it.id, ownerId = it.ownerId) else it }
        }
    }

    override suspend fun deletePet(id: PetId) {
        store.update(JsonStore.Keys.PETS, serializer, seed) { list -> list.filterNot { it.id == id } }
    }
}
