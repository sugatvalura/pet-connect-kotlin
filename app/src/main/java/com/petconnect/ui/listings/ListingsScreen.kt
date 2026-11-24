package com.petconnect.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.petconnect.domain.model.ListingKind
import com.petconnect.domain.util.Geo
import com.petconnect.ui.components.Badge
import com.petconnect.ui.components.BadgeTone
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PcChip
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun ListingsScreen(
    contentPadding: PaddingValues,
    onOpenListing: (String) -> Unit,
    onNewListing: () -> Unit,
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().background(PcColors.SurfaceAlt).statusBarsPadding()) {
        Row(Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Adopt & sell", style = MaterialTheme.typography.headlineSmall)
                Text("Find a pet a home, or a home a pet", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
            }
            FilledIconButton(onClick = onNewListing) { Icon(Icons.Filled.Add, "New listing") }
        }
        Row(Modifier.padding(horizontal = Spacing.lg), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            PcChip("All", state.kind == null) { viewModel.selectKind(null) }
            ListingKind.entries.forEach { k -> PcChip(k.label, state.kind == k) { viewModel.selectKind(k) } }
        }

        when {
            state.loading -> LoadingBox()
            state.items.isEmpty() -> EmptyState(
                icon = Icons.Outlined.Storefront,
                title = "No listings yet",
                message = "Be the first to post a pet for adoption or sale in your area.",
                actionLabel = "Create listing",
                onAction = onNewListing,
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = Spacing.lg, end = Spacing.lg, top = Spacing.md,
                    bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
                ),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text("${state.items.size} listings near you", style = MaterialTheme.typography.labelMedium, color = PcColors.InkFaint)
                }
                items(state.items, key = { it.listing.id }) { item ->
                    ListingCard(item, viewModel.dates.formatAge(item.listing.dateOfBirth)) { onOpenListing(item.listing.id) }
                }
            }
        }
    }
}

@Composable
private fun ListingCard(item: ListingItem, ageLabel: String, onClick: () -> Unit) {
    val l = item.listing
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PcColors.Surface),
    ) {
        Box {
            AsyncImage(
                model = l.photos.primaryPhoto(),
                contentDescription = l.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
            Box(Modifier.padding(Spacing.sm)) {
                Badge(l.kind.label, if (l.kind == ListingKind.ADOPT) BadgeTone.SUCCESS else BadgeTone.ACCENT)
            }
        }
        Column(Modifier.padding(Spacing.md)) {
            Text(l.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${l.breed} · $ageLabel", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted, maxLines = 1)
            Text(
                l.priceLabel(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = PcColors.Primary,
                maxLines = 1,
            )
            Text(Geo.formatDistance(item.distanceKm), style = MaterialTheme.typography.labelSmall, color = PcColors.InkFaint)
        }
    }
}
