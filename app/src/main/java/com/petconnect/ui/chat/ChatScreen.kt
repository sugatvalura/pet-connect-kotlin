package com.petconnect.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing
import kotlinx.coroutines.launch

private enum class ChatDialog { REPORT, LOCATION }

@Composable
fun ChatScreen(onBack: () -> Unit, viewModel: ChatViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft by viewModel.draft.collectAsStateWithLifecycle()
    val closed by viewModel.closed.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<ChatDialog?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.sendPhoto(it.toString()) }
    }

    LaunchedEffect(closed) { if (closed) onBack() }

    val ready = state as? ChatUiState.Ready
    DetailScaffold(
        title = ready?.let { "${it.theirPet.name}'s owner" } ?: "Chat",
        onBack = onBack,
        snackbar = snackbar,
        actions = {
            if (ready != null) IconButton(onClick = { dialog = ChatDialog.REPORT }) { Icon(Icons.Outlined.MoreHoriz, "Report or block") }
        },
        bottomBar = {
            if (ready != null) {
                Composer(
                    draft = draft,
                    onDraft = viewModel::onDraft,
                    onSend = viewModel::sendText,
                    onPhoto = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    onLocation = { dialog = ChatDialog.LOCATION },
                )
            }
        },
    ) { padding ->
        when (val s = state) {
            ChatUiState.Loading -> LoadingBox()
            ChatUiState.Unavailable -> Text(
                "This conversation is no longer available.",
                color = PcColors.InkMuted,
                modifier = Modifier.padding(padding).padding(Spacing.xl),
            )
            is ChatUiState.Ready -> {
                val listState = rememberLazyListState()
                // Keep the newest message in view.
                LaunchedEffect(s.messages.size) {
                    if (s.messages.isNotEmpty()) listState.animateScrollToItem(s.messages.lastIndex)
                }
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    if (s.messages.isEmpty()) {
                        item {
                            Text(
                                "You matched with ${s.theirPet.name}! Say hello to ${s.theirOwner.name} 👋",
                                color = PcColors.InkMuted,
                                modifier = Modifier.fillMaxWidth().padding(Spacing.xl),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            )
                        }
                    }
                    items(s.messages, key = { it.id }) { m ->
                        MessageBubble(m, isMine = m.senderId == s.me, time = viewModel.dates.formatClock(m.createdAt))
                    }
                }
            }
        }
    }

    when (dialog) {
        ChatDialog.REPORT -> ready?.let { r ->
            AlertDialog(
                onDismissRequest = { dialog = null },
                title = { Text("Report ${r.theirOwner.name}?") },
                text = { Text("Our team will review this conversation. You can also block this owner so they can no longer reach you.") },
                confirmButton = {
                    TextButton(onClick = { dialog = null; viewModel.reportAndBlock() }) { Text("Report & block", color = PcColors.Danger) }
                },
                dismissButton = {
                    TextButton(onClick = {
                        dialog = null
                        scope.launch { snackbar.showSnackbar("Report sent — thanks, our team will take a look.") }
                    }) { Text("Report only") }
                },
            )
        }
        ChatDialog.LOCATION -> AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text("Share approximate location?") },
            text = { Text("Your location will be blurred to about a 1 km radius so your exact address stays private.") },
            confirmButton = { TextButton(onClick = { dialog = null; viewModel.shareApproximateLocation() }) { Text("Share") } },
            dismissButton = { TextButton(onClick = { dialog = null }) { Text("Cancel") } },
        )
        null -> Unit
    }
}

@Composable
private fun Composer(
    draft: String,
    onDraft: (String) -> Unit,
    onSend: () -> Unit,
    onPhoto: () -> Unit,
    onLocation: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PcColors.Surface)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPhoto) { Icon(Icons.Outlined.Image, "Send a photo", tint = PcColors.Primary) }
        IconButton(onClick = onLocation) { Icon(Icons.Outlined.Place, "Share approximate location", tint = PcColors.Primary) }
        OutlinedTextField(
            value = draft,
            onValueChange = onDraft,
            placeholder = { Text("Message…") },
            shape = CircleShape,
            maxLines = 4,
            modifier = Modifier.weight(1f),
        )
        FilledIconButton(
            onClick = onSend,
            enabled = draft.isNotBlank(),
            modifier = Modifier.padding(start = Spacing.sm),
        ) { Icon(Icons.AutoMirrored.Filled.Send, "Send message") }
    }
}
