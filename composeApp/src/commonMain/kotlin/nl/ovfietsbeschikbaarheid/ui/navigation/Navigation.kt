package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import nl.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import nl.ovfietsbeschikbaarheid.ui.screen.FavouritesScreen
import nl.ovfietsbeschikbaarheid.ui.screen.HomeScreen
import nl.ovfietsbeschikbaarheid.ui.screen.LocationsMapScreen

@Serializable
object Home: NavKey

@Serializable
data class Details(val title: String, val locationCode: String, val fetchTime: Long): NavKey

@Serializable
data class About(val pricePer24Hours: String?): NavKey

@Serializable
object LocationsMap: NavKey

@Serializable
object Favourites: NavKey

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Home::class, Home.serializer())
            subclass(Details::class, Details.serializer())
            subclass(About::class, About.serializer())
            subclass(LocationsMap::class, LocationsMap.serializer())
            subclass(Favourites::class, Favourites.serializer())
        }
    }
}

private val withoutTransitions =
    NavDisplay.transitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
        NavDisplay.popTransitionSpec { EnterTransition.None togetherWith ExitTransition.None } +
        NavDisplay.predictivePopTransitionSpec { EnterTransition.None togetherWith ExitTransition.None }

@Composable
fun Navigation() {
    var selectedTab by rememberSaveable { mutableStateOf(NavigationTab.Home) }
    val homeBackStack = rememberNavBackStack(config, Home)
    val locationsMapBackStack = rememberNavBackStack(config, LocationsMap)
    val favouritesBackStack = rememberNavBackStack(config, Favourites)
    val homeEntries = rememberTabEntries(homeBackStack, onTabClick = { selectedTab = it })
    val locationsMapEntries = rememberTabEntries(locationsMapBackStack, onTabClick = { selectedTab = it })
    val favouritesEntries = rememberTabEntries(favouritesBackStack, onTabClick = { selectedTab = it })
    val selectedBackStack = when (selectedTab) {
        NavigationTab.Home -> homeBackStack
        NavigationTab.LocationsMap -> locationsMapBackStack
        NavigationTab.Favourites -> favouritesBackStack
    }
    NavDisplay(
        entries = when (selectedTab) {
            NavigationTab.Home -> homeEntries
            NavigationTab.LocationsMap -> homeEntries + locationsMapEntries
            NavigationTab.Favourites -> homeEntries + favouritesEntries
        },
        onBack = {
            if (selectedBackStack.size > 1) {
                selectedBackStack.removeLastOrNull()
            } else {
                selectedTab = NavigationTab.Home
            }
        },
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
    )
}

@Composable
private fun rememberTabEntries(backStack: NavBackStack<NavKey>, onTabClick: (NavigationTab) -> Unit): List<NavEntry<NavKey>> =
    rememberDecoratedNavEntries(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = { key ->
            when (key) {
                is Home -> NavEntry(key) {
                    NavigationTabsLayout(selectedTab = NavigationTab.Home, onTabClick) {
                        HomeScreen(
                            onInfoClicked = { pricePer24Hours ->
                                backStack.add(About(pricePer24Hours))
                            },
                            onLocationClick = {
                                backStack.add(Details(it.title, it.locationCode, it.fetchTime))
                            }
                        )
                    }
                }

                is Details -> NavEntry(key) {
                    DetailScreen(
                        detailScreenData = key,
                        onAlternativeClicked = { alternative ->
                            backStack.add(Details(alternative.title, alternative.locationCode, alternative.fetchTime))
                        },
                        onBackClicked = {
                            backStack.removeAll { it is Details }
                        }
                    )
                }

                is About -> NavEntry(key) {
                    AboutScreen(
                        pricePer24Hours = key.pricePer24Hours,
                        onBackClicked = {
                            backStack.removeAll { it != Home }
                        }
                    )
                }

                is LocationsMap -> NavEntry(key, metadata = withoutTransitions) {
                    NavigationTabsLayout(selectedTab = NavigationTab.LocationsMap, onTabClick) {
                        LocationsMapScreen(
                            onLocationClick = {
                                backStack.add(Details(it.title, it.locationCode, it.fetchTime))
                            }
                        )
                    }
                }

                is Favourites -> NavEntry(key, metadata = withoutTransitions) {
                    NavigationTabsLayout(selectedTab = NavigationTab.Favourites, onTabClick) {
                        FavouritesScreen(
                            onLocationClick = {
                                backStack.add(Details(it.title, it.locationCode, it.fetchTime))
                            }
                        )
                    }
                }

                else -> {
                    error("Unknown route: $key")
                }
            }
        }
    )
