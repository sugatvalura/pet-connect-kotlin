package com.petconnect.domain

import com.petconnect.domain.model.CompatibilityResult
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.model.CompatibilityFactor
import com.petconnect.domain.usecase.CompatibilityCalculator
import com.petconnect.domain.usecase.compatibility.CompatibilityRule
import com.petconnect.domain.usecase.compatibility.Weights
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompatibilityCalculatorTest {
    private val calc = CompatibilityCalculator(fixedDates)
    private val mine = pet(id = "mine", owner = ME, gender = PetGender.MALE)

    @Test fun `weights sum to one`() {
        with(Weights) {
            assertEquals(1.0, BREED + AGE + GENDER + DISTANCE + HEALTH, 1e-9)
        }
    }

    @Test fun `ideal pair scores 100`() {
        val result = calc(mine, pet(gender = PetGender.FEMALE), distanceKm = 1.0)
        assertEquals(100, result.score)
        assertEquals(CompatibilityResult.Band.HIGH, result.band)
    }

    @Test fun `different species zeroes the breed factor`() {
        val result = calc(mine, pet(species = PetSpecies.CAT, breed = "Persian"), 1.0)
        assertEquals(0.0, result.factors.first { it.label == "Breed" }.score, 0.0)
        assertTrue(result.score < 100)
    }

    @Test fun `same gender scores lower than complementary`() {
        val same = calc(mine, pet(gender = PetGender.MALE), 1.0).score
        val complementary = calc(mine, pet(gender = PetGender.FEMALE), 1.0).score
        assertTrue(same < complementary)
    }

    @Test fun `distance tapers to zero at 30 km`() {
        val far = calc(mine, pet(), 30.0)
        assertEquals(0.0, far.factors.first { it.label == "Distance" }.score, 1e-9)
        assertTrue(calc(mine, pet(), 10.0).score > far.score)
    }

    @Test fun `unvaccinated candidate lowers the score`() {
        val vaccinated = calc(mine, pet(), 1.0).score
        val not = calc(mine, pet(vaccination = VaccinationStatus.NOT_VACCINATED), 1.0).score
        assertTrue(not < vaccinated)
    }

    @Test fun `viewer without a pet still gets a three-factor score`() {
        val result = calc(null, pet(), 1.0)
        assertEquals(listOf("Distance", "Health", "Availability"), result.factors.map { it.label })
        assertEquals(100, result.score)
    }

    /** OPEN/CLOSED in action: a brand-new rule, zero edits to existing code. */
    @Test fun `new rule plugs in without modifying the calculator`() {
        val alwaysHalf = CompatibilityRule { CompatibilityFactor("Temperament", 0.5, 1.0, "Calm") }
        val result = CompatibilityCalculator(listOf(alwaysHalf))(mine, pet(), 1.0)
        assertEquals(50, result.score)
        assertEquals("Temperament", result.factors.single().label)
    }

    @Test fun `no applicable rules gives zero rather than crashing`() {
        val neverApplies = CompatibilityRule { null }
        assertEquals(0, CompatibilityCalculator(listOf(neverApplies))(mine, pet(), 1.0).score)
    }
}
