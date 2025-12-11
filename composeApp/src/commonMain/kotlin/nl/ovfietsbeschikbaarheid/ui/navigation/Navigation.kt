package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import nl.ovfietsbeschikbaarheid.ui.screen.HomeScreen

@Serializable
object Home

@Serializable
data class AboutScreen(val pricePer24Hours: String?)

//@Serializable
//data class DetailScreenData(val title: String, val locationCode: String, val fetchTime: Long)

@Serializable
data class DetailScreenAlternative(val title: String, val locationCode: String, val fetchTime: Long)

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
            // Slide in from right when navigating forward
            slideInHorizontally(
                initialOffsetX = { it },
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
            )
        },
        popTransitionSpec = {
            // Slide in from left when navigating back
            slideInHorizontally(
                initialOffsetX = { -it },
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it }
            )
        },
        entryProvider = { key ->
            when (key) {
                is Home -> NavEntry(key) {
                    HomeScreen(
                        onInfoClicked = { pricePer24Hours -> Unit },
                        onLocationClick = {
                            backStack.add(DetailScreenData(it.title, it.locationCode, it.fetchTime))
                        }
                    )
                }

                is DetailScreenData -> NavEntry(key) {
                    DetailScreen(
                        detailScreenData = key,
                        onAlternativeClicked = { alternative ->
                            Unit
                        },
                        onBackClicked = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                else -> {
                    error("Unknown route: $key")
                }
            }
        }
    )

//    val navController = rememberNavController()
//    NavHost(navController = navController, startDestination = Home) {
//        composable<Home> {
//            HomeScreen(
//                onInfoClicked = { pricePer24Hours -> navController.navigate(AboutScreen(pricePer24Hours)) },
//                onLocationClick = {
//                    navController.navigate(DetailScreen(it.title, it.locationCode, it.fetchTime))
//                }
//            )
//        }
//        slideInOutComposable<AboutScreen> { backStackEntry ->
//            val aboutScreen: AboutScreen = backStackEntry.toRoute()
//            AboutScreen(
//                pricePer24Hours = aboutScreen.pricePer24Hours,
//                onBackClicked = {
//                navController.popBackStack<Home>(inclusive = false)
//            })
//        }
//        slideInOutComposable<DetailScreen> { backStackEntry ->
//            val detailScreen: DetailScreen = backStackEntry.toRoute()
//            val detailScreenData = DetailScreenData(detailScreen.title, detailScreen.locationCode, detailScreen.fetchTime)
//            NavigableDetailScreen(navController, detailScreenData)
//        }
//        composable<DetailScreenAlternative> { backStackEntry ->
//            val detailScreenAlternative: DetailScreenAlternative = backStackEntry.toRoute()
//            val detailScreenData = DetailScreenData(detailScreenAlternative.title, detailScreenAlternative.locationCode, detailScreenAlternative.fetchTime)
//            NavigableDetailScreen(navController, detailScreenData)
//        }
//    }
}

@Composable
private fun NavigableDetailScreen(navController: NavHostController, detailScreenData: DetailScreenData) {
    DetailScreen(
        detailScreenData = detailScreenData,
        onAlternativeClicked = { alternative ->
            navController.navigate(DetailScreenAlternative(alternative.title, alternative.locationCode, alternative.fetchTime))
        },
        onBackClicked = {
            navController.popBackStack<Home>(inclusive = false)
        }
    )
}

private inline fun <reified T : Any> NavGraphBuilder.slideInOutComposable(
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) =
    composable<T>(
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        content = content
    )