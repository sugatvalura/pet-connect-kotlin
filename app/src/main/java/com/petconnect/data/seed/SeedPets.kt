package com.petconnect.data.seed

import com.petconnect.domain.model.AvailabilityVisibility
import com.petconnect.domain.model.AvailabilityVisibility.MATCHES_ONLY
import com.petconnect.domain.model.AvailabilityVisibility.NEARBY_OWNERS
import com.petconnect.domain.model.AvailabilityVisibility.PRIVATE
import com.petconnect.domain.model.AvailabilityWindow
import com.petconnect.domain.model.DemoOffsetKm
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetGender.FEMALE
import com.petconnect.domain.model.PetGender.MALE
import com.petconnect.domain.model.PetId
import com.petconnect.domain.model.PetLocation
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.PetSpecies.CAT
import com.petconnect.domain.model.PetSpecies.DOG
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.model.VaccinationStatus.PARTIAL
import com.petconnect.domain.model.VaccinationStatus.VACCINATED
import com.petconnect.domain.usecase.DemoAccount

/**
 * Demo pets. Each carries an `offsetKm` so it is placed around wherever the
 * device actually is (see DemoRelocator). Coordinates are a fallback only.
 */
object SeedPets {
    private val DELHI = PetLocation(28.6139, 77.209, "New Delhi")

    private fun window(start: String, end: String, visibility: AvailabilityVisibility, notes: String? = null) =
        AvailabilityWindow(true, start, end, visibility, notes)

    @Suppress("LongParameterList")
    private fun pet(
        id: String, owner: String, name: String, photos: List<String>, species: PetSpecies, breed: String,
        gender: PetGender, dob: String, offset: Pair<Double, Double>, bio: String,
        vaccination: VaccinationStatus = VACCINATED, neutered: Boolean = false,
        availability: AvailabilityWindow? = null, created: String,
    ) = Pet(
        id = PetId(id),
        ownerId = OwnerId(owner),
        name = name,
        photos = photos.map { photo(it) },
        species = species,
        breed = breed,
        gender = gender,
        dateOfBirth = dob,
        location = DELHI,
        bio = bio,
        vaccination = vaccination,
        isSpayedOrNeutered = neutered,
        availableForBreeding = availability != null,
        availability = availability ?: AvailabilityWindow.Off,
        createdAt = created,
        offsetKm = DemoOffsetKm(offset.first, offset.second),
    )

