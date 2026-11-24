package com.petconnect.ui.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.PetId
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PcChip
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun DiscoverScreen(
    contentPadding: PaddingValues,
    onOpenPet: (PetId) -> Unit,
    onAddPet: () -> Unit,
    onOpenChat: (String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .background(PcColors.SurfaceAlt)
            .statusBarsPadding()
            .padding(bottom = contentPadding.calculateBottomPadding()),
    ) {
        Column(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {
            Text("Match", style = MaterialTheme.typography.headlineSmall)
            Text("Swipe right if you're interested", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
        }

        when {
            state.loading -> LoadingBox()
            state.activePet == null -> EmptyState(
                icon = Icons.Outlined.Pets,
                title = "Add your pet first",
                message = "Matching works pet-to-pet. Create a profile to start swiping.",
                actionLabel = "Add a pet",
                onAction = onAddPet,
            )
            else -> {
                if (state.myPets.size > 1) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    ) {
                        items(state.myPets, key = { it.id.value }) { pet ->
                            PcChip("Matching as ${pet.name}", pet.id == state.activePet?.id) { viewModel.setActivePet(pet.id) }
                        }
                    }
                }
                Deck(state, viewModel, onOpenPet, Modifier.weight(1f))
            }
        }
    }

    celebration?.let { MatchCelebrationDialog(it, onOpenChat, viewModel::dismissCelebration) }
}

@Composable
private fun Deck(
    state: DiscoverUiState,
    viewModel: DiscoverViewModel,
    onOpenPet: (PetId) -> Unit,
    modifier: Modifier,
) {
    val top = state.deck.firstOrNull()
    if (top == null) {
        EmptyState(
            icon = Icons.Outlined.Refresh,
            title = "You've seen everyone nearby",
            message = "Widen your filters on the Nearby tab, or start over to review pets you skipped.",
            actionLabel = "Start over",
            onAction = viewModel::resetDeck,
            modifier = modifier,
        )
        return
    }

    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1f).fillMaxWidth().padding(Spacing.lg)) {
            // Peek of the next card for depth.
            state.deck.getOrNull(1)?.let { next ->
                key(next.pet.id.value) {
                    SwipeCard(
                        next, viewModel.dates.formatAge(next.pet.dateOfBirth),
                        onDecision = {}, onClick = {},
                        modifier = Modifier.fillMaxSize().scale(0.94f),
                    )
                }
            }
            key(top.pet.id.value) {
                SwipeCard(
                    top,
                    viewModel.dates.formatAge(top.pet.dateOfBirth),
                    onDecision = { viewModel.onDecision(top.pet, it) },
                    onClick = { onOpenPet(top.pet.id) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.xxl),
            modifier = Modifier.padding(bottom = Spacing.lg),
        ) {
            FilledIconButton(
                onClick = { viewModel.onDecision(top.pet, InterestDecision.SKIPPED) },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White, contentColor = PcColors.Danger),
                modifier = Modifier.size(64.dp),
            ) { Icon(Icons.Filled.Close, "Skip", Modifier.size(30.dp)) }
            FilledIconButton(
                onClick = { viewModel.onDecision(top.pet, InterestDecision.INTERESTED) },
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = PcColors.Primary, contentColor = Color.White),
                modifier = Modifier.size(64.dp),
            ) { Icon(Icons.Filled.Favorite, "Interested", Modifier.size(30.dp)) }
        }
    }
}
