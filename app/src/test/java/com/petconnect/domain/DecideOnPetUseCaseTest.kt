package com.petconnect.domain

import com.petconnect.domain.model.AppNotification
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.NotificationKind
import com.petconnect.domain.model.PetId
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.NotificationRepository
import com.petconnect.domain.usecase.DecideOnPetUseCase
import com.petconnect.domain.usecase.DecisionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * DEPENDENCY INVERSION, demonstrated.
 *
 * DecideOnPetUseCase depends on the MatchRepository / NotificationRepository
 * *interfaces*, not on DataStore. So in tests we hand it tiny in-memory fakes:
 * no Android, no disk, no mocking library — and the test runs in milliseconds.
 */
class DecideOnPetUseCaseTest {

    /** Hand-written fake. Records what happened so the test can assert on it. */
    private class FakeMatches(private val mutual: Boolean) : MatchRepository {
        val recorded = mutableListOf<Triple<PetId, PetId, InterestDecision>>()
        override val matches: Flow<List<Match>> = flowOf(emptyList())
        override fun decidedPetIds(fromPetId: PetId): Flow<Set<PetId>> = flowOf(emptySet())
        override suspend fun recordInterest(from: PetId, to: PetId, decision: InterestDecision): Match? {
            recorded += Triple(from, to, decision)
            return if (mutual && decision == InterestDecision.INTERESTED) Match("m1", listOf(from, to), "now") else null
        }
        override suspend fun simulateReciprocalInterest(candidate: PetId, viewerPet: PetId) = Unit
        override suspend fun updatePreview(matchId: String, preview: String) = Unit
        override suspend fun resetDecisions(fromPetId: PetId) = Unit
    }

    private class FakeNotifications : NotificationRepository {
        val pushed = mutableListOf<Pair<NotificationKind, String?>>()
        override val notifications = MutableStateFlow<List<AppNotification>>(emptyList())
        override suspend fun push(kind: NotificationKind, title: String, body: String, route: String?) {
            pushed += kind to route
        }
        override suspend fun markRead(id: String) = Unit
        override suspend fun markAllRead() = Unit
        override suspend fun clear() = Unit
    }

    private val mine = pet(id = "mine", owner = ME)
    private val theirs = pet(id = "theirs")

    @Test fun `no active pet short-circuits without touching repositories`() = runTest {
        val matches = FakeMatches(mutual = true)
        val result = DecideOnPetUseCase(matches, FakeNotifications())(null, theirs, InterestDecision.INTERESTED)
        assertEquals(DecisionResult.NoActivePet, result)
        assertTrue(matches.recorded.isEmpty())
    }

    @Test fun `one-sided interest is recorded but no notification sent`() = runTest {
        val notifications = FakeNotifications()
        val result = DecideOnPetUseCase(FakeMatches(mutual = false), notifications)(mine, theirs, InterestDecision.INTERESTED)
        assertEquals(DecisionResult.Recorded, result)
        assertTrue(notifications.pushed.isEmpty())
    }

    @Test fun `mutual interest creates a match and deep-links the notification to chat`() = runTest {
        val notifications = FakeNotifications()
        val result = DecideOnPetUseCase(FakeMatches(mutual = true), notifications)(mine, theirs, InterestDecision.INTERESTED)

        assertTrue(result is DecisionResult.Matched)
        assertEquals(listOf(NotificationKind.MATCH to "chat/m1"), notifications.pushed)
    }

    @Test fun `skipping never matches`() = runTest {
        val result = DecideOnPetUseCase(FakeMatches(mutual = true), FakeNotifications())(mine, theirs, InterestDecision.SKIPPED)
        assertEquals(DecisionResult.Recorded, result)
    }
}