    val all: List<Pet> = listOf(
        pet("pet-luna", DemoAccount.OWNER_ID.value, "Luna", listOf("photo-1552053831-71594a27632d", "photo-1601758228041-f3b2795255f1"),
            DOG, "Golden Retriever", FEMALE, "2023-04-18", 0.0 to 0.0,
            "Gentle, endlessly patient, and convinced every stranger is a friend.",
            availability = window("2026-09-10", "2026-09-15", MATCHES_ONLY, "Prefer meeting at a park first."),
            created = "2025-02-12T09:00:00Z"),
        pet("pet-bruno", "owner-neha", "Bruno", listOf("photo-1633722715463-d30f4f325e24", "photo-1530281700549-e82e7bf110d6"),
            DOG, "Golden Retriever", MALE, "2023-08-02", 1.4 to -0.8, "Fetch is a religion. Will trade paw for treats.",
            availability = window("2026-09-08", "2026-09-20", NEARBY_OWNERS), created = "2025-01-08T09:00:00Z"),
        pet("pet-simba", "owner-arjun", "Simba", listOf("photo-1548199973-03cce0bbc87b", "photo-1477884213360-7e9d7dcc1e48"),
            DOG, "Indie", MALE, "2022-01-20", -2.1 to 1.6, "Rescued from a Saket parking lot. Now runs the house.",
            neutered = true, created = "2025-03-25T09:00:00Z"),
        pet("pet-misty", "owner-fatima", "Misty", listOf("photo-1595433707802-6b2626ef1c91", "photo-1573865526739-10659fec78a5"),
            CAT, "Persian", FEMALE, "2024-02-14", 3.2 to 2.4, "Regal, judgemental, secretly a lap cat at 2am.",
            availability = window("2026-09-12", "2026-09-22", MATCHES_ONLY), created = "2024-12-02T09:00:00Z"),
        pet("pet-coco", "owner-rohan", "Coco", listOf("photo-1505628346881-b72b27e84530", "photo-1543466835-00a7907e9de1"),
            DOG, "Beagle", FEMALE, "2023-11-05", -1.1 to -3.4, "Nose first, questions later. Escape artist.",
            vaccination = PARTIAL, availability = window("2026-09-14", "2026-09-24", NEARBY_OWNERS),
            created = "2025-05-20T09:00:00Z"),
        pet("pet-oreo", "owner-priya", "Oreo", listOf("photo-1596854407944-bf87f6fdd49e", "photo-1513245543132-31f507417b26"),
            CAT, "Indian Shorthair", MALE, "2024-07-09", 4.8 to -2.2, "Found in a scooter box. Loves shoulders and sunbeams.",
            neutered = true, created = "2025-06-05T09:00:00Z"),
        pet("pet-rocky", "owner-arjun", "Rocky", listOf("photo-1587300003388-59208cc962cb", "photo-1558788353-f76d92427f16"),
            DOG, "Labrador", MALE, "2021-06-30", -5.4 to 3.1, "Senior gentleman. Naps competitively.",
            availability = window("2026-09-09", "2026-09-18", PRIVATE), created = "2025-04-01T09:00:00Z"),
        pet("pet-bella", "owner-neha", "Bella", listOf("photo-1568572933382-74d440642117", "photo-1589941013453-ec89f33b5e95"),
            DOG, "German Shepherd", FEMALE, "2022-09-15", 2.6 to 6.8, "Watchful, whip-smart, obsessed with tennis balls.",
            created = "2025-01-15T09:00:00Z"),
        pet("pet-mocha", "owner-kabir", "Mocha", listOf("photo-1605568427561-40dd23c2acea", "photo-1450778869180-41d0601e046e"),
            DOG, "Siberian Husky", FEMALE, "2023-06-22", -3.4 to 5.2,
            "Talks back. Will howl at sirens, fireworks, the vacuum, and your song.",
            availability = window("2026-09-15", "2026-09-28", NEARBY_OWNERS), created = "2025-04-14T09:00:00Z"),
        pet("pet-teddy", "owner-kabir", "Teddy", listOf("photo-1537151608828-ea2b11777ee8", "photo-1583337130417-3346a1be7dee"),
            DOG, "Shih Tzu", MALE, "2022-10-04", -1.8 to -2.1, "Looks like a mop, moves like a courtier.",
            availability = window("2026-09-09", "2026-09-19", MATCHES_ONLY), created = "2025-04-19T09:00:00Z"),
        pet("pet-snowball", "owner-anjali", "Snowball", listOf("photo-1514888286974-6c03e2ca1dba", "photo-1495360010541-f48722b34f7d"),
            CAT, "Persian", MALE, "2024-12-08", 4.2 to -3.6,
            "Pure white, perpetually unimpressed. Excellent taste in napping spots.",
            availability = window("2026-09-11", "2026-09-21", PRIVATE), created = "2025-03-02T09:00:00Z"),
        pet("pet-pepper", "owner-priya", "Pepper", listOf("photo-1561037404-61cd46aa615b", "photo-1518717758536-85ae29035b6d"),
            DOG, "Indie", FEMALE, "2022-06-14", -3.2 to -1.8,
            "Salty personality, soft heart. Will judge you on sight and love you in a week.",
            availability = window("2026-09-13", "2026-09-23", NEARBY_OWNERS), created = "2025-07-08T09:00:00Z"),
        pet("pet-milo", "owner-rohan", "Milo", listOf("photo-1544568100-847a948585b9", "photo-1560807707-8cc77767d783"),
            DOG, "Corgi", MALE, "2023-09-30", 2.1 to -4.4, "Short legs, massive ambition. Heel-nips included.",
            availability = window("2026-09-10", "2026-09-25", MATCHES_ONLY), created = "2025-06-15T09:00:00Z"),
        pet("pet-max", "owner-arjun", "Max", listOf("photo-1583511655826-05700d52f4d9", "photo-1601758124510-52d02ddb7cbd"),
            DOG, "Indie", MALE, "2025-03-18", -4.5 to 5.6, "Puppy tax: he will eat your socks, then ask for belly rubs.",
            vaccination = PARTIAL, availability = window("2026-09-16", "2026-09-30", NEARBY_OWNERS),
            created = "2025-08-22T09:00:00Z"),
        pet("pet-pippa", "owner-neha", "Pippa", listOf("photo-1516734212186-a967f81ad0d7", "photo-1517849845537-4d257902454a"),
            DOG, "Toy Poodle", FEMALE, "2023-02-11", 5.6 to 7.1, "Tiny, smug, fully aware she runs the household.",
            availability = window("2026-09-12", "2026-09-26", PRIVATE), created = "2025-02-04T09:00:00Z"),
        pet("pet-hazel", "owner-fatima", "Hazel", listOf("photo-1513360371669-4adf3dd7dff8", "photo-1495360010541-f48722b34f7d"),
            CAT, "Siamese", FEMALE, "2023-05-22", 6.4 to 3.2,
            "Sings the song of her people at 5am. Sharp opinions about everything.",
            neutered = true, created = "2025-04-09T09:00:00Z"),
    )
}
