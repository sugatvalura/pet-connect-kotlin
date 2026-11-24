package com.petconnect.domain

import com.petconnect.domain.model.AvailabilityVisibility
import com.petconnect.domain.model.AvailabilityWindow
import com.petconnect.domain.usecase.AvailabilityPrivacyPolicy
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AvailabilityPrivacyPolicyTest {

    private fun windowed(visibility: AvailabilityVisibility, enabled: Boolean = true) =
        pet(availability = AvailabilityWindow(enabled, "2026-12-01", "2026-12-10", visibility))

    @Test fun `owner always sees their own pet's window`() {
        val own = pet(owner = ME, availability = AvailabilityWindow(false, visibility = AvailabilityVisibility.PRIVATE))
        assertTrue(AvailabilityPrivacyPolicy.canView(own, ME, isMatched = false))
    }

    @Test fun `disabled window is hidden from others even if public`() {
        assertFalse(AvailabilityPrivacyPolicy.canView(windowed(AvailabilityVisibility.NEARBY_OWNERS, enabled = false), ME, true))
    }

    @Test fun `private window is hidden even from matches`() {
        assertFalse(AvailabilityPrivacyPolicy.canView(windowed(AvailabilityVisibility.PRIVATE), ME, isMatched = true))
    }

    @Test fun `matches-only window requires a match`() {
        val p = windowed(AvailabilityVisibility.MATCHES_ONLY)
        assertFalse(AvailabilityPrivacyPolicy.canView(p, ME, isMatched = false))
        assertTrue(AvailabilityPrivacyPolicy.canView(p, ME, isMatched = true))
    }

    @Test fun `nearby-owners window is visible to anyone`() {
        assertTrue(AvailabilityPrivacyPolicy.canView(windowed(AvailabilityVisibility.NEARBY_OWNERS), ME, isMatched = false))
    }
}
