package com.petconnect.domain.util

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * Age and date helpers. Ages are always derived from a date of birth, never
 * stored. A [Clock] is injectable so tests can freeze "today".
 */
class Dates(private val clock: Clock = Clock.systemDefaultZone()) {

    fun today(): LocalDate = LocalDate.now(clock)
    fun nowIso(): String = Instant.now(clock).toString()

    fun ageInMonths(dateOfBirth: String): Long {
        val dob = parseOrNull(dateOfBirth) ?: return 0
        return ChronoUnit.MONTHS.between(dob, today()).coerceAtLeast(0)
    }

    fun ageInYears(dateOfBirth: String): Double = ageInMonths(dateOfBirth) / 12.0

    /** "Newborn", "7 months", "2 years". */
    fun formatAge(dateOfBirth: String): String {
        val months = ageInMonths(dateOfBirth)
        return when {
            months < 1 -> "Newborn"
            months < 24 -> "$months ${if (months == 1L) "month" else "months"}"
            else -> (months / 12).let { "$it ${if (it == 1L) "year" else "years"}" }
        }
    }

    /** "10–15 Sep" or "28 Sep – 3 Oct". */
    fun formatDateRange(start: String?, end: String?): String {
        val from = start?.let(::parseOrNull) ?: return "Not set"
        val to = end?.let(::parseOrNull) ?: return "Not set"
        val month = DateTimeFormatter.ofPattern("MMM", Locale.ENGLISH)
        return if (from.month == to.month && from.year == to.year) {
            "${from.dayOfMonth}–${to.dayOfMonth} ${to.format(month)}"
        } else {
            "${from.dayOfMonth} ${from.format(month)} – ${to.dayOfMonth} ${to.format(month)}"
        }
    }

    /** "now", "5m", "3h", "2d", or "12 Sep". */
    fun formatRelative(iso: String): String {
        val then = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        val minutes = Duration.between(then, Instant.now(clock)).toMinutes()
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            minutes < 60 * 24 -> "${minutes / 60}h"
            minutes < 60 * 24 * 7 -> "${minutes / (60 * 24)}d"
            else -> DateTimeFormatter.ofPattern("d MMM", Locale.ENGLISH)
                .format(then.atZone(clock.zone))
        }
    }

    fun formatClock(iso: String): String {
        val then = runCatching { Instant.parse(iso) }.getOrNull() ?: return ""
        return DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH).format(then.atZone(clock.zone))
    }

    /** True when today falls inside the window (inclusive). */
    fun isWithinWindow(start: String?, end: String?): Boolean {
        val from = start?.let(::parseOrNull) ?: return false
        val to = end?.let(::parseOrNull) ?: return false
        val t = today()
        return !t.isBefore(from) && !t.isAfter(to)
    }

    /** Validation shared by every form that takes a date. Returns an error or null. */
    fun validatePastDate(value: String): String? {
        if (!DATE_PATTERN.matches(value)) return "Use the format YYYY-MM-DD."
        val date = parseOrNull(value) ?: return "That date is not valid."
        if (date.isAfter(today())) return "Date cannot be in the future."
        return null
    }

    fun zone(): ZoneId = clock.zone

    companion object {
        val DATE_PATTERN = Regex("""^\d{4}-\d{2}-\d{2}$""")

        fun parseOrNull(value: String): LocalDate? = try {
            LocalDate.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }
}
