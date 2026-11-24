package com.petconnect.ui.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.model.PetId
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.Badge
import com.petconnect.ui.components.BadgeTone
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PetCard
import com.petconnect.ui.components.ScrollBody
import com.petconnect.ui.components.SectionHeader
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.listings.priceLabel
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun OwnerProfileScreen(
    onBack: () -> Unit,
    onOpenPet: (PetId) -> Unit,
    onOpenListing: (String) -> Unit,
    viewModel: OwnerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScaffold(title = (state as? OwnerUiState.Ready)?.owner?.name ?: "Owner", onBack = onBack) { padding ->
        when (val s = state) {
            OwnerUiState.Loading -> LoadingBox()
            OwnerUiState.NotFound -> EmptyState(
                Icons.Outlined.SearchOff, "Owner not found", "This profile is no longer available.",
                Modifier.fillMaxSize().padding(padding),
            )
            is OwnerUiState.Ready -> ScrollBody(padding) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(s.owner.avatar, s.owner.name, size = 72.dp)
                    Column(Modifier.padding(start = Spacing.lg)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
                            Text(s.owner.name, style = MaterialTheme.typography.titleLarge)
                            if (s.owner.isVerified) Badge("⭐ Verified", BadgeTone.BRAND)
                        }
                        Text(s.owner.city, color = PcColors.InkMuted)
                        Text("Member since ${s.owner.joinedAt.take(4)}", style = MaterialTheme.typography.bodySmall, color = PcColors.InkFaint)
                    }
                }
                if (s.owner.bio.isNotBlank()) Text(s.owner.bio, color = PcColors.InkMuted)

                SectionHeader("${s.owner.name}'s pets")
                if (s.pets.isEmpty()) Text("No pets listed yet.", color = PcColors.InkMuted)
                s.pets.forEach { item ->
                    PetCard(item, viewModel.dates.formatAge(item.pet.dateOfBirth), onClick = { onOpenPet(item.pet.id) })
                }

                if (s.listings.isNotEmpty()) {
                    SectionHeader("Listings")
                    s.listings.forEach { l ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(PcColors.Surface, MaterialTheme.shapes.large)
                                .clickable { onOpenListing(l.id) }
                                .padding(Spacing.md),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AsyncImage(
                                model = l.photos.primaryPhoto(),
                                contentDescription = l.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)),
                            )
                            Column(Modifier.weight(1f).padding(start = Spacing.md)) {
                                Text(l.title, style = MaterialTheme.typography.titleSmall, maxLines = 1)
                                Text(l.priceLabel(), style = MaterialTheme.typography.bodySmall, color = PcColors.Primary)
                            }
                            Badge(l.kind.label, if (l.kind == ListingKind.ADOPT) BadgeTone.SUCCESS else BadgeTone.ACCENT)
                        }
                    }
                }
            }
        }
    }
}
