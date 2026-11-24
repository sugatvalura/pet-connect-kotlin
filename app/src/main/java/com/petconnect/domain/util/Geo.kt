package com.petconnect.domain.util

import com.petconnect.domain.model.Coordinates
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Pure geo helpers. No Android imports — trivially unit-testable. */
object Geo {
    private const val EARTH_RADIUS_KM = 6371.0
    private const val KM_PER_DEGREE = 111.32

    private fun Double.toRadians() = this * PI / 180.0

    /** Great-circle (haversine) distance in km. */
    fun distanceKm(a: Coordinates, b: Coordinates): Double {
        val dLat = (b.latitude - a.latitude).toRadians()
        val dLon = (b.longitude - a.longitude).toRadians()
        val lat1 = a.latitude.toRadians()
        val lat2 = b.latitude.toRadians()
        val h = sin(dLat / 2).let { it * it } +
            cos(lat1) * cos(lat2) * sin(dLon / 2).let { it * it }
        return 2 * EARTH_RADIUS_KM * asin(min(1.0, sqrt(h)))
    }

    fun formatDistance(km: Double): String =
        if (km < 1) "${(km * 1000).roundToInt()} m away" else "${"%.1f".format(km)} km away"

    /**
     * Offsets a coordinate by km. Longitude degrees shrink with latitude, so the
     * east component is divided by cos(lat).
     */
    fun offsetByKm(origin: Coordinates, eastKm: Double, northKm: Double): Coordinates {
        val latitude = origin.latitude + northKm / KM_PER_DEGREE
        val cosLat = cos(origin.latitude.toRadians()).let { if (abs(it) < 0.01) 0.01 else it }
        val longitude = origin.longitude + eastKm / (KM_PER_DEGREE * cosLat)
        return Coordinates(latitude, longitude)
    }

    /**
     * Blurs a point to roughly [radiusKm], so a shared chat location never
     * leaks an owner's exact address.
     */
    fun approximate(point: Coordinates, radiusKm: Double = 1.0, random: Random = Random): Coordinates {
        val angle = random.nextDouble() * 2 * PI
        val distance = random.nextDouble() * radiusKm
        val blurred = offsetByKm(point, distance * sin(angle), distance * cos(angle))
        return Coordinates(blurred.latitude.round4(), blurred.longitude.round4())
    }

    private fun Double.round4() = (this * 10_000).roundToInt() / 10_000.0
}
