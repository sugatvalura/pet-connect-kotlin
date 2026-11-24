package com.petconnect.ui.pet

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.AvailabilityVisibility
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.components.Field
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.components.ScrollBody
import com.petconnect.ui.components.SectionHeader
import com.petconnect.ui.components.SwitchRow
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun AvailabilityScreen(onDone: () -> Unit, viewModel: AvailabilityViewModel = hiltViewModel()) {
    val f by viewModel.form.collectAsStateWithLifecycle()
    LaunchedEffect(f.saved) { if (f.saved) onDone() }

    DetailScaffold(title = "Breeding availability", onBack = onDone) { padding ->
        if (!f.loaded) {
            LoadingBox()
            return@DetailScaffold
        }
        ScrollBody(padding) {
            SwitchRow(
                "${f.petName} is available", f.enabled, { v -> viewModel.edit { it.copy(enabled = v) } },
                hint = "Turn off any time to hide all dates.",
            )
            if (f.enabled) {
                SectionHeader("Availability period", subtitle = "When ${f.petName} is open to meeting a match")
                Field(
                    "Start date", f.startDate, { v -> viewModel.edit("startDate") { it.copy(startDate = v) } },
                    placeholder = "2026-09-10", error = f.errors["startDate"],
                )
                Field(
                    "End date", f.endDate, { v -> viewModel.edit("endDate") { it.copy(endDate = v) } },
                    placeholder = "2026-09-15", error = f.errors["endDate"],
                )

                SectionHeader("Who can see this?", subtitle = "You control exactly who sees these dates")
                AvailabilityVisibility.entries.forEach { option ->
                    VisibilityOption(option, selected = f.visibility == option) {
                        viewModel.edit { it.copy(visibility = option) }
                    }
                }
                Field(
                    "Notes (optional)", f.notes, { v -> viewModel.edit { it.copy(notes = v) } },
                    placeholder = "Prefer meeting at a park first…", singleLine = false, minLines = 2,
                )
            }
            PrimaryButton("Save", viewModel::save, loading = f.saving)
        }
    }
}

@Composable
private fun VisibilityOption(option: AvailabilityVisibility, selected: Boolean, onClick: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = if (selected) PcColors.PrimarySoft else PcColors.Surface),
        border = BorderStroke(1.dp, if (selected) PcColors.Primary else PcColors.Border),
        modifier = Modifier.fillMaxWidth().selectable(selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null)
            Column(Modifier.padding(start = Spacing.sm)) {
                Text(option.label, style = MaterialTheme.typography.titleSmall)
                Text(option.hint, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
            }
        }
    }
}
