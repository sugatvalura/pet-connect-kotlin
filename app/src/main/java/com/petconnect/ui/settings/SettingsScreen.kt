package com.petconnect.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.repository.AlertSetting
import com.petconnect.ui.components.DetailScaffold
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.components.ScrollBody
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

private fun AlertSetting.icon(): ImageVector = when (this) {
    AlertSetting.MATCHES -> Icons.Outlined.FavoriteBorder
    AlertSetting.NEARBY -> Icons.Outlined.LocationOn
    AlertSetting.MESSAGES -> Icons.AutoMirrored.Outlined.Chat
}

@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DetailScaffold(title = "Settings", onBack = onBack) { padding ->
        ScrollBody(padding) {
            SectionLabel("Notifications")
            Column(Modifier.background(PcColors.Surface, MaterialTheme.shapes.large)) {
                AlertSetting.entries.forEachIndexed { i, setting ->
                    Row(Modifier.fillMaxWidth().padding(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                        Icon(setting.icon(), null, tint = PcColors.Ink)
                        Text(setting.label, modifier = Modifier.weight(1f).padding(start = Spacing.md))
                        Switch(checked = state.alerts[setting] ?: true, onCheckedChange = { viewModel.toggle(setting, it) })
                    }
                    if (i < AlertSetting.entries.lastIndex) HorizontalDivider(color = PcColors.Border)
                }
            }

            SectionLabel("Assistant")
            InfoCard(
                Icons.Outlined.AutoAwesome,
                null,
                "The AI Assistant answers Pet Connect and breed questions only. Guardrails block off-topic or misleading requests.",
            )

            SectionLabel("About")
            Row(Modifier.fillMaxWidth().background(PcColors.Surface, MaterialTheme.shapes.large).padding(Spacing.lg)) {
                Text("Version", modifier = Modifier.weight(1f))
                Text(state.version, color = PcColors.InkMuted)
            }
        }
    }
}

private data class SupportTopic(val icon: ImageVector, val title: String, val body: String)

private val TOPICS = listOf(
    SupportTopic(Icons.Outlined.Shield, "Stay safe when meeting",
        "Meet in public places, bring a friend, and never share your exact home address in chat."),
    SupportTopic(Icons.Outlined.Flag, "Report & block",
        "Open any match chat → menu → Report. You can also block an owner so they cannot message you."),
    SupportTopic(Icons.Outlined.Pets, "Breeding & health",
        "Pet Connect connects owners — it does not replace a veterinarian. Ask your vet about vaccines and breeding readiness."),
    SupportTopic(Icons.Outlined.AutoAwesome, "AI Assistant",
        "Use the menu → AI Assistant for breed FAQs and app how-tos. Out-of-scope questions are blocked on purpose."),
)

@Composable
fun SupportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    DetailScaffold(title = "Support", onBack = onBack) { padding ->
        ScrollBody(padding) {
            Text(
                "Need help with Pet Connect? Start here — or ask the AI Assistant for breed and app FAQs.",
                color = PcColors.InkMuted,
            )
            TOPICS.forEach { InfoCard(it.icon, it.title, it.body) }
            PrimaryButton("Email support", onClick = {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@petconnect.app?subject=Pet%20Connect%20Help"))
                runCatching { context.startActivity(intent) }
            })
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelMedium, color = PcColors.InkMuted, modifier = Modifier.padding(top = Spacing.sm))
}

@Composable
private fun InfoCard(icon: ImageVector, title: String?, body: String) {
    Row(Modifier.fillMaxWidth().background(PcColors.Surface, MaterialTheme.shapes.large).padding(Spacing.lg)) {
        Icon(
            icon, null, tint = PcColors.Primary,
            modifier = Modifier.background(PcColors.PrimarySoft, CircleShape).padding(8.dp).size(20.dp),
        )
        Column(Modifier.padding(start = Spacing.md)) {
            title?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
            Text(body, style = MaterialTheme.typography.bodyMedium, color = PcColors.InkMuted)
        }
    }
}
