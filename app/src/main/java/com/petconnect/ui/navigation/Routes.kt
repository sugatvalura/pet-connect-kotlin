package com.petconnect.ui.navigation

import kotlinx.serialization.Serializable

/** Type-safe destinations. Arguments are compile-checked, not string-built. */
sealed interface Route {
    @Serializable data object Login : Route
    @Serializable data object Signup : Route
    @Serializable data object Main : Route

    @Serializable data class PetDetail(val petId: String) : Route
    @Serializable data object NewPet : Route
    @Serializable data class Availability(val petId: String) : Route
    @Serializable data class Chat(val matchId: String) : Route
    @Serializable data class ListingDetail(val listingId: String) : Route
    @Serializable data object NewListing : Route
    @Serializable data class OwnerProfile(val ownerId: String) : Route
    @Serializable data object Notifications : Route
    @Serializable data object Assistant : Route
    @Serializable data object Settings : Route
    @Serializable data object Support : Route
}

/** Bottom tabs inside [Route.Main]. */
enum class Tab(val label: String) { NEARBY("Nearby"), MATCH("Match"), ADOPT("Adopt"), CHATS("Chats"), PROFILE("Profile") }

/**
 * Notifications store a portable string route ("chat/<id>") so the domain
 * never depends on navigation types. This maps it back.
 */
fun parseDeepRoute(route: String): Route? {
    val (head, arg) = route.split('/', limit = 2).let { it[0] to it.getOrNull(1) }
    return when (head) {
        "chat" -> arg?.let(Route::Chat)
        "pet" -> arg?.let(Route::PetDetail)
        "listing" -> arg?.let(Route::ListingDetail)
        else -> null
    }
}
