package com.petconnect.domain.usecase

import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.util.Dates
import javax.inject.Inject

/** Field key → error message. Empty map means valid. */
typealias FieldErrors = Map<String, String>

/** Form validation lives in the domain so it's unit-testable and shared. */
class FormValidator @Inject constructor(private val dates: Dates) {

    fun pet(name: String, breed: String, dateOfBirth: String): FieldErrors = buildMap {
        if (name.isBlank()) put("name", "Give your pet a name.")
        if (breed.isBlank()) put("breed", "Breed helps owners find a good match.")
        dates.validatePastDate(dateOfBirth.trim())?.let { put("dateOfBirth", it) }
    }

    fun listing(
        kind: ListingKind,
        title: String,
        breed: String,
        description: String,
        dateOfBirth: String,
        price: String,
    ): FieldErrors = buildMap {
        if (title.isBlank()) put("title", "Add a short, clear title.")
        if (breed.isBlank()) put("breed", "Breed helps people find this listing.")
        if (description.isBlank()) put("description", "Describe the pet and any requirements.")
        dates.validatePastDate(dateOfBirth.trim())?.let { put("dateOfBirth", it) }
        if (kind == ListingKind.SELL) {
            val value = price.trim().toIntOrNull()
            if (value == null || value <= 0) put("price", "Enter a valid price in rupees.")
        }
    }

    fun availability(enabled: Boolean, start: String, end: String): FieldErrors = buildMap {
        if (!enabled) return@buildMap
        val s = Dates.parseOrNull(start.trim())
        val e = Dates.parseOrNull(end.trim())
        if (!Dates.DATE_PATTERN.matches(start.trim()) || s == null) put("startDate", "Use the format YYYY-MM-DD.")
        if (!Dates.DATE_PATTERN.matches(end.trim()) || e == null) put("endDate", "Use the format YYYY-MM-DD.")
        if (s != null && e != null && e.isBefore(s)) {
            put("endDate", "The end date must be on or after the start date.")
        }
    }

    fun login(email: String, password: String): FieldErrors = buildMap {
        if (!EMAIL.matches(email.trim())) put("email", "Enter a valid email address.")
        if (password.length < 4) put("password", "Use at least 4 characters.")
    }

    companion object {
        private val EMAIL = Regex("""^[^@\s]+@[^@\s]+\.[^@\s]+$""")
    }
}
