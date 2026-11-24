package com.petconnect.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.petconnect.domain.model.Pet
import com.petconnect.ui.components.Badge
import com.petconnect.ui.components.BadgeTone
import com.petconnect.ui.components.PetMetaRow
import com.petconnect.ui.components.primaryPhoto
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun MyPetCard(
    pet: Pet,
    isActive: Boolean,
    ageLabel: String,
    availabilityLabel: String,
    onOpen: () -> Unit,
    onToggleBreeding: (Boolean) -> Unit,
    onMatchAs: () -> Unit,
    onEditAvailability: () -> Unit,
    onRemove: () -> Unit,
) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = PcColors.Surface),
        border = if (isActive) BorderStroke(2.dp, PcColors.Primary) else null,
    ) {
        Row(
            Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = pet.photos.primaryPhoto(),
                contentDescription = pet.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(14.dp)),
            )
            Column(Modifier.weight(1f).padding(start = Spacing.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(pet.name, style = MaterialTheme.typography.titleMedium)
                    if (isActive) Badge("Matching as", BadgeTone.BRAND)
                }
                PetMetaRow(pet, ageLabel, showBadges = false)
            }
        }
        HorizontalDivider(color = PcColors.Border)
        Row(Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Available for breeding", style = MaterialTheme.typography.labelLarge)
                Text(availabilityLabel, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
            }
            Switch(checked = pet.availableForBreeding, onCheckedChange = onToggleBreeding)
        }
        Row(
            Modifier.padding(start = Spacing.md, end = Spacing.md, bottom = Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
        ) {
            if (!isActive) Action("Match as ${pet.name}", PcColors.Primary, onMatchAs)
            Action("Edit availability", PcColors.Primary, onEditAvailability)
            Action("Remove", PcColors.Danger, onRemove)
        }
    }
}

@Composable
private fun Action(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Text(
        label,
        color = color,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = Spacing.xs),
    )
}
