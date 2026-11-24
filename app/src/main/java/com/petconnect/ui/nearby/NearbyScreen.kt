package com.petconnect.ui.nearby

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.petconnect.domain.model.PetId
import com.petconnect.ui.components.EmptyState
import com.petconnect.ui.components.LoadingBox
import com.petconnect.ui.components.PetCard
import com.petconnect.ui.components.SecondaryButton
import com.petconnect.ui.theme.PcColors
import com.petconnect.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyScreen(
    contentPadding: PaddingValues,
    onOpenPet: (PetId) -> Unit,
    onAddPet: () -> Unit,
    onOpenMatches: () -> Unit,
    onMenu: (MenuDestination) -> Unit,
    viewModel: NearbyViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
    var filtersOpen by remember { mutableStateOf(false) }
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawer,
        drawerContent = {
            SideMenu { dest ->
                scope.launch { drawer.close() }
                onMenu(dest)
            }
        },
    ) {
        Column(Modifier.fillMaxSize().background(PcColors.SurfaceAlt).statusBarsPadding()) {
            NearbyHeader(
                locationLabel = state.locationLabel,
                precise = state.isPreciseLocation,
                unread = state.unreadNotifications,
                onMenu = { scope.launch { drawer.open() } },
                onNotifications = { onMenu(MenuDestination.NOTIFICATIONS) },
            )
            if (state.loading) {
                LoadingBox()
                return@Column
            }
            PullToRefreshBox(isRefreshing = refreshing, onRefresh = viewModel::refresh) {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = Spacing.lg, end = Spacing.lg,
                        bottom = contentPadding.calculateBottomPadding() + Spacing.lg,
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    if (!state.hasOwnPets) {
                        item { AddPetBanner(onAddPet) }
                    }
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryRail(state.filters, viewModel::setFilters, Modifier.weight(1f))
                            BadgedBox(
                                badge = {
                                    if (state.filters.activeCount > 0) {
                                        androidx.compose.material3.Badge { Text("${state.filters.activeCount}") }
                                    }
                                },
                                modifier = Modifier.padding(start = Spacing.sm),
                            ) {
                                FilledTonalIconButton(onClick = { filtersOpen = true }) {
                                    Icon(Icons.Outlined.Tune, "Filters")
                                }
                            }
                        }
                    }
                    state.topMatch?.let { top ->
                        item { TopMatchCard(top, viewModel.dates.formatAge(top.pet.dateOfBirth)) { onOpenPet(top.pet.id) } }
                    }
                    item { AssistantPromo { onMenu(MenuDestination.ASSISTANT) } }
                    item { AvailableNowRail(state.availableNow) { onOpenPet(it.pet.id) } }
                    item {
                        StatsStrip(
                            listOf(
                                Stat("Nearby", "${state.feed?.totalNearby ?: 0}"),
                                Stat("Matches", "${state.matchCount}", onOpenMatches),
                                Stat("Available", "${state.availableNow.size}"),
                                Stat("Best", "${state.topMatch?.compatibility?.score ?: 0}%"),
                            ),
                        )
                    }

                    if (state.all.isEmpty()) {
                        item {
                            EmptyState(
                                icon = Icons.Outlined.Pets,
                                title = "No pets match your filters",
                                message = "Try widening the distance or clearing a filter to see more pets around you.",
                                actionLabel = "Reset filters",
                                onAction = viewModel::resetFilters,
                            )
                        }
                    } else {
                        item {
                            Row(Modifier.fillMaxWidth()) {
                                Text("All pets nearby", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text("${state.visible.size} of ${state.all.size}", style = MaterialTheme.typography.labelSmall, color = PcColors.InkFaint)
                            }
                        }
                        items(state.visible, key = { it.pet.id.value }) { item ->
                            PetCard(item, viewModel.dates.formatAge(item.pet.dateOfBirth), onClick = { onOpenPet(item.pet.id) })
                        }
                        item {
                            if (state.hasMore) {
                                SecondaryButton(
                                    "Load more · ${state.visible.size} of ${state.all.size}",
                                    viewModel::loadMore,
                                    Modifier.fillMaxWidth(),
                                )
                            } else {
                                Text(
                                    "Showing all ${state.all.size} pets",
                                    color = PcColors.InkFaint,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.fillMaxWidth().padding(Spacing.md),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (filtersOpen) {
        FilterSheet(
            initial = state.filters,
            breeds = state.feed?.availableBreeds.orEmpty(),
            onApply = { viewModel.setFilters(it); filtersOpen = false },
            onDismiss = { filtersOpen = false },
        )
    }
}

@Composable
private fun NearbyHeader(
    locationLabel: String,
    precise: Boolean,
    unread: Int,
    onMenu: () -> Unit,
    onNotifications: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = Spacing.lg, vertical = Spacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FilledTonalIconButton(onClick = onMenu) { Icon(Icons.Filled.Menu, "Open menu") }
        Column(Modifier.weight(1f).padding(horizontal = Spacing.md)) {
            Text("Nearby pets", style = MaterialTheme.typography.headlineSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, null, tint = PcColors.Primary, modifier = Modifier.padding(end = 2.dp))
                Text(locationLabel, style = MaterialTheme.typography.bodySmall, color = PcColors.InkMuted, maxLines = 1)
                if (!precise) Text(" · approximate", style = MaterialTheme.typography.labelSmall, color = PcColors.InkFaint)
            }
        }
        BadgedBox(badge = { if (unread > 0) androidx.compose.material3.Badge { Text("$unread") } }) {
            FilledTonalIconButton(onClick = onNotifications) { Icon(Icons.Outlined.Notifications, "Notifications") }
        }
    }
}

@Composable
private fun AddPetBanner(onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(PcColors.PrimarySoft, MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .padding(Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("🐾", style = MaterialTheme.typography.headlineSmall)
        Column(Modifier.weight(1f).padding(start = Spacing.md)) {
            Text("Add your pet", style = MaterialTheme.typography.titleSmall)
            Text(
                "Create a profile to start matching and get accurate scores.",
                style = MaterialTheme.typography.bodySmall,
                color = PcColors.InkMuted,
            )
        }
    }
}
