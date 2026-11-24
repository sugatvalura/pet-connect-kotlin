package com.petconnect.ui.assistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.repository.AssistantReply
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssistantScreen(onBack: () -> Unit, viewModel: AssistantViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    LaunchedEffect(state.turns.size, state.thinking) {
        val count = state.turns.size + if (state.thinking) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    DetailScaffold(
        title = "Paw · AI Assistant",
        onBack = onBack,
        actions = {
            if (state.turns.isNotEmpty()) IconButton(onClick = viewModel::reset) { Icon(Icons.Outlined.Refresh, "New conversation") }
        },
        bottomBar = {
            Column(Modifier.background(PcColors.Surface).navigationBarsPadding().imePadding().padding(Spacing.sm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = state.draft,
                        onValueChange = viewModel::onDraft,
                        placeholder = { Text("Ask about breeds, matching, privacy…") },
                        shape = CircleShape,
                        maxLines = 4,
                        modifier = Modifier.weight(1f),
                    )
                    FilledIconButton(
                        onClick = { viewModel.send() },
                        enabled = state.draft.isNotBlank() && !state.thinking,
                        modifier = Modifier.padding(start = Spacing.sm),
                    ) { Icon(Icons.AutoMirrored.Filled.Send, "Send") }
                }
                Text(
                    "Scoped to Pet Connect · guardrails on",
                    style = MaterialTheme.typography.labelSmall,
                    color = PcColors.InkFaint,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            if (state.turns.isEmpty()) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xl)) {
                        Text("🐾", style = MaterialTheme.typography.displaySmall)
                        Text("Ask me anything pet", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Breed facts, how matching works, and app how-tos.",
                            color = PcColors.InkMuted,
                            textAlign = TextAlign.Center,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterHorizontally),
                            modifier = Modifier.padding(top = Spacing.lg),
                        ) {
                            AssistantUiState.SUGGESTIONS.forEach { s ->
                                SuggestionChip(onClick = { viewModel.send(s.prompt) }, label = { Text(s.label) })
                            }
                        }
                    }
                }
            }
            items(state.turns, key = { it.id }) { TurnBubble(it) }
            if (state.thinking) item { TurnBubble(AssistantTurn(-1, false, "Thinking…")) }
        }
    }
}

@Composable
private fun TurnBubble(turn: AssistantTurn) {
    val bg = when {
        turn.fromUser -> PcColors.Primary
        turn.blocked -> PcColors.WarningSoft
        else -> PcColors.Surface
    }
    val fg = if (turn.fromUser) Color.White else PcColors.Ink
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (turn.fromUser) Arrangement.End else Arrangement.Start) {
        Column(Modifier.widthIn(max = 300.dp).clip(RoundedCornerShape(18.dp)).background(bg).padding(Spacing.md)) {
            if (turn.blocked) {
                Text("Guardrail · out of scope", style = MaterialTheme.typography.labelSmall, color = PcColors.Warning)
            }
            Text(turn.text, color = fg)
            if (!turn.fromUser && turn.source == AssistantReply.Source.LOCAL) {
                Text("Offline answer", style = MaterialTheme.typography.labelSmall, color = PcColors.InkFaint, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
