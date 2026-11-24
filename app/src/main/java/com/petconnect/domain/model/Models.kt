package com.petconnect.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val id: OwnerId,
    val name: String,
    val avatar: String?,
    val city: String,
    val isVerified: Boolean,
    val bio: String,
    val joinedAt: String,
)

@Serializable
enum class InterestDecision {
    @SerialName("interested") INTERESTED,
    @SerialName("skipped") SKIPPED,
}

@Serializable
data class Interest(
    val id: String,
    val fromPetId: PetId,
    val toPetId: PetId,
    val decision: InterestDecision,
    val createdAt: String,
)

@Serializable
data class Match(
    val id: String,
    val petIds: List<PetId>,
    val createdAt: String,
    val lastMessagePreview: String? = null,
    val lastMessageAt: String? = null,
    val unreadCount: Int = 0,
) {
    fun involves(petId: PetId): Boolean = petId in petIds
    fun otherPet(mine: Set<PetId>): PetId? = petIds.firstOrNull { it !in mine }
}

@Serializable
enum class MessageKind {
    @SerialName("text") TEXT,
    @SerialName("image") IMAGE,
    @SerialName("location") LOCATION;

    /** Short text for the conversation list. */
    fun preview(body: String): String = when (this) {
        TEXT -> body
        IMAGE -> "📷 Photo"
        LOCATION -> "📍 Location"
    }
}

@Serializable
data class Message(
    val id: String,
    val matchId: String,
    val senderId: OwnerId,
    val kind: MessageKind,
    /** Text, image URI, or "lat,lng" depending on [kind]. */
    val body: String,
    val createdAt: String,
)

@Serializable
enum class ListingKind(val label: String) {
    @SerialName("adopt") ADOPT("Adopt"),
    @SerialName("sell") SELL("For sale"),
}

@Serializable
data class Listing(
    val id: String,
    val ownerId: OwnerId,
    val kind: ListingKind,
    val title: String,
    val photos: List<String>,
    val species: PetSpecies,
    val breed: String,
    val gender: PetGender,
    val dateOfBirth: String,
    val location: PetLocation,
    /** Null for adoption. Rupees for sales. */
    val price: Int?,
    val description: String,
    val vaccination: VaccinationStatus,
    val createdAt: String,
    val offsetKm: DemoOffsetKm? = null,
)

data class NewListing(
    val ownerId: OwnerId,
    val kind: ListingKind,
    val title: String,
    val photos: List<String>,
    val species: PetSpecies,
    val breed: String,
    val gender: PetGender,
    val dateOfBirth: String,
    val location: PetLocation,
    val price: Int?,
    val description: String,
    val vaccination: VaccinationStatus,
)

@Serializable
enum class NotificationKind {
    @SerialName("match") MATCH,
    @SerialName("message") MESSAGE,
    @SerialName("nearby_available") NEARBY_AVAILABLE,
    @SerialName("availability_reminder") AVAILABILITY_REMINDER,
    @SerialName("listing_response") LISTING_RESPONSE,
}

@Serializable
data class AppNotification(
    val id: String,
    val kind: NotificationKind,
    val title: String,
    val body: String,
    val createdAt: String,
    val read: Boolean = false,
    /** In-app route to open when tapped. */
    val route: String? = null,
)

@Serializable
data class Session(val ownerId: OwnerId, val email: String, val name: String?)

/** Home-feed filter state. `null` species/gender means "all". */
data class PetFilters(
    val maxDistanceKm: Int = 25,
    val species: PetSpecies? = null,
    val breed: String? = null,
    val gender: PetGender? = null,
    val minAgeYears: Int = 0,
    val maxAgeYears: Int = 15,
    val availableForBreedingOnly: Boolean = false,
) {
    val activeCount: Int
        get() = listOf(
            maxDistanceKm != DEFAULT.maxDistanceKm,
            species != null,
            gender != null,
            breed != null,
            availableForBreedingOnly,
            minAgeYears != DEFAULT.minAgeYears || maxAgeYears != DEFAULT.maxAgeYears,
        ).count { it }

    companion object {
        val DEFAULT = PetFilters()
        val DISTANCE_OPTIONS = listOf(2, 5, 10, 25, 50)
    }
}

data class CompatibilityFactor(
    val label: String,
    /** 0..1 */
    val score: Double,
    val weight: Double,
    val detail: String,
)

data class CompatibilityResult(val score: Int, val factors: List<CompatibilityFactor>) {
    val band: Band
        get() = when {
            score >= 75 -> Band.HIGH
            score >= 50 -> Band.MEDIUM
            else -> Band.LOW
        }

    enum class Band { HIGH, MEDIUM, LOW }
}

/** A pet decorated with values computed relative to the viewer. */
data class PetWithContext(
    val pet: Pet,
    val owner: Owner?,
    val distanceKm: Double,
    val compatibility: CompatibilityResult,
)
