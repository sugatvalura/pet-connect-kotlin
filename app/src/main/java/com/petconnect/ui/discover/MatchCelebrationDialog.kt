package com.petconnect.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.primaryPhoto

/** Shown after a mutual match from either the swipe deck or a pet profile. */
@Composable
fun MatchCelebrationDialog(
    celebration: MatchCelebration,
    onMessage: (matchId: String) -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Keep swiping",
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                Avatar(celebration.myPet.photos.primaryPhoto(), celebration.myPet.name, size = 64.dp)
                Avatar(celebration.theirPet.photos.primaryPhoto(), celebration.theirPet.name, size = 64.dp)
            }
        },
        title = { Text("It's a match! 🎉", textAlign = TextAlign.Center) },
        text = {
            Text(
                "${celebration.myPet.name} and ${celebration.theirPet.name} are both interested. Say hello to the owner.",
                textAlign = TextAlign.Center,
            )
        },
        confirmButton = {
            TextButton(onClick = { onDismiss(); onMessage(celebration.matchId) }) { Text("Send a message") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismissLabel) } },
    )
}
