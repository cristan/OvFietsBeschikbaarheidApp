package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import nl.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import nl.ovfietsbeschikbaarheid.ui.screen.HomeScreen

@Serializable
object Home

@Serializable
data class Details(val title: String, val locationCode: String, val fetchTime: Long)

@Serializable
data class About(val pricePer24Hours: String?)

@Composable
fun Navigation() {
    val backStack = remember { mutableStateListOf<Any>(Home) }
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = {
            slideInHorizontally(initialOffsetX = { it }) togetherWith slideOutHorizontally(targetOffsetX = { -it })
        },
        popTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(initialOffsetX = { -it }) togetherWith slideOutHorizontally(targetOffsetX = { it })
        },
        entryProvider = { key ->
            when (key) {
                is Home -> NavEntry(key) {
                    HomeScreen(
                        onInfoClicked = { pricePer24Hours ->
                            backStack.add(About(pricePer24Hours))
                        },
                        onLocationClick = {
                            backStack.add(Details(it.title, it.locationCode, it.fetchTime))
                        }
                    )
                }

                is Details -> NavEntry(key) {
                    DetailScreen(
                        detailScreenData = key,
                        onAlternativeClicked = { alternative ->
                            backStack.add(Details(alternative.title, alternative.locationCode, alternative.fetchTime))
                        },
                        onBackClicked = {
                            backStack.removeRange(1, backStack.size)
                        }
                    )
                }

                is About -> NavEntry(key) {
                    AboutScreen(
                        pricePer24Hours = key.pricePer24Hours,
                        onBackClicked = {
                            backStack.removeRange(1, backStack.size)
                        }
                    )
                }

                else -> {
                    error("Unknown route: $key")
                }
            }
        }
    )
}