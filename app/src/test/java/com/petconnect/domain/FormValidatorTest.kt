package com.petconnect.domain

import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.usecase.FormValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormValidatorTest {
    private val v = FormValidator(fixedDates)

    @Test fun `valid pet has no errors`() {
        assertTrue(v.pet("Bruno", "Beagle", "2022-05-01").isEmpty())
    }

    @Test fun `blank pet fields are flagged`() {
        assertEquals(setOf("name", "breed", "dateOfBirth"), v.pet(" ", "", "").keys)
    }

    @Test fun `future birth date is rejected`() {
        assertTrue("dateOfBirth" in v.pet("Bruno", "Beagle", "2030-01-01"))
    }

    @Test fun `price is only required for sale listings`() {
        assertTrue(v.listing(ListingKind.ADOPT, "Pup", "Beagle", "Sweet", "2025-01-01", "").isEmpty())
        assertTrue("price" in v.listing(ListingKind.SELL, "Pup", "Beagle", "Sweet", "2025-01-01", ""))
        assertTrue("price" in v.listing(ListingKind.SELL, "Pup", "Beagle", "Sweet", "2025-01-01", "0"))
        assertTrue(v.listing(ListingKind.SELL, "Pup", "Beagle", "Sweet", "2025-01-01", "15000").isEmpty())
    }

    @Test fun `disabled availability skips date checks`() {
        assertTrue(v.availability(false, "", "").isEmpty())
    }

    @Test fun `availability end must not precede start`() {
        assertTrue(v.availability(true, "2026-12-01", "2026-12-10").isEmpty())
        assertTrue("endDate" in v.availability(true, "2026-12-10", "2026-12-01"))
        assertTrue("startDate" in v.availability(true, "12/01/2026", "2026-12-10"))
    }

    @Test fun `login requires email shape and 4-char password`() {
        assertTrue(v.login("a@b.co", "1234").isEmpty())
        assertEquals(setOf("email", "password"), v.login("nope", "123").keys)
    }
}
