package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.window.core.layout.WindowSizeClass
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.favourites_title
import nl.ovfietsbeschikbaarheid.resources.home_tab_label
import nl.ovfietsbeschikbaarheid.resources.map_title
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

enum class NavigationTab(val label: StringResource, val icon: ImageVector, val selectedIcon: ImageVector) {
    Home(Res.string.home_tab_label, Icons.AutoMirrored.Outlined.List, Icons.AutoMirrored.Filled.List),
    LocationsMap(Res.string.map_title, Icons.Outlined.Map, Icons.Filled.Map),
    Favourites(Res.string.favourites_title, Icons.Outlined.FavoriteBorder, Icons.Filled.Favorite),
}

@Composable
fun NavigationTabsLayout(selectedTab: NavigationTab, onTabClick: (NavigationTab) -> Unit, content: @Composable () -> Unit) {
    val showsNavigationRail =
        currentWindowAdaptiveInfoV2().windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

    OVFietsBeschikbaarheidTheme {
        Row {
            if (showsNavigationRail) {
                NavigationRail(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    NavigationTab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = tab == selectedTab,
                            onClick = { onTabClick(tab) },
                            icon = { Icon(if (tab == selectedTab) tab.selectedIcon else tab.icon, contentDescription = null) },
                            label = { Text(stringResource(tab.label)) },
                            colors = NavigationRailItemDefaults.colors(selectedTextColor = MaterialTheme.colorScheme.onSurface),
                        )
                    }
                }
            }
            Scaffold(
                modifier = if (showsNavigationRail) {
                    Modifier.consumeWindowInsets(NavigationRailDefaults.windowInsets.only(WindowInsetsSides.Start))
                } else {
                    Modifier
                },
                bottomBar = {
                    if (!showsNavigationRail) {
                        NavigationBar {
                            NavigationTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = tab == selectedTab,
                                    onClick = { onTabClick(tab) },
                                    icon = { Icon(if (tab == selectedTab) tab.selectedIcon else tab.icon, contentDescription = null) },
                                    label = { Text(stringResource(tab.label)) },
                                    colors = NavigationBarItemDefaults.colors(selectedTextColor = MaterialTheme.colorScheme.onSurface),
                                )
                            }
                        }
                    }
                },
                contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    content()
                }
            }
        }
    }
}
