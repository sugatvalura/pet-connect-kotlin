package com.petconnect.data.seed

import com.petconnect.domain.model.DemoOffsetKm
import com.petconnect.domain.model.PetLocation
import com.petconnect.domain.util.Geo

/**
 * Demo-only: places seed records around the user's real location so
 * "2.4 km away" is believable in any city. Applied on every read (never
 * written back), so demo pets follow the user. Records the user created have
 * no offset and are left untouched. Delete when a backend supplies real data.
 */
object DemoRelocator {
    private val AREAS = listOf("Nearby", "North side", "East side", "South side", "West side", "Uptown", "Downtown", "Riverside")
    private val COORDINATE_LABEL = Regex("""^-?\d+\.\d+,\s*-?\d+\.\d+$""")

    fun <T> relocate(
        records: List<T>,
        user: PetLocation,
        offsetOf: (T) -> DemoOffsetKm?,
        withLocation: (T, PetLocation) -> T,
    ): List<T> {
        val city = when {
            COORDINATE_LABEL.matches(user.label) -> ""
            ',' in user.label -> user.label.substringAfterLast(',').trim()
            else -> user.label
        }
        return records.mapIndexed { index, record ->
            val offset = offsetOf(record) ?: return@mapIndexed record
            val coords = Geo.offsetByKm(user.coordinates, offset.east, offset.north)
            val label = if (offset.east == 0.0 && offset.north == 0.0) {
                user.label
            } else {
                val area = AREAS[index % AREAS.size]
                if (city.isBlank()) area else "$area, $city"
            }
            withLocation(record, PetLocation(coords.latitude, coords.longitude, label))
        }
    }
}
