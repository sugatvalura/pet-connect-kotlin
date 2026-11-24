package com.petconnect.ui.navigation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.petconnect.ui.assistant.AssistantScreen
import com.petconnect.ui.auth.AuthScreen
import com.petconnect.ui.auth.SessionState
import com.petconnect.ui.auth.SessionViewModel
import com.petconnect.ui.chat.ChatScreen
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.listings.ListingDetailScreen
import com.petconnect.ui.listings.NewListingScreen
import com.petconnect.ui.main.MainScreen
import com.petconnect.ui.notifications.NotificationsScreen
import com.petconnect.ui.owner.OwnerProfileScreen
import com.petconnect.ui.pet.AvailabilityScreen
import com.petconnect.ui.pet.NewPetScreen
import com.petconnect.ui.pet.PetDetailScreen
import com.petconnect.ui.settings.SettingsScreen
import com.petconnect.ui.settings.SupportScreen

/**
 * Root navigation. The auth gate is driven by the session flow: signing in or
 * out swaps the whole graph, so no screen has to know about auth redirects.
 */
@Composable
fun PetConnectNavHost(session: SessionViewModel = hiltViewModel()) {
    val state by session.state.collectAsStateWithLifecycle()

    // Ask once; location falls back to a default city if denied.
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        session.refreshLocation()
    }
    LaunchedEffect(state) {
        if (state == SessionState.SignedIn) {
            permission.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    when (state) {
        SessionState.Loading -> LoadingBox()
        SessionState.SignedOut -> AuthGraph()
        SessionState.SignedIn -> AppGraph()
    }
}

@Composable
private fun AuthGraph() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = Route.Login) {
        composable<Route.Login> {
            AuthScreen(isSignup = false, onSwitchMode = { nav.navigate(Route.Signup) { launchSingleTop = true } })
        }
        composable<Route.Signup> {
            AuthScreen(isSignup = true, onSwitchMode = { nav.popBackStack() })
        }
    }
}

@Composable
private fun AppGraph() {
    val nav = rememberNavController()
    val back: () -> Unit = { nav.popBackStack() }
    val go: (Route) -> Unit = { nav.navigate(it) { launchSingleTop = true } }

    NavHost(nav, startDestination = Route.Main) {
        composable<Route.Main> { MainScreen(navigate = go) }

        composable<Route.PetDetail> {
            PetDetailScreen(
                onBack = back,
                onOpenChat = { go(Route.Chat(it)) },
                onOpenOwner = { go(Route.OwnerProfile(it.value)) },
                onEditAvailability = { go(Route.Availability(it.value)) },
            )
        }
        composable<Route.NewPet> { NewPetScreen(onDone = back) }
        composable<Route.Availability> { AvailabilityScreen(onDone = back) }
        composable<Route.Chat> { ChatScreen(onBack = back) }
        composable<Route.ListingDetail> {
            ListingDetailScreen(onBack = back, onOpenOwner = { go(Route.OwnerProfile(it.value)) })
        }
        composable<Route.NewListing> { NewListingScreen(onDone = back) }
        composable<Route.OwnerProfile> {
            OwnerProfileScreen(
                onBack = back,
                onOpenPet = { go(Route.PetDetail(it.value)) },
                onOpenListing = { go(Route.ListingDetail(it)) },
            )
        }
        composable<Route.Notifications> {
            NotificationsScreen(onBack = back, onOpenRoute = { raw -> parseDeepRoute(raw)?.let(go) })
        }
        composable<Route.Assistant> { AssistantScreen(onBack = back) }
        composable<Route.Settings> { SettingsScreen(onBack = back) }
        composable<Route.Support> { SupportScreen(onBack = back) }
    }
}
