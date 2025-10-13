package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import nl.ovfietsbeschikbaarheid.model.DetailScreenData
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import nl.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import nl.ovfietsbeschikbaarheid.ui.screen.HomeScreen

@Serializable
object Home

@Serializable
data class AboutScreen(val pricePer24Hours: String?)

@Serializable
data class DetailScreen(val title: String, val locationCode: String, val fetchTime: Long)

@Serializable
data class DetailScreenAlternative(val title: String, val locationCode: String, val fetchTime: Long)

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onInfoClicked = { pricePer24Hours -> navController.navigate(AboutScreen(pricePer24Hours)) },
                onLocationClick = {
                    navController.navigate(DetailScreen(it.title, it.locationCode, it.fetchTime))
                }
            )
        }
        slideInOutComposable<AboutScreen> { backStackEntry ->
            val aboutScreen: AboutScreen = backStackEntry.toRoute()
            AboutScreen(
                pricePer24Hours = aboutScreen.pricePer24Hours,
                onBackClicked = {
                navController.popBackStack<Home>(inclusive = false)
            })
        }
        slideInOutComposable<DetailScreen> { backStackEntry ->
            val detailScreen: DetailScreen = backStackEntry.toRoute()
            val detailScreenData = DetailScreenData(detailScreen.title, detailScreen.locationCode, detailScreen.fetchTime)
            NavigableDetailScreen(navController, detailScreenData)
        }
        composable<DetailScreenAlternative> { backStackEntry ->
            val detailScreenAlternative: DetailScreenAlternative = backStackEntry.toRoute()
            val detailScreenData = DetailScreenData(detailScreenAlternative.title, detailScreenAlternative.locationCode, detailScreenAlternative.fetchTime)
            NavigableDetailScreen(navController, detailScreenData)
        }
    }
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