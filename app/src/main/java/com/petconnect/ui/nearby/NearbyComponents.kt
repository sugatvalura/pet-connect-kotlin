package com.petconnect.ui.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetSpecies
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.util.Geo
import com.petconnect.ui.components.PcChip
import com.petconnect.ui.components.ScorePill
import com.petconnect.ui.components.SectionHeader
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

private data class Category(val label: String, val isSelected: (PetFilters) -> Boolean, val apply: (PetFilters) -> PetFilters)

private val CATEGORIES = listOf(
    Category("All", { it.species == null && !it.availableForBreedingOnly }) {
        it.copy(species = null, availableForBreedingOnly = false)
    },
    Category("🐕 Dogs", { it.species == PetSpecies.DOG }) { it.copy(species = PetSpecies.DOG) },
    Category("🐈 Cats", { it.species == PetSpecies.CAT }) { it.copy(species = PetSpecies.CAT) },
    Category("✨ Available", { it.availableForBreedingOnly }) { it.copy(availableForBreedingOnly = true) },
    Category("Under 5 km", { it.maxDistanceKm == 5 }) { it.copy(maxDistanceKm = 5) },
)

@Composable
fun CategoryRail(filters: PetFilters, onChange: (PetFilters) -> Unit, modifier: Modifier = Modifier) {
    LazyRow(modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        items(CATEGORIES, key = { it.label }) { c ->
            PcChip(c.label, c.isSelected(filters)) { onChange(c.apply(filters)) }
        }
    }
}

@Composable
fun TopMatchCard(item: PetWithContext, ageLabel: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth().height(220.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = item.pet.photos.primaryPhoto(),
                contentDescription = item.pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(0.4f to Color.Transparent, 1f to Color(0xCC000000)),
                ),
            )
            Text(
                "★ Top match",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier
                    .padding(Spacing.md)
                    .clip(CircleShape)
                    .background(PcColors.Accent)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            )
            ScorePill(item.compatibility.score, Modifier.align(Alignment.TopEnd).padding(Spacing.md), large = true)
            Column(Modifier.align(Alignment.BottomStart).padding(Spacing.lg)) {
                Text(item.pet.name, color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${item.pet.breed} · $ageLabel · ${Geo.formatDistance(item.distanceKm)}",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun AssistantPromo(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(PcColors.PrimarySoft)
            .clickable(onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(40.dp).clip(CircleShape).background(PcColors.Primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.AutoAwesome, null, tint = Color.White)
        }
        Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
            Text("Ask the Pet Assistant", style = MaterialTheme.typography.titleSmall)
            Text("Breeds, care, and how matching works.", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
        }
        Icon(Icons.Filled.ChevronRight, null, tint = PcColors.Primary)
    }
}

@Composable
fun AvailableNowRail(items: List<PetWithContext>, onSelect: (PetWithContext) -> Unit) {
    if (items.isEmpty()) return
    Column {
        SectionHeader("Available now", subtitle = "Open to breeding near you")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            items(items, key = { it.pet.id.value }) { item ->
                Column(Modifier.width(110.dp).clickable { onSelect(item) }) {
                    AsyncImage(
                        model = item.pet.photos.primaryPhoto(),
                        contentDescription = item.pet.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(110.dp).clip(RoundedCornerShape(18.dp)),
                    )
                    Text(item.pet.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, modifier = Modifier.padding(top = 6.dp))
                    Text(Geo.formatDistance(item.distanceKm), style = MaterialTheme.typography.labelSmall, color = PcColors.InkMuted)
                }
            }
        }
    }
}

data class Stat(val label: String, val value: String, val onClick: (() -> Unit)? = null)

@Composable
fun StatsStrip(stats: List<Stat>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        stats.forEach { s ->
            Column(
                Modifier
                    .weight(1f)
                    .clip(MaterialTheme.shapes.medium)
                    .background(PcColors.Surface)
                    .then(if (s.onClick != null) Modifier.clickable(onClick = s.onClick) else Modifier)
                    .padding(vertical = Spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(s.value, style = MaterialTheme.typography.titleMedium, color = PcColors.Primary)
                Text(s.label, style = MaterialTheme.typography.labelSmall, color = PcColors.InkMuted)
            }
        }
    }
}
