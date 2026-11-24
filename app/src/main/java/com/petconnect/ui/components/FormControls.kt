package com.petconnect.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

/** Single-select chip group bound to any option type. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ChoiceGroup(
    label: String,
    options: List<T>,
    selected: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = PcColors.InkMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            options.forEach { option -> PcChip(optionLabel(option), option == selected) { onSelect(option) } }
        }
    }
}

@Composable
fun SwitchRow(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, hint: String? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            hint?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted) }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
