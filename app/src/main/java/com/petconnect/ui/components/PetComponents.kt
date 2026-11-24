package com.petconnect.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.domain.model.CompatibilityResult
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetWithContext
import com.petconnect.domain.model.VaccinationStatus
import com.petconnect.domain.util.Geo
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

const val PLACEHOLDER_PHOTO =
    "https://images.unsplash.com/photo-1425082661705-1834bfd09dca?auto=format&fit=crop&w=600&q=70"

fun List<String>.primaryPhoto(): String = firstOrNull() ?: PLACEHOLDER_PHOTO

fun PetGender.symbol() = if (this == PetGender.MALE) "♂ Male" else "♀ Female"

/** Age · gender · breed, plus health badges. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetMetaRow(pet: Pet, ageLabel: String, showBadges: Boolean = true) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            "${pet.breed} · $ageLabel · ${pet.gender.symbol()}",
            style = MaterialTheme.typography.bodyMedium,
            color = PcColors.InkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showBadges) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                when (pet.vaccination) {
                    VaccinationStatus.VACCINATED -> Badge("✅ Vaccinated", BadgeTone.SUCCESS)
                    VaccinationStatus.PARTIAL -> Badge("Partly vaccinated", BadgeTone.WARNING)
                    VaccinationStatus.NOT_VACCINATED -> Badge("Not vaccinated", BadgeTone.DANGER)
                }
                if (pet.availableForBreeding) Badge("Open to breeding", BadgeTone.BRAND)
                if (pet.isSpayedOrNeutered) Badge("Neutered", BadgeTone.NEUTRAL)
            }
        }
    }
}

@Composable
fun PetCard(item: PetWithContext, ageLabel: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PcColors.Surface),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = item.pet.photos.primaryPhoto(),
                contentDescription = item.pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(84.dp).clip(RoundedCornerShape(14.dp)),
            )
            Column(Modifier.weight(1f).padding(horizontal = Spacing.md), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${item.pet.species.emoji} ${item.pet.name}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                )
                PetMetaRow(item.pet, ageLabel, showBadges = false)
                DistanceRow(item.distanceKm, item.pet.location.label)
            }
            ScorePill(item.compatibility.score)
        }
    }
}

@Composable
fun DistanceRow(distanceKm: Double, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.LocationOn, null, tint = PcColors.Primary, modifier = Modifier.size(14.dp))
        Text(
            "${Geo.formatDistance(distanceKm)} · $label",
            style = MaterialTheme.typography.labelSmall,
            color = PcColors.InkMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Shows each factor's bar and reason, so the score is never a black box. */
@Composable
fun CompatibilityBreakdown(result: CompatibilityResult) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        result.factors.forEach { factor ->
            Column {
                Row(Modifier.fillMaxWidth()) {
                    Text(factor.label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                    Text(factor.detail, style = MaterialTheme.typography.labelSmall, color = PcColors.InkMuted)
                }
                LinearProgressIndicator(
                    progress = { factor.score.toFloat() },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = PcColors.Primary,
                    trackColor = PcColors.PrimarySoft,
                )
            }
        }
    }
}

/**
 * Renders a breeding window only if the privacy policy allowed it
 * ([canView] is computed by AvailabilityPrivacyPolicy upstream).
 */
@Composable
fun AvailabilityCard(pet: Pet, canView: Boolean, isOwner: Boolean, dateRange: String) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PcColors.Surface),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            when {
                !pet.availability.enabled && !isOwner ->
                    Text("${pet.name} isn't open to breeding right now.", color = PcColors.InkMuted)
                !pet.availability.enabled ->
                    Text("Breeding availability is off.", color = PcColors.InkMuted)
                !canView -> Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Lock, null, tint = PcColors.InkFaint, modifier = Modifier.size(18.dp))
                    Text(
                        "  Dates are private. ${if (pet.availability.visibility.name == "MATCHES_ONLY") "Match to see them." else ""}",
                        color = PcColors.InkMuted,
                    )
                }
                else -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(PcColors.Accent))
                        Text("  Available $dateRange", fontWeight = FontWeight.SemiBold)
                    }
                    if (isOwner) {
                        Text("Visible to: ${pet.availability.visibility.label}", style = MaterialTheme.typography.labelSmall, color = PcColors.InkMuted)
                    }
                    pet.availability.notes?.let { Text("“$it”", color = PcColors.InkMuted, style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}

/** Small labelled stat used on detail screens. */
@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier.clip(MaterialTheme.shapes.medium).background(PcColors.Surface).padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, style = MaterialTheme.typography.titleSmall)
        Text(label, style = MaterialTheme.typography.labelSmall, color = PcColors.InkMuted)
    }
}

@Composable
fun Gap(width: Int = 0, height: Int = 0) {
    androidx.compose.foundation.layout.Spacer(Modifier.width(width.dp).height(height.dp))
}
