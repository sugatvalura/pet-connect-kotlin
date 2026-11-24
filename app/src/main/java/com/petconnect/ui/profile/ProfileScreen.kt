package com.petconnect.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.Pet
import com.petconnect.domain.model.PetId
import com.petconnect.ui.components.Avatar
import com.petconnect.ui.components.Badge
import com.petconnect.ui.components.BadgeTone
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PrimaryButton
import com.petconnect.ui.components.SectionHeader
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    onOpenPet: (PetId) -> Unit,
    onAddPet: () -> Unit,
    onEditAvailability: (PetId) -> Unit,
    onNotifications: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<Pet?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }

    if (state.loading) {
        LoadingBox()
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(PcColors.SurfaceAlt)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg)
            .padding(bottom = contentPadding.calculateBottomPadding()),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(state.owner?.avatar, state.owner?.name ?: "You", size = 72.dp)
            Column(Modifier.padding(start = Spacing.lg), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    Text(state.owner?.name ?: "You", style = MaterialTheme.typography.titleLarge)
                    if (state.owner?.isVerified == true) Badge("⭐ Verified", BadgeTone.BRAND)
                }
                Text(state.owner?.city ?: state.locationLabel, color = PcColors.InkMuted)
                Text("🐾 ${state.myPets.size} ${if (state.myPets.size == 1) "pet" else "pets"}", color = PcColors.InkMuted)
            }
        }
        state.owner?.bio?.takeIf { it.isNotBlank() }?.let { Text(it, color = PcColors.InkMuted) }

        Card {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Your location", style = MaterialTheme.typography.labelMedium, color = PcColors.InkMuted)
                    Text(state.locationLabel, style = MaterialTheme.typography.titleSmall)
                    Text(
                        if (state.isPreciseLocation) "Used to calculate distances to nearby pets."
                        else "Using an approximate location. Enable permission for accurate distances.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PcColors.InkFaint,
                    )
                }
                IconButton(onClick = viewModel::refreshLocation) { Icon(Icons.Filled.Refresh, "Refresh location", tint = PcColors.Primary) }
            }
        }

        Column {
            SectionHeader("My pets", subtitle = "Pick which pet you're matching as", actionLabel = "Add pet", onAction = onAddPet)
            if (state.myPets.isEmpty()) {
                Card {
                    Text("You haven't added a pet yet. Add one to start matching.", color = PcColors.InkMuted)
                    PrimaryButton("Add your first pet", onAddPet, Modifier.padding(top = Spacing.md))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                state.myPets.forEach { pet ->
                    MyPetCard(
                        pet = pet,
                        isActive = pet.id == state.activePetId,
                        ageLabel = viewModel.dates.formatAge(pet.dateOfBirth),
                        availabilityLabel = if (pet.availability.enabled) {
                            viewModel.dates.formatDateRange(pet.availability.startDate, pet.availability.endDate)
                        } else "Off",
                        onOpen = { onOpenPet(pet.id) },
                        onToggleBreeding = { viewModel.setBreeding(pet.id, it) },
                        onMatchAs = { viewModel.setActivePet(pet.id) },
                        onEditAvailability = { onEditAvailability(pet.id) },
                        onRemove = { pendingDelete = pet },
                    )
                }
            }
        }

        Column {
            SectionHeader("Notifications")
            LinkRow(Icons.Outlined.Notifications, "View all notifications", onClick = onNotifications)
        }
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            SectionHeader("Account")
            state.email?.let { Text(it, color = PcColors.InkMuted, modifier = Modifier.padding(horizontal = Spacing.sm)) }
            LinkRow(Icons.AutoMirrored.Filled.Logout, "Log out", danger = true) { confirmLogout = true }
        }
    }

    pendingDelete?.let { pet ->
        ConfirmDialog(
            title = "Remove pet",
            message = "Remove ${pet.name}'s profile? This cannot be undone.",
            confirm = "Remove",
            onConfirm = { viewModel.removePet(pet.id); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
    if (confirmLogout) {
        ConfirmDialog(
            title = "Log out?",
            message = "You will need to sign back in to use Pet Connect.",
            confirm = "Log out",
            onConfirm = { confirmLogout = false; viewModel.signOut() },
            onDismiss = { confirmLogout = false },
        )
    }
}

@Composable
private fun Card(content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(PcColors.Surface, MaterialTheme.shapes.large).padding(Spacing.lg),
    ) { content() }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, danger: Boolean = false, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PcColors.Surface, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = if (danger) PcColors.Danger else PcColors.Ink
        Icon(icon, null, tint = tint)
        Text(label, color = tint, modifier = Modifier.padding(start = Spacing.md))
    }
}

@Composable
fun ConfirmDialog(title: String, message: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm, color = PcColors.Danger) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
