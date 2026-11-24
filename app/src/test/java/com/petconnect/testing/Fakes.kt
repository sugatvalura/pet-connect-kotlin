package com.petconnect.testing

import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.Message
import com.petconnect.domain.model.MessageKind
import com.petconnect.domain.model.NewPet
import com.petconnect.domain.model.Owner
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetId
import com.petconnect.domain.model.Session
import com.petconnect.domain.repository.AuthRepository
import com.petconnect.domain.repository.ChatRepository
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.PetRepository
import com.petconnect.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/*
 * In-memory fakes for ViewModel tests.
 *
 * Each fake keeps its data in a MutableStateFlow, so a test can change data
 * mid-test (e.g. `chat.blockOwner(x)`) and watch the ViewModel react, exactly
 * like the real Room / DataStore flows would.
 *
 * Fake vs mock: a fake is a tiny WORKING implementation. Tests read like normal
 * code and don't break when the ViewModel changes internal call order.
 */

class FakeAuthRepository(signedInAs: OwnerId? = null) : AuthRepository {
    override val session = MutableStateFlow(signedInAs?.let { Session(it, "me@test.com", "Me") })

    /** Records calls so tests can assert "signIn was (not) called". */
    val signInCalls = mutableListOf<Pair<String, String?>>()

    override suspend fun signIn(email: String, name: String?): Session {
        signInCalls += email to name
        return Session(OwnerId("owner-$email"), email, name).also { session.value = it }
    }

    override suspend fun signOut() {
        session.value = null
    }
}

class FakePetRepository(pets: List<Pet> = emptyList(), owners: List<Owner> = emptyList()) : PetRepository {
    override val pets = MutableStateFlow(pets)
    override val owners = MutableStateFlow(owners)
    override suspend fun createPet(input: NewPet): Pet = error("not needed in these tests")
    override suspend fun updatePet(id: PetId, transform: (Pet) -> Pet) =
        pets.update { list -> list.map { if (it.id == id) transform(it) else it } }
    override suspend fun deletePet(id: PetId) = pets.update { list -> list.filterNot { it.id == id } }
}

class FakeMatchRepository(matches: List<Match> = emptyList()) : MatchRepository {
    override val matches = MutableStateFlow(matches)
    override fun decidedPetIds(fromPetId: PetId): Flow<Set<PetId>> = flowOf(emptySet())
    override suspend fun recordInterest(from: PetId, to: PetId, decision: InterestDecision): Match? = null
    override suspend fun simulateReciprocalInterest(candidate: PetId, viewerPet: PetId) = Unit
    override suspend fun updatePreview(matchId: String, preview: String) = Unit
    override suspend fun resetDecisions(fromPetId: PetId) = Unit
}

class FakeChatRepository : ChatRepository {
    private val all = MutableStateFlow<List<Message>>(emptyList())
    private val blocked = MutableStateFlow<Set<OwnerId>>(emptySet())

    override fun messages(matchId: String): Flow<List<Message>> =
        all.map { list -> list.filter { it.matchId == matchId } }

    override suspend fun send(matchId: String, sender: OwnerId, kind: MessageKind, body: String): Message =
        Message("m${all.value.size}", matchId, sender, kind, body, "2026-11-19T00:00:00Z")
            .also { m -> all.update { it + m } }

    override val blockedOwners: Flow<Set<OwnerId>> = blocked
    override suspend fun blockOwner(ownerId: OwnerId) = blocked.update { it + ownerId }
}

class FakePreferencesRepository : PreferencesRepository {
    override val activePetId = MutableStateFlow<PetId?>(null)
    override suspend fun setActivePetId(id: PetId?) { activePetId.value = id }
    override val filters = MutableStateFlow(PetFilters())
    override fun setFilters(filters: PetFilters) { this.filters.value = filters }
}
