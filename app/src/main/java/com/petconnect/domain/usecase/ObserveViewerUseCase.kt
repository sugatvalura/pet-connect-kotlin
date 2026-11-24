package com.petconnect.domain.usecase

import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet
import com.petconnect.domain.repository.AuthRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject

/** Who is looking at the app, and which of their pets drives matching. */
data class Viewer(
    val ownerId: OwnerId,
    val myPets: List<Pet>,
    val activePet: Pet?,
)

object DemoAccount {
    /** The demo build has a single local account; seed data references it. */
    val OWNER_ID = OwnerId("owner-me")
}

class ObserveViewerUseCase @Inject constructor(
    private val auth: AuthRepository,
    private val pets: PetRepository,
    private val prefs: PreferencesRepository,
) {
    operator fun invoke(): Flow<Viewer> = combine(
        auth.session,
        pets.pets,
        prefs.activePetId,
    ) { session, allPets, activeId ->
        val ownerId = session?.ownerId ?: DemoAccount.OWNER_ID
        val mine = allPets.filter { it.isOwnedBy(ownerId) }
        // Default to the first own pet if the stored choice is gone or unset.
        val active = mine.firstOrNull { it.id == activeId } ?: mine.firstOrNull()
        Viewer(ownerId, mine, active)
    }.distinctUntilChanged()
}
