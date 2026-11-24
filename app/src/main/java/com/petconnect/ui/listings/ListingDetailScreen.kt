package com.petconnect.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.model.OwnerId
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.Badge
import com.petconnect.ui.components.BadgeTone
import com.petconnect.ui.components.DistanceRow
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.components.SecondaryButton
import com.petconnect.ui.components.StatTile
import com.petconnect.ui.pet.PetGallery
import com.petconnect.ui.profile.ConfirmDialog
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    onOpenOwner: (OwnerId) -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val closed by viewModel.closed.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(closed) { if (closed) onBack() }
    LaunchedEffect(message) { message?.let { snackbar.showSnackbar(it); viewModel.messageShown() } }

    when (val s = state) {
        ListingDetailUiState.Loading -> LoadingBox()
        ListingDetailUiState.NotFound -> EmptyState(
            Icons.Outlined.SearchOff, "Listing not found", "It may have been removed.",
            Modifier.fillMaxSize(), actionLabel = "Go back", onAction = onBack,
        )
        is ListingDetailUiState.Ready -> Scaffold(
            containerColor = PcColors.SurfaceAlt,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                Row(Modifier.fillMaxWidth().background(PcColors.Surface).navigationBarsPadding().padding(Spacing.lg)) {
                    if (s.isMine) {
                        SecondaryButton("Delete listing", { confirmDelete = true }, Modifier.fillMaxWidth())
                    } else {
                        PrimaryButton(
                            if (s.listing.kind == ListingKind.ADOPT) "Enquire about adoption" else "Contact seller",
                            viewModel::respond,
                        )
                    }
                }
            },
        ) { padding ->
            val l = s.listing
            Column(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding()).verticalScroll(rememberScrollState())) {
                PetGallery(l.photos, l.title, onBack)
                Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    Badge(l.kind.label, if (l.kind == ListingKind.ADOPT) BadgeTone.SUCCESS else BadgeTone.ACCENT)
                    Text(l.title, style = MaterialTheme.typography.headlineSmall)
                    Text(l.priceLabel(), style = MaterialTheme.typography.titleLarge, color = PcColors.Primary, fontWeight = FontWeight.ExtraBold)
                    DistanceRow(s.distanceKm, l.location.label)
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        StatTile("Breed", l.breed, Modifier.weight(1f))
                        StatTile("Age", viewModel.dates.formatAge(l.dateOfBirth), Modifier.weight(1f))
                        StatTile("Gender", l.gender.label, Modifier.weight(1f))
                    }
                    when (l.vaccination) {
                        VaccinationStatus.VACCINATED -> Badge("✅ Vaccinated", BadgeTone.SUCCESS)
                        VaccinationStatus.PARTIAL -> Badge("Partly vaccinated", BadgeTone.WARNING)
                        VaccinationStatus.NOT_VACCINATED -> Badge("Not vaccinated", BadgeTone.DANGER)
                    }
                    Text("Description", style = MaterialTheme.typography.titleSmall)
                    Text(l.description, color = PcColors.InkMuted)
                    s.owner?.let { owner ->
                        Text("Posted by", style = MaterialTheme.typography.titleSmall)
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(PcColors.Surface, MaterialTheme.shapes.large)
                                .clickable { onOpenOwner(owner.id) }
                                .padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(owner.avatar, owner.name)
                            Column(Modifier.padding(start = Spacing.md)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                    Text(owner.name, style = MaterialTheme.typography.titleSmall)
                                    if (owner.isVerified) Badge("⭐ Verified", BadgeTone.BRAND)
                                }
                                Text(owner.city, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            "Delete listing", "Remove this listing permanently?", "Delete",
            onConfirm = { confirmDelete = false; viewModel.delete() },
            onDismiss = { confirmDelete = false },
        )
    }
}
