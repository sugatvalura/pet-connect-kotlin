package com.petconnect.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Who may see a pet's breeding availability window. Defaults to [PRIVATE]. */
@Serializable
enum class AvailabilityVisibility(val label: String, val hint: String) {
    @SerialName("private")
    PRIVATE("Only me", "Nobody else can see these dates."),

    @SerialName("matches_only")
    MATCHES_ONLY("Owners I match with", "Shared only after you both show interest."),

    @SerialName("nearby_owners")
    NEARBY_OWNERS("Nearby owners", "Visible to verified owners close to you."),
}

/**
 * A breeding availability window. Reproductive details are sensitive, so reads
 * must go through [com.petconnect.domain.usecase.AvailabilityPrivacyPolicy].
 */
@Serializable
data class AvailabilityWindow(
    val enabled: Boolean = false,
    /** ISO date, YYYY-MM-DD. */
    val startDate: String? = null,
    val endDate: String? = null,
    val visibility: AvailabilityVisibility = AvailabilityVisibility.PRIVATE,
    val notes: String? = null,
) {
    companion object {
        val Off = AvailabilityWindow()
    }
}
