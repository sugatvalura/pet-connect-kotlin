package com.petconnect.domain.usecase.compatibility

import com.petconnect.domain.model.CompatibilityFactor
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.util.Dates
import kotlin.math.abs
import kotlin.math.max

/*
 * The five (+1) scoring strategies. Each is tiny, pure and independently testable.
 * Weights must sum to 1 for the "with a viewer pet" set.
 */

object Weights {
    const val BREED = 0.25
    const val AGE = 0.2
    const val GENDER = 0.25
    const val DISTANCE = 0.2
    const val HEALTH = 0.1
    const val AVAILABILITY = 0.2
}

/** Pet-vs-pet rule: only applies when the viewer has a pet. */
object BreedRule : CompatibilityRule {
    override fun evaluate(context: MatchContext): CompatibilityFactor? {
        val viewer = context.viewer ?: return null
        val candidate = context.candidate
        return when {
            viewer.species != candidate.species ->
                CompatibilityFactor("Breed", 0.0, Weights.BREED, "Different species")
            viewer.breed.equals(candidate.breed, ignoreCase = true) ->
                CompatibilityFactor("Breed", 1.0, Weights.BREED, "Both ${candidate.breed}")
            else ->
                CompatibilityFactor("Breed", 0.55, Weights.BREED, "${viewer.breed} · ${candidate.breed}")
        }
    }
}

/** Needs [Dates] — a dependency injected through the constructor (DIP). */
class AgeRule(private val dates: Dates) : CompatibilityRule {
    override fun evaluate(context: MatchContext): CompatibilityFactor? {
        val viewer = context.viewer ?: return null
        val gap = abs(dates.ageInYears(viewer.dateOfBirth) - dates.ageInYears(context.candidate.dateOfBirth))
        val score = max(0.0, 1 - gap / 4) // 4+ years apart scores zero
        val detail = if (gap < 1) "Similar age" else "${"%.1f".format(gap)} year gap"
        return CompatibilityFactor("Age", score, Weights.AGE, detail)
    }
}

object GenderRule : CompatibilityRule {
    override fun evaluate(context: MatchContext): CompatibilityFactor? {
        val viewer = context.viewer ?: return null
        return if (viewer.gender.complements(context.candidate.gender)) {
            CompatibilityFactor("Gender", 1.0, Weights.GENDER, "Complementary pair")
        } else {
            CompatibilityFactor("Gender", 0.15, Weights.GENDER, "Same gender")
        }
    }
}

/** Always applies. Full marks under 2 km, tapering to zero at 30 km. */
object DistanceRule : CompatibilityRule {
    override fun evaluate(context: MatchContext): CompatibilityFactor {
        val km = context.distanceKm
        val score = if (km <= 2) 1.0 else max(0.0, 1 - (km - 2) / 28)
        return CompatibilityFactor("Distance", score, Weights.DISTANCE, "${"%.1f".format(km)} km apart")
    }
}

object HealthRule : CompatibilityRule {
    override fun evaluate(context: MatchContext): CompatibilityFactor = when (context.candidate.vaccination) {
        VaccinationStatus.VACCINATED -> CompatibilityFactor("Health", 1.0, Weights.HEALTH, "Fully vaccinated")
        VaccinationStatus.PARTIAL -> CompatibilityFactor("Health", 0.5, Weights.HEALTH, "Partially vaccinated")
        VaccinationStatus.NOT_VACCINATED -> CompatibilityFactor("Health", 0.1, Weights.HEALTH, "Not vaccinated")
    }
}

/** The opposite of the pet-vs-pet rules: only used when the viewer has NO pet. */
object AvailabilityRule : CompatibilityRule {
    override fun evaluate(context: MatchContext): CompatibilityFactor? {
        if (context.viewer != null) return null
        val open = context.candidate.availableForBreeding
        return CompatibilityFactor(
            label = "Availability",
            score = if (open) 1.0 else 0.4,
            weight = Weights.AVAILABILITY,
            detail = if (open) "Open to matches" else "Not seeking matches",
        )
    }
}
