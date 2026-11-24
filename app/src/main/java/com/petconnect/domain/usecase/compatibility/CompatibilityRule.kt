package com.petconnect.domain.usecase.compatibility

import com.petconnect.domain.model.CompatibilityFactor
import com.petconnect.domain.model.Pet

/**
 * Everything a rule may look at. Bundling inputs in one object means adding a
 * new input later (say, "same city") doesn't change every rule's signature.
 */
data class MatchContext(val viewer: Pet?, val candidate: Pet, val distanceKm: Double)

/**
 * STRATEGY PATTERN + OPEN/CLOSED PRINCIPLE
 *
 * Each scoring factor is its own class implementing this interface.
 * To add a new factor (e.g. "Temperament") you write ONE new class and add it
 * to the list in CompatibilityCalculator — existing rules are never edited.
 * "Open for extension, closed for modification."
 *
 * SINGLE RESPONSIBILITY: each rule knows exactly one thing (breed, age, …).
 *
 * LISKOV SUBSTITUTION: the calculator treats every rule identically. Any new
 * rule must honour the same contract (score in 0..1, null = not applicable),
 * or swapping it in would break the calculator.
 */
fun interface CompatibilityRule {
    /** Returns null when the rule doesn't apply to this context. */
    fun evaluate(context: MatchContext): CompatibilityFactor?
}
