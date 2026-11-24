package com.petconnect.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * ════════════════════════════════════════════════════════════════════════════
 *   THE DOMAIN LAYER  —  "Where the business rules live and Android can't follow"
 * ════════════════════════════════════════════════════════════════════════════
 *
 *  Rule #1 of this package: no `import android.*`. Ever. Not even a tiny one.
 *  Not even if it asks nicely with puppy eyes.
 *
 *  Why? Because the domain is the *heart* of the app. The UI is a costume and the
 *  database is a closet; you can swap both without the heart noticing. That's
 *  Clean Architecture's Dependency Rule: source code dependencies only point
 *  INWARD. UI → domain ← data. The domain depends on nobody. It is the cat of
 *  layers: aloof, self-sufficient, and judging you.
 *
 *  These models are immutable `data class`es. Want to "change" a pet? `copy()` it.
 *  Mutable shared state is how bugs get litters.
 */

/** Strongly-typed ID. You can no longer pass an OwnerId where a PetId goes. The compiler is now your vet. */
@JvmInline
@Serializable
value class PetId(val value: String)

@JvmInline
@Serializable
value class OwnerId(val value: String)

@Serializable
enum class PetSpecies(val label: String, val emoji: String) {
    @SerialName("dog") DOG("Dog", "🐕"),
    @SerialName("cat") CAT("Cat", "🐈"),
    @SerialName("other") OTHER("Other", "🐾"),
}

@Serializable
enum class PetGender(val label: String) {
    @SerialName("male") MALE("Male"),
    @SerialName("female") FEMALE("Female");

    /** Opposites attract. At least in the compatibility algorithm. */
    fun complements(other: PetGender): Boolean = this != other
}

@Serializable
enum class VaccinationStatus(val label: String) {
    @SerialName("vaccinated") VACCINATED("Vaccinated"),
    @SerialName("partial") PARTIAL("Partly vaccinated"),
    @SerialName("not_vaccinated") NOT_VACCINATED("Not vaccinated"),
}

/**
 * Pure coordinates. A point on a sphere, with no opinion about street names.
 */
@Serializable
data class Coordinates(val latitude: Double, val longitude: Double)

/** Coordinates that know what humans call them ("Hauz Khas, Delhi"). */
@Serializable
data class PetLocation(
    val latitude: Double,
    val longitude: Double,
    val label: String,
) {
    val coordinates: Coordinates get() = Coordinates(latitude, longitude)
}

/**
 * Demo-only offset from the user, in km. Lets seed pets orbit wherever the
 * device actually is, so "2.4 km away" is true in Delhi *and* in a California
 * emulator. Real pets have `null` here and stay exactly where they are.
 */
@Serializable
data class DemoOffsetKm(val east: Double, val north: Double)

/**
 * The pet. The star of the show. The reason we're all here.
 *
 * Note: there is no `age` field. Age is DERIVED from [dateOfBirth] — storing it
 * would mean it silently rots by one year every birthday. Derived data never lies.
 */
@Serializable
data class Pet(
    val id: PetId,
    val ownerId: OwnerId,
    val name: String,
    val photos: List<String>,
    val species: PetSpecies,
    val breed: String,
    val gender: PetGender,
    /** ISO date, YYYY-MM-DD. */
    val dateOfBirth: String,
    val location: PetLocation,
    val bio: String,
    val vaccination: VaccinationStatus,
    val isSpayedOrNeutered: Boolean,
    val availableForBreeding: Boolean,
    val availability: AvailabilityWindow,
    val createdAt: String,
    val offsetKm: DemoOffsetKm? = null,
) {
    fun isOwnedBy(owner: OwnerId): Boolean = ownerId == owner
}

/**
 * Everything needed to create a pet, minus what the system assigns (id, createdAt).
 * A separate type (not "Pet with nullable id") so you can't accidentally save a
 * half-born pet. Make illegal states unrepresentable — the best bug is the one
 * that can't compile.
 */
data class NewPet(
    val ownerId: OwnerId,
    val name: String,
    val photos: List<String>,
    val species: PetSpecies,
    val breed: String,
    val gender: PetGender,
    val dateOfBirth: String,
    val location: PetLocation,
    val bio: String,
    val vaccination: VaccinationStatus,
    val isSpayedOrNeutered: Boolean,
    val availableForBreeding: Boolean,
    val availability: AvailabilityWindow,
)
