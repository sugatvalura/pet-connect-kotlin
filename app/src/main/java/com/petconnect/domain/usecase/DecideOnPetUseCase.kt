package com.petconnect.domain.usecase

import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.Match
import com.petconnect.domain.model.NotificationKind
import com.petconnect.domain.model.Pet
import com.petconnect.domain.repository.MatchRepository
import com.petconnect.domain.repository.NotificationRepository
import javax.inject.Inject

sealed interface DecisionResult {
    data object NoActivePet : DecisionResult
    data object Recorded : DecisionResult
    data class Matched(val match: Match, val theirPet: Pet, val myPet: Pet) : DecisionResult
}

/** Swipe right/left. A match only exists when interest is mutual. */
class DecideOnPetUseCase @Inject constructor(
    private val matches: MatchRepository,
    private val notifications: NotificationRepository,
) {
    suspend operator fun invoke(myPet: Pet?, candidate: Pet, decision: InterestDecision): DecisionResult {
        if (myPet == null) return DecisionResult.NoActivePet

        // Demo: other owners have no device to tap "interested" on, so they reciprocate.
        // A real backend deletes this line and receives their tap instead.
        if (decision == InterestDecision.INTERESTED) {
            matches.simulateReciprocalInterest(candidate.id, myPet.id)
        }

        val match = matches.recordInterest(myPet.id, candidate.id, decision)
            ?: return DecisionResult.Recorded

        notifications.push(
            kind = NotificationKind.MATCH,
            title = "It's a match! 🎉",
            body = "${myPet.name} and ${candidate.name} are both interested. Say hello!",
            route = "chat/${match.id}",
        )
        return DecisionResult.Matched(match, candidate, myPet)
    }
}
