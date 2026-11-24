package com.petconnect.data.local

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Typed, reactive JSON persistence on top of DataStore — the Kotlin
 * counterpart of the RN `storage.ts` wrapper.
 *
 * Reads never throw: a corrupt or missing value falls back to the default,
 * so a bad write can never brick the app on launch. Writes are atomic
 * read-modify-write transactions (DataStore serialises `edit` calls), which
 * fixes the lost-update race the AsyncStorage version had.
 */
@Singleton
class JsonStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    fun <T> observe(key: String, serializer: KSerializer<T>, default: () -> T): Flow<T> =
        dataStore.data
            .catch { error ->
                Log.w(TAG, "read failed for $key", error)
                emit(androidx.datastore.preferences.core.emptyPreferences())
            }
            .map { prefs -> decode(prefs[stringPreferencesKey(key)], serializer, default, key) }
            .distinctUntilChanged()

    suspend fun <T> get(key: String, serializer: KSerializer<T>, default: () -> T): T =
        observe(key, serializer, default).first()

    /** Atomically transforms the stored value and returns the new one. */
    suspend fun <T> update(
        key: String,
        serializer: KSerializer<T>,
        default: () -> T,
        transform: (T) -> T,
    ): T {
        var result: T? = null
        dataStore.edit { prefs ->
            val prefKey = stringPreferencesKey(key)
            val next = transform(decode(prefs[prefKey], serializer, default, key))
            prefs[prefKey] = json.encodeToString(serializer, next)
            result = next
        }
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    suspend fun remove(key: String) {
        dataStore.edit { it.remove(stringPreferencesKey(key)) }
    }

    private fun <T> decode(raw: String?, serializer: KSerializer<T>, default: () -> T, key: String): T {
        if (raw == null) return default()
        return runCatching { json.decodeFromString(serializer, raw) }
            .onFailure { Log.w(TAG, "corrupt value for $key, using default", it) }
            .getOrElse { default() }
    }

    object Keys {
        const val PETS = "pets:v1"
        const val INTERESTS = "interests:v1"
        const val MATCHES = "matches:v1"
        const val MESSAGES = "messages:v1"
        const val LISTINGS = "listings:v1"
        const val NOTIFICATIONS = "notifications:v1"
        const val BLOCKED = "blocked:v1"
        const val SESSION = "session:v1"
        const val ACTIVE_PET = "active_pet:v1"
    }

    private companion object {
        const val TAG = "JsonStore"
    }
}
