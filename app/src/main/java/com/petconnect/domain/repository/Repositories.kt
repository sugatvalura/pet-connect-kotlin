package com.petconnect.domain.repository

import com.petconnect.domain.model.AppNotification
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Listing
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.Message
import com.petconnect.domain.model.MessageKind
import com.petconnect.domain.model.NewListing
import com.petconnect.domain.model.NewPet
import com.petconnect.domain.model.NotificationKind
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetId
import com.petconnect.domain.model.PetLocation
import com.petconnect.domain.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/*
 * Repository contracts. The domain owns these interfaces; the data layer
 * implements them (Dependency Inversion). Swapping local storage for a real
 * backend means writing new implementations — no ViewModel changes.
 */

interface AuthRepository {
    val session: Flow<Session?>
    suspend fun signIn(email: String, name: String?): Session
    suspend fun signOut()
}

interface PetRepository {
    /** All pets, already positioned relative to the user (demo relocation). */
    val pets: Flow<List<Pet>>
    val owners: Flow<List<Owner>>
    suspend fun createPet(input: NewPet): Pet
    suspend fun updatePet(id: PetId, transform: (Pet) -> Pet)
    suspend fun deletePet(id: PetId)
}

interface MatchRepository {
    val matches: Flow<List<Match>>
    fun decidedPetIds(fromPetId: PetId): Flow<Set<PetId>>

    /** Records a decision; returns the match when interest is mutual. */
    suspend fun recordInterest(from: PetId, to: PetId, decision: InterestDecision): Match?

    /** Demo-only stand-in for the other owner tapping "interested". */
    suspend fun simulateReciprocalInterest(candidate: PetId, viewerPet: PetId)
    suspend fun updatePreview(matchId: String, preview: String)
    suspend fun resetDecisions(fromPetId: PetId)
}

interface ChatRepository {
    fun messages(matchId: String): Flow<List<Message>>
    suspend fun send(matchId: String, sender: OwnerId, kind: MessageKind, body: String): Message
    val blockedOwners: Flow<Set<OwnerId>>
    suspend fun blockOwner(ownerId: OwnerId)
}

interface ListingRepository {
    val listings: Flow<List<Listing>>
    suspend fun create(input: NewListing): Listing
    suspend fun delete(id: String)
}

interface NotificationRepository {
    val notifications: Flow<List<AppNotification>>
    suspend fun push(kind: NotificationKind, title: String, body: String, route: String? = null)
    suspend fun markRead(id: String)
    suspend fun markAllRead()
    suspend fun clear()
}

data class UserLocation(val location: PetLocation, val isPrecise: Boolean)

interface LocationRepository {
    val location: StateFlow<UserLocation>
    /** Best effort; falls back to a default city and never throws. */
    suspend fun refresh()
}

/** UI-session preferences: which pet you're matching as, and feed filters. */
interface PreferencesRepository {
    val activePetId: Flow<PetId?>
    suspend fun setActivePetId(id: PetId?)
    val filters: StateFlow<PetFilters>
    fun setFilters(filters: PetFilters)
}

enum class AlertSetting(val label: String) {
    MATCHES("Match alerts"),
    NEARBY("Nearby availability"),
    MESSAGES("Message alerts"),
}

/** Per-category alert switches. Default on. */
interface SettingsRepository {
    val alerts: Flow<Map<AlertSetting, Boolean>>
    suspend fun setAlert(setting: AlertSetting, enabled: Boolean)
}

interface ImageRepository {
    /** Copies a picked image into app storage and returns a stable URI string. */
    suspend fun import(sourceUri: String): String?
}

data class AssistantReply(
    val reply: String,
    val blocked: Boolean,
    val intent: String,
    val source: Source,
) {
    enum class Source { REMOTE, LOCAL, GUARDRAIL }
}

interface AssistantRepository {
    suspend fun ask(message: String, userId: String?): AssistantReply
}
