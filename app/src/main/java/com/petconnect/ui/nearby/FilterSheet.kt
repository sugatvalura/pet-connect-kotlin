package com.petconnect.ui.nearby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.petconnect.domain.model.PetFilters
import com.petconnect.domain.model.PetGender
import com.petconnect.domain.model.PetSpecies
import com.petconnect.ui.components.PcChip
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

private data class AgeBand(val label: String, val min: Int, val max: Int)

private val AGE_BANDS = listOf(
    AgeBand("Any age", 0, 15),
    AgeBand("Under 1", 0, 1),
    AgeBand("1–3 yrs", 1, 3),
    AgeBand("3–7 yrs", 3, 7),
    AgeBand("7+ yrs", 7, 25),
)

/** Edits a local draft; nothing changes in the feed until Apply. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    initial: PetFilters,
    breeds: List<String>,
    onApply: (PetFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    var draft by remember(initial) { mutableStateOf(initial) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = PcColors.Surface,
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Filters", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { draft = PetFilters.DEFAULT }) { Text("Reset") }
            }

            Label("Distance")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PetFilters.DISTANCE_OPTIONS.forEach { km ->
                    PcChip("$km km", draft.maxDistanceKm == km) { draft = draft.copy(maxDistanceKm = km) }
                }
            }

            Label("Species")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PcChip("All", draft.species == null) { draft = draft.copy(species = null) }
                PetSpecies.entries.forEach { s ->
                    PcChip("${s.emoji} ${s.label}s", draft.species == s) { draft = draft.copy(species = s) }
                }
            }

            Label("Gender")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                PcChip("Any", draft.gender == null) { draft = draft.copy(gender = null) }
                PetGender.entries.forEach { g ->
                    PcChip(g.label, draft.gender == g) { draft = draft.copy(gender = g) }
                }
            }

            Label("Age")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                AGE_BANDS.forEach { band ->
                    val selected = draft.minAgeYears == band.min && draft.maxAgeYears == band.max
                    PcChip(band.label, selected) { draft = draft.copy(minAgeYears = band.min, maxAgeYears = band.max) }
                }
            }

            if (breeds.isNotEmpty()) {
                Label("Breed")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                ) {
                    PcChip("Any", draft.breed == null) { draft = draft.copy(breed = null) }
                    breeds.forEach { b ->
                        PcChip(b, draft.breed == b) { draft = draft.copy(breed = if (draft.breed == b) null else b) }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Available for breeding", style = MaterialTheme.typography.titleSmall)
                    Text("Only show pets open to matches", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
                }
                Switch(
                    checked = draft.availableForBreedingOnly,
                    onCheckedChange = { draft = draft.copy(availableForBreedingOnly = it) },
                )
            }

            PrimaryButton("Show results", onClick = { onApply(draft) }, modifier = Modifier.padding(vertical = Spacing.md))
        }
    }
}

@Composable
private fun Label(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = PcColors.InkMuted)
}
