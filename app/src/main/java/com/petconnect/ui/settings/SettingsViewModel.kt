package com.petconnect.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.BuildConfig
import com.petconnect.domain.repository.AlertSetting
import com.petconnect.domain.repository.SettingsRepository
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val alerts: Map<AlertSetting, Boolean> = AlertSetting.entries.associateWith { true },
    val version: String = BuildConfig.VERSION_NAME,
)

/** Unlike the RN app (screen-local state), toggles persist across launches. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {
    val state: StateFlow<SettingsUiState> = settings.alerts
        .map { SettingsUiState(alerts = it) }
        .stateInVm(this, SettingsUiState())

    fun toggle(setting: AlertSetting, enabled: Boolean) {
        viewModelScope.launch { settings.setAlert(setting, enabled) }
    }
}
