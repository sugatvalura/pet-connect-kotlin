package com.petconnect.data.repository

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.petconnect.R
import com.petconnect.data.local.IdGenerator
import com.petconnect.data.local.JsonStore
import com.petconnect.domain.model.AppNotification
import com.petconnect.domain.model.NotificationKind
import com.petconnect.domain.repository.NotificationRepository
import com.petconnect.domain.util.Dates
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-app feed (source of truth) plus a best-effort system notification.
 * A denied permission must never break the in-app feed.
 */
@Singleton
class LocalNotificationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val store: JsonStore,
    private val ids: IdGenerator,
    private val dates: Dates,
) : NotificationRepository {
    private val serializer = ListSerializer(AppNotification.serializer())
    private val none = { emptyList<AppNotification>() }

    override val notifications: Flow<List<AppNotification>> =
        store.observe(JsonStore.Keys.NOTIFICATIONS, serializer, none)
            .map { list -> list.sortedByDescending { it.createdAt } }

    override suspend fun push(kind: NotificationKind, title: String, body: String, route: String?) {
        val item = AppNotification(ids.next("notif"), kind, title, body, dates.nowIso(), read = false, route = route)
        store.update(JsonStore.Keys.NOTIFICATIONS, serializer, none) { listOf(item) + it }
        showSystemNotification(item)
    }

    override suspend fun markRead(id: String) {
        store.update(JsonStore.Keys.NOTIFICATIONS, serializer, none) { list ->
            list.map { if (it.id == id) it.copy(read = true) else it }
        }
    }

    override suspend fun markAllRead() {
        store.update(JsonStore.Keys.NOTIFICATIONS, serializer, none) { list -> list.map { it.copy(read = true) } }
    }

    override suspend fun clear() {
        store.update(JsonStore.Keys.NOTIFICATIONS, serializer, none) { emptyList() }
    }

    private fun showSystemNotification(item: AppNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(item.title)
            .setContentText(item.body)
            .setAutoCancel(true)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(item.id.hashCode(), notification) }
    }

    private fun ensureChannel() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Pet Connect", NotificationManager.IMPORTANCE_DEFAULT),
        )
    }

    private companion object {
        const val CHANNEL_ID = "default"
    }
}
