package com.petconnect.ui.pet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.InterestDecision
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.PetId
import com.petconnect.ui.components.AvailabilityCard
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.Badge
import com.petconnect.ui.components.BadgeTone
import com.petconnect.ui.components.CompatibilityBreakdown
import com.petconnect.ui.components.DistanceRow
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PetMetaRow
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.components.ScorePill
import com.petconnect.ui.components.SecondaryButton
import com.petconnect.ui.components.SectionHeader
import com.petconnect.ui.components.StatTile
import com.petconnect.ui.discover.MatchCelebrationDialog
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun PetDetailScreen(
    onBack: () -> Unit,
    onOpenChat: (String) -> Unit,
    onOpenOwner: (OwnerId) -> Unit,
    onEditAvailability: (PetId) -> Unit,
    viewModel: PetDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val deciding by viewModel.deciding.collectAsStateWithLifecycle()
    val celebration by viewModel.celebration.collectAsStateWithLifecycle()
    val closed by viewModel.closed.collectAsStateWithLifecycle()

    LaunchedEffect(closed) { if (closed) onBack() }

    when (val s = state) {
        PetDetailUiState.Loading -> LoadingBox()
        PetDetailUiState.NotFound -> EmptyState(
            icon = Icons.Outlined.SearchOff,
            title = "Pet not found",
            message = "This profile may have been removed.",
            actionLabel = "Go back",
            onAction = onBack,
            modifier = Modifier.fillMaxSize(),
        )
        is PetDetailUiState.Ready -> Scaffold(
            containerColor = PcColors.SurfaceAlt,
            bottomBar = {
                ActionBar(
                    s = s,
                    deciding = deciding,
                    onOpenChat = onOpenChat,
                    onEditAvailability = { onEditAvailability(s.pet.id) },
                    onDecide = viewModel::decide,
                )
            },
        ) { padding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = padding.calculateBottomPadding())
                    .verticalScroll(rememberScrollState()),
            ) {
                PetGallery(s.pet.photos, s.pet.name, onBack)
                Column(
                    Modifier
                        .offset(y = (-20).dp)
                        .background(PcColors.SurfaceAlt, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                        .padding(Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    Body(s, viewModel, onOpenOwner)
                }
            }
        }
    }

    celebration?.let { MatchCelebrationDialog(it, onOpenChat, viewModel::dismissCelebration, dismissLabel = "Later") }
}

@Composable
private fun Body(s: PetDetailUiState.Ready, viewModel: PetDetailViewModel, onOpenOwner: (OwnerId) -> Unit) {
    val pet = s.pet
    val age = viewModel.dates.formatAge(pet.dateOfBirth)

    Row(verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text("${pet.species.emoji} ${pet.name}", style = MaterialTheme.typography.headlineSmall)
            PetMetaRow(pet, age)
            DistanceRow(s.distanceKm, pet.location.label)
        }
        if (!s.isOwnPet) ScorePill(s.compatibility.score, large = true)
    }

    if (pet.bio.isNotBlank()) Text(pet.bio, color = PcColors.InkMuted)

    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        StatTile("Age", age, Modifier.weight(1f))
        StatTile("Gender", pet.gender.label, Modifier.weight(1f))
        StatTile("Neutered", if (pet.isSpayedOrNeutered) "Yes" else "No", Modifier.weight(1f))
    }

    if (!s.isOwnPet) {
        Column(Modifier.background(PcColors.Surface, MaterialTheme.shapes.large).padding(Spacing.lg)) {
            SectionHeader(
                "Pet Connect Score",
                subtitle = s.activePet?.let { "How ${pet.name} fits with ${it.name}" }
                    ?: "Add your pet for a personalised score",
            )
            CompatibilityBreakdown(s.compatibility)
        }
    }

    Column {
        SectionHeader("Availability")
        AvailabilityCard(
            pet = pet,
            canView = s.canSeeAvailability,
            isOwner = s.isOwnPet,
            dateRange = viewModel.dates.formatDateRange(pet.availability.startDate, pet.availability.endDate),
        )
    }

    s.owner?.takeIf { !s.isOwnPet }?.let { owner ->
        Column {
            SectionHeader("Owner")
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(PcColors.Surface, MaterialTheme.shapes.large)
                    .clickable { onOpenOwner(owner.id) }
                    .padding(Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(owner.avatar, owner.name)
                Column(Modifier.weight(1f).padding(start = Spacing.md)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                        Text(owner.name, style = MaterialTheme.typography.titleSmall)
                        if (owner.isVerified) Badge("⭐ Verified", BadgeTone.BRAND)
                    }
                    Text(owner.city, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
                }
            }
        }
    }
}

@Composable
private fun ActionBar(
    s: PetDetailUiState.Ready,
    deciding: Boolean,
    onOpenChat: (String) -> Unit,
    onEditAvailability: () -> Unit,
    onDecide: (InterestDecision) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PcColors.Surface)
            .navigationBarsPadding()
            .padding(Spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            s.isOwnPet -> PrimaryButton("Edit availability", onEditAvailability)
            s.existingMatch != null -> PrimaryButton("Open chat", { onOpenChat(s.existingMatch.id) })
            s.activePet == null -> Text(
                "Add your own pet to show interest.",
                color = PcColors.InkMuted,
                modifier = Modifier.fillMaxWidth(),
            )
            else -> {
                SecondaryButton("Skip", { onDecide(InterestDecision.SKIPPED) }, Modifier.weight(1f))
                PrimaryButton(
                    "❤️ Interested",
                    { onDecide(InterestDecision.INTERESTED) },
                    Modifier.weight(2f),
                    loading = deciding,
                )
            }
        }
    }
}