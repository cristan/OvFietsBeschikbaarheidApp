package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import nl.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import nl.ovfietsbeschikbaarheid.ui.screen.HomeScreen

@Serializable
object Home: NavKey

@Serializable
data class Details(val title: String, val locationCode: String, val fetchTime: Long): NavKey

@Serializable
data class About(val pricePer24Hours: String?): NavKey

private val config = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Home::class, Home.serializer())
            subclass(Details::class, Details.serializer())
            subclass(About::class, About.serializer())
        }
    }
}

@Composable
fun Navigation() {
    val backStack = rememberNavBackStack(config, Home)
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
                            backStack.removeAll { it != Home }
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

                else -> {
                    error("Unknown route: $key")
                }
            }
        }
    )
}