package com.petconnect.domain

import com.petconnect.domain.model.Coordinates
import com.petconnect.domain.util.Geo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GeoTest {
    private val delhi = Coordinates(28.6139, 77.2090)

    @Test fun `distance to self is zero`() {
        assertEquals(0.0, Geo.distanceKm(delhi, delhi), 1e-9)
    }

    @Test fun `delhi to mumbai is about 1150 km`() {
        val mumbai = Coordinates(19.0760, 72.8777)
        assertEquals(1150.0, Geo.distanceKm(delhi, mumbai), 25.0)
    }

    @Test fun `offset by km round-trips through haversine`() {
        val moved = Geo.offsetByKm(delhi, eastKm = 3.0, northKm = 4.0)
        assertEquals(5.0, Geo.distanceKm(delhi, moved), 0.05)
    }

    @Test fun `approximate location stays within the blur radius`() {
        val random = Random(42)
        repeat(200) {
            val blurred = Geo.approximate(delhi, radiusKm = 1.0, random = random)
            // + rounding slack from 4-decimal truncation (~11 m)
            assertTrue(Geo.distanceKm(delhi, blurred) <= 1.02)
        }
    }

    @Test fun `distance formatting switches to metres under 1 km`() {
        assertEquals("450 m away", Geo.formatDistance(0.45))
        assertTrue(Geo.formatDistance(2.4).endsWith("km away"))
    }
}
