package com.petconnect.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.petconnect.domain.repository.AlertSetting
import com.petconnect.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private fun AlertSetting.key() = booleanPreferencesKey("alerts_${name.lowercase()}")

    override val alerts: Flow<Map<AlertSetting, Boolean>> = dataStore.data.map { prefs ->
        AlertSetting.entries.associateWith { prefs[it.key()] ?: true }
    }

    override suspend fun setAlert(setting: AlertSetting, enabled: Boolean) {
        dataStore.edit { it[setting.key()] = enabled }
    }
}
