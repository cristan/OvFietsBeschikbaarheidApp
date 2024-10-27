package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import nl.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import nl.ovfietsbeschikbaarheid.ui.screen.HomeScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onInfoClicked = { navController.navigate("info") },
                onLocationClick = { navController.navigate("detail/${it.locationCode}") }
            )
        }
        slideInOutComposable("info") {
            AboutScreen(onBackClicked = {
                navController.navigate("home") {
                    popUpTo(0)
                }
            })
        }
        slideInOutComposable("detail/{locationCode}") {
            NavigableDetailScreen(navController)
        }
        composable(
            route = "alternative/{locationCode}",
        ) {
            NavigableDetailScreen(navController)
        }
    }
}

@Composable
private fun NavigableDetailScreen(navController: NavHostController) {
    DetailScreen(
        onAlternativeClicked = { alternative ->
            navController.navigate("alternative/${alternative.locationCode}")
        },
        onBackClicked = {
            navController.navigate("home") {
                popUpTo(0)
            }
        }
    )
}

private fun NavGraphBuilder.slideInOutComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    deepLinks: List<NavDeepLink> = emptyList(),
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) =
    composable(
        route = route,
        arguments = arguments,
        deepLinks = deepLinks,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End) },
        content = content
    )