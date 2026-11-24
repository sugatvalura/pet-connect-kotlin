package com.petconnect.ui.nearby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing

enum class MenuDestination(val label: String, val icon: ImageVector) {
    ASSISTANT("AI Assistant", Icons.Filled.AutoAwesome),
    SETTINGS("Settings", Icons.Outlined.Settings),
    SUPPORT("Support", Icons.AutoMirrored.Outlined.HelpOutline),
    PROFILE("Your profile", Icons.Outlined.Person),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications),
}

/** Drawer content; hosted by a ModalNavigationDrawer in NearbyScreen. */
@Composable
fun SideMenu(onSelect: (MenuDestination) -> Unit) {
    ModalDrawerSheet(drawerContainerColor = PcColors.Surface) {
        Row(Modifier.padding(Spacing.xl), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).clip(CircleShape).background(PcColors.Primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Pets, null, tint = Color.White)
            }
            Column(Modifier.padding(start = Spacing.md)) {
                Text("Pet Connect", style = MaterialTheme.typography.titleMedium)
                Text("Menu", style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted)
            }
        }
        MenuDestination.entries.forEach { d ->
            NavigationDrawerItem(
                label = { Text(d.label) },
                icon = { Icon(d.icon, null) },
                selected = false,
                onClick = { onSelect(d) },
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
        }
    }
}
