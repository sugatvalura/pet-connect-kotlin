package com.petconnect.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.petconnect.domain.model.PetLocation
import com.petconnect.domain.repository.LocationRepository
import com.petconnect.domain.repository.UserLocation
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Device location with a graceful fallback. Location is a convenience, not a
 * gate: without permission the app works from a default city.
 */
@Singleton
class DeviceLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : LocationRepository {

    private val _location = MutableStateFlow(UserLocation(FALLBACK, isPrecise = false))
    override val location: StateFlow<UserLocation> = _location.asStateFlow()

    override suspend fun refresh() {
        if (!hasPermission()) {
            _location.value = UserLocation(FALLBACK, isPrecise = false)
            return
        }
        val fix = runCatching { currentFix() }
            .onFailure { Log.w(TAG, "falling back to default city", it) }
            .getOrNull() ?: return
        val label = reverseGeocode(fix.first, fix.second)
        _location.value = UserLocation(PetLocation(fix.first, fix.second, label), isPrecise = true)
    }

    private fun hasPermission() = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ).any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    @SuppressLint("MissingPermission") // checked in hasPermission()
    private suspend fun currentFix(): Pair<Double, Double>? = withTimeoutOrNull(10_000) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val token = CancellationTokenSource()
        val loc = client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, token.token).await()
            ?: client.lastLocation.await()
        loc?.let { it.latitude to it.longitude }
    }

    /** Falls back to the coordinate pair rather than a meaningless "Unknown". */
    @Suppress("DEPRECATION") // the async API is 33+; this runs off the main thread
    private suspend fun reverseGeocode(lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        val coordinateLabel = String.format(Locale.US, "%.3f, %.3f", lat, lng)
        if (!Geocoder.isPresent()) return@withContext coordinateLabel
        runCatching {
            val place = Geocoder(context, Locale.getDefault()).getFromLocation(lat, lng, 1)?.firstOrNull()
                ?: return@runCatching coordinateLabel
            val area = place.subLocality ?: place.subAdminArea ?: place.featureName
            val city = place.locality ?: place.adminArea ?: place.countryName
            listOfNotNull(area, city).distinct().joinToString(", ").ifBlank { coordinateLabel }
        }.getOrDefault(coordinateLabel)
    }

    companion object {
        private const val TAG = "Location"
        val FALLBACK = PetLocation(28.6139, 77.209, "New Delhi")
    }
}
