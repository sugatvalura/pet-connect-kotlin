package com.petconnect.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.AppNotification
import com.petconnect.domain.model.NotificationKind
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

private fun NotificationKind.emoji() = when (this) {
    NotificationKind.MATCH -> "💞"
    NotificationKind.MESSAGE -> "💬"
    NotificationKind.NEARBY_AVAILABLE -> "📍"
    NotificationKind.AVAILABILITY_REMINDER -> "📅"
    NotificationKind.LISTING_RESPONSE -> "🏠"
}

/** [onOpenRoute] receives the portable string route stored on the notification. */
@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpenRoute: (String) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScaffold(
        title = "Notifications",
        onBack = onBack,
        actions = {
            if (state.unread > 0) TextButton(onClick = viewModel::markAllRead) { Text("Mark all read") }
            else if (state.items.isNotEmpty()) TextButton(onClick = viewModel::clear) { Text("Clear") }
        },
    ) { padding ->
        when {
            state.loading -> LoadingBox()
            state.items.isEmpty() -> EmptyState(
                Icons.Outlined.NotificationsNone,
                "You're all caught up",
                "Matches, messages, and nearby availability will show up here.",
                Modifier.fillMaxSize().padding(padding),
            )
            else -> LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(bottom = Spacing.lg)) {
                items(state.items, key = { it.id }) { n ->
                    NotificationRow(n, viewModel.dates.formatRelative(n.createdAt)) {
                        viewModel.open(n)
                        n.route?.let(onOpenRoute)
                    }
                    HorizontalDivider(color = PcColors.Border)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(n: AppNotification, time: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (n.read) PcColors.Surface else PcColors.PrimarySoft)
            .clickable(onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(PcColors.Surface), contentAlignment = Alignment.Center) {
            Text(n.kind.emoji())
        }
        Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
            Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = if (n.read) FontWeight.Medium else FontWeight.Bold)
            Text(n.body, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(time, style = MaterialTheme.typography.labelSmall, color = PcColors.InkFaint)
            if (!n.read) Box(Modifier.padding(top = 6.dp).size(8.dp).clip(CircleShape).background(PcColors.Primary))
        }
    }
}
