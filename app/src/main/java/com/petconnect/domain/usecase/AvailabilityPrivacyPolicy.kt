package com.petconnect.domain.usecase

import com.petconnect.domain.model.AvailabilityVisibility
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.Pet

/**
 * The single gate deciding whether a viewer may see a pet's breeding window.
 * Screens never read `pet.availability` dates directly, so the rule can't
 * drift between surfaces.
 */
object AvailabilityPrivacyPolicy {
    fun canView(pet: Pet, viewer: OwnerId, isMatched: Boolean): Boolean {
        if (pet.isOwnedBy(viewer)) return true
        if (!pet.availability.enabled) return false
        return when (pet.availability.visibility) {
            AvailabilityVisibility.PRIVATE -> false
            AvailabilityVisibility.MATCHES_ONLY -> isMatched
            AvailabilityVisibility.NEARBY_OWNERS -> true
        }
    }
}
