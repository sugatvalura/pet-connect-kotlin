package com.petconnect.ui.chats

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun ChatsScreen(
    contentPadding: PaddingValues,
    onOpenChat: (String) -> Unit,
    onDiscover: () -> Unit,
    viewModel: ChatsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(PcColors.SurfaceAlt).statusBarsPadding()) {
        Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
            Text("Chats", style = MaterialTheme.typography.headlineSmall)
            Text("Talk to owners you matched with", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
        }
        when {
            state.loading -> LoadingBox()
            state.conversations.isEmpty() -> EmptyState(
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "No matches yet",
                message = "When you and another owner both show interest, your chat appears here.",
                actionLabel = "Start matching",
                onAction = onDiscover,
            )
            else -> LazyColumn(contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding())) {
                items(state.conversations, key = { it.match.id }) { c ->
                    ConversationRow(c, viewModel.dates.formatRelative(c.match.lastMessageAt ?: c.match.createdAt)) {
                        onOpenChat(c.match.id)
                    }
                    HorizontalDivider(color = PcColors.Border, modifier = Modifier.padding(start = 76.dp))
                }
            }
        }
    }
}
@Composable
private fun ConversationRow(c: Conversation, time: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(PcColors.Surface)
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(c.theirPet.photos.primaryPhoto(), c.theirPet.name, size = 48.dp)
        Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
            Text(
                "${c.theirPet.name} · ${c.theirOwner?.name ?: "Owner"}",
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                c.match.lastMessagePreview ?: "Matched with ${c.myPet.name} — say hello 👋",
                style = MaterialTheme.typography.bodySmall,
                color = if (c.match.lastMessagePreview == null) PcColors.Primary else PcColors.InkMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(time, style = MaterialTheme.typography.labelSmall, color = PcColors.InkFaint)
    }
}
