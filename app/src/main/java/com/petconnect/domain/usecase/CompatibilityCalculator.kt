package com.petconnect.domain.usecase

import com.petconnect.domain.model.CompatibilityFactor
import com.petconnect.domain.model.CompatibilityResult
import com.petconnect.domain.model.Pet
import com.petconnect.domain.usecase.compatibility.AgeRule
import com.petconnect.domain.usecase.compatibility.AvailabilityRule
import com.petconnect.domain.usecase.compatibility.BreedRule
import com.petconnect.domain.usecase.compatibility.CompatibilityRule
import com.petconnect.domain.usecase.compatibility.DistanceRule
import com.petconnect.domain.usecase.compatibility.GenderRule
import com.petconnect.domain.usecase.compatibility.HealthRule
import com.petconnect.domain.usecase.compatibility.MatchContext
import com.petconnect.domain.util.Dates
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Pet Connect Score: combines explainable factors into 0–100.
 *
 * This class only *combines*; it knows nothing about breeds or ages. The
 * scoring knowledge lives in [CompatibilityRule] strategies (Open/Closed).
 *
 * The secondary constructor lets tests pass any rule list — e.g. a single
 * fake rule — without touching production wiring.
 */
class CompatibilityCalculator(private val rules: List<CompatibilityRule>) {

    @Inject constructor(dates: Dates) : this(
        listOf(BreedRule, AgeRule(dates), GenderRule, DistanceRule, HealthRule, AvailabilityRule),
    )

    operator fun invoke(viewer: Pet?, candidate: Pet, distanceKm: Double): CompatibilityResult {
        val context = MatchContext(viewer, candidate, distanceKm)
        val factors = rules.mapNotNull { it.evaluate(context) }
        return CompatibilityResult(weighted(factors), factors)
    }

    private fun weighted(factors: List<CompatibilityFactor>): Int {
        val total = factors.sumOf { it.weight }
        if (total == 0.0) return 0
        return (factors.sumOf { it.score * it.weight } / total * 100).roundToInt()
    }
}
