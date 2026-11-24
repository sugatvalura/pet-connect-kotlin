package com.petconnect.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petconnect.domain.model.AppNotification
import com.petconnect.domain.repository.NotificationRepository
import com.petconnect.domain.util.Dates
import com.petconnect.ui.common.stateInVm
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(val loading: Boolean = true, val items: List<AppNotification> = emptyList()) {
    val unread: Int get() = items.count { !it.read }
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val notifications: NotificationRepository,
    val dates: Dates,
) : ViewModel() {
    val state: StateFlow<NotificationsUiState> = notifications.notifications
        .map { list -> NotificationsUiState(false, list.sortedByDescending { it.createdAt }) }
        .stateInVm(this, NotificationsUiState())

    fun open(n: AppNotification) {
        viewModelScope.launch { notifications.markRead(n.id) }
    }

    fun markAllRead() {
        viewModelScope.launch { notifications.markAllRead() }
    }

    fun clear() {
        viewModelScope.launch { notifications.clear() }
    }
}
