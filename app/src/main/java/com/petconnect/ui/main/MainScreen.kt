package com.petconnect.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.petconnect.domain.model.PetId
import com.petconnect.ui.chats.ChatsScreen
import com.petconnect.ui.discover.DiscoverScreen
import com.petconnect.ui.listings.ListingsScreen
import com.petconnect.ui.navigation.Route
import com.petconnect.ui.navigation.Tab
import com.petconnect.ui.nearby.MenuDestination
import com.petconnect.ui.nearby.NearbyScreen
import com.petconnect.ui.profile.ProfileScreen
import com.petconnect.ui.theme.PcColors

private fun Tab.icon(selected: Boolean): ImageVector = when (this) {
    Tab.NEARBY -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
    Tab.MATCH -> if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
    Tab.ADOPT -> if (selected) Icons.Filled.Storefront else Icons.Outlined.Storefront
    Tab.CHATS -> if (selected) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline
    Tab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
}

/**
 * The tab shell. Tabs switch in place (no back-stack entry per tab), and every
 * push goes through [navigate] so screens never touch the NavController.
 */
@Composable
fun MainScreen(navigate: (Route) -> Unit) {
    var tab by rememberSaveable { mutableStateOf(Tab.NEARBY) }

    Scaffold(
        containerColor = PcColors.SurfaceAlt,
        bottomBar = {
            NavigationBar(containerColor = PcColors.Surface) {
                Tab.entries.forEach { t ->
                    NavigationBarItem(
                        selected = t == tab,
                        onClick = { tab = t },
                        icon = { Icon(t.icon(t == tab), contentDescription = t.label) },
                        label = { Text(t.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PcColors.Primary,
                            selectedTextColor = PcColors.Primary,
                            indicatorColor = PcColors.PrimarySoft,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        val openPet: (PetId) -> Unit = { navigate(Route.PetDetail(it.value)) }
        val openChat: (String) -> Unit = { navigate(Route.Chat(it)) }
        val addPet = { navigate(Route.NewPet) }

        when (tab) {
            Tab.NEARBY -> NearbyScreen(
                contentPadding = padding,
                onOpenPet = openPet,
                onAddPet = addPet,
                onOpenMatches = { tab = Tab.CHATS },
                onMenu = { dest ->
                    when (dest) {
                        MenuDestination.ASSISTANT -> navigate(Route.Assistant)
                        MenuDestination.SETTINGS -> navigate(Route.Settings)
                        MenuDestination.SUPPORT -> navigate(Route.Support)
                        MenuDestination.PROFILE -> tab = Tab.PROFILE
                        MenuDestination.NOTIFICATIONS -> navigate(Route.Notifications)
                    }
                },
            )
            Tab.MATCH -> DiscoverScreen(padding, openPet, addPet, openChat)
            Tab.ADOPT -> ListingsScreen(padding, { navigate(Route.ListingDetail(it)) }, { navigate(Route.NewListing) })
            Tab.CHATS -> ChatsScreen(padding, openChat, onDiscover = { tab = Tab.MATCH })
            Tab.PROFILE -> ProfileScreen(
                contentPadding = padding,
                onOpenPet = openPet,
                onAddPet = addPet,
                onEditAvailability = { navigate(Route.Availability(it.value)) },
                onNotifications = { navigate(Route.Notifications) },
            )
        }
    }
}