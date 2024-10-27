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
object Info

@Serializable
data class DetailScreen(val title: String, val uri: String, val fetchTime: Long)

@Serializable
data class DetailScreenAlternative(val title: String, val uri: String, val fetchTime: Long)

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            HomeScreen(
                onInfoClicked = { navController.navigate(Info) },
                onLocationClick = {
                    navController.navigate(DetailScreen(it.title, it.uri, it.fetchTime))
                }
            )
        }
        slideInOutComposable<Info> {
            AboutScreen(onBackClicked = {
                navController.navigate(Home) {
                    popUpTo(0)
                }
            })
        }
        slideInOutComposable<DetailScreen> { backStackEntry ->
            val detailScreen: DetailScreen = backStackEntry.toRoute()
            val detailScreenData = DetailScreenData(detailScreen.title, detailScreen.uri, detailScreen.fetchTime)
            NavigableDetailScreen(navController, detailScreenData)
        }
        composable<DetailScreenAlternative> { backStackEntry ->
            val detailScreenAlternative: DetailScreenAlternative = backStackEntry.toRoute()
            val detailScreenData = DetailScreenData(detailScreenAlternative.title, detailScreenAlternative.uri, detailScreenAlternative.fetchTime)
            NavigableDetailScreen(navController, detailScreenData)
        }
    }
}

@Composable
private fun NavigableDetailScreen(navController: NavHostController, detailScreenData: DetailScreenData) {
    DetailScreen(
        detailScreenData = detailScreenData,
        onAlternativeClicked = { alternative ->
            navController.navigate(DetailScreenAlternative(alternative.title, alternative.uri, alternative.fetchTime))
        },
        onBackClicked = {
            navController.navigate(Home) {
                popUpTo(0)
            }
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