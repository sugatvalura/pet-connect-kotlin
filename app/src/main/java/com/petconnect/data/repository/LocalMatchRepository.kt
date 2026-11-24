package com.petconnect.data.repository

import com.petconnect.data.local.IdGenerator
import com.petconnect.data.local.JsonStore
import com.petconnect.domain.model.Interest
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.PetId
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalMatchRepository @Inject constructor(
    private val store: JsonStore,
    private val ids: IdGenerator,
    private val dates: Dates,
) : MatchRepository {

    private val interestSerializer = ListSerializer(Interest.serializer())
    private val matchSerializer = ListSerializer(Match.serializer())
    private val none = { emptyList<Interest>() }
    private val noMatches = { emptyList<Match>() }

    override val matches: Flow<List<Match>> =
        store.observe(JsonStore.Keys.MATCHES, matchSerializer, noMatches)
            .map { list -> list.sortedByDescending { it.lastMessageAt ?: it.createdAt } }

    override fun decidedPetIds(fromPetId: PetId): Flow<Set<PetId>> =
        store.observe(JsonStore.Keys.INTERESTS, interestSerializer, none)
            .map { list -> list.filter { it.fromPetId == fromPetId }.map { it.toPetId }.toSet() }
            .distinctUntilChanged()

    override suspend fun recordInterest(from: PetId, to: PetId, decision: InterestDecision): Match? {
        val interest = Interest(ids.next("interest"), from, to, decision, dates.nowIso())
        // Replace any earlier decision for this pair, so re-deciding is idempotent.
        val all = store.update(JsonStore.Keys.INTERESTS, interestSerializer, none) { list ->
            list.filterNot { it.fromPetId == from && it.toPetId == to } + interest
        }
        if (decision == InterestDecision.SKIPPED) return null
        val mutual = all.any {
            it.fromPetId == to && it.toPetId == from && it.decision == InterestDecision.INTERESTED
        }
        return if (mutual) createMatch(from, to) else null
    }

    private suspend fun createMatch(a: PetId, b: PetId): Match {
        var result: Match? = null
        store.update(JsonStore.Keys.MATCHES, matchSerializer, noMatches) { list ->
            val existing = list.firstOrNull { it.involves(a) && it.involves(b) }
            if (existing != null) {
                result = existing
                list
            } else {
                val match = Match(ids.next("match"), listOf(a, b), dates.nowIso())
                result = match
                listOf(match) + list
            }
        }
        return checkNotNull(result)
    }

    override suspend fun simulateReciprocalInterest(candidate: PetId, viewerPet: PetId) {
        store.update(JsonStore.Keys.INTERESTS, interestSerializer, none) { list ->
            if (list.any { it.fromPetId == candidate && it.toPetId == viewerPet }) {
                list
            } else {
                list + Interest(ids.next("interest"), candidate, viewerPet, InterestDecision.INTERESTED, dates.nowIso())
            }
        }
    }

    override suspend fun updatePreview(matchId: String, preview: String) {
        store.update(JsonStore.Keys.MATCHES, matchSerializer, noMatches) { list ->
            list.map { if (it.id == matchId) it.copy(lastMessagePreview = preview, lastMessageAt = dates.nowIso()) else it }
        }
    }

    /** Clears this pet's decisions so the deck refills. Matches are kept. */
    override suspend fun resetDecisions(fromPetId: PetId) {
        store.update(JsonStore.Keys.INTERESTS, interestSerializer, none) { list ->
            list.filterNot { it.fromPetId == fromPetId }
        }
    }
}
