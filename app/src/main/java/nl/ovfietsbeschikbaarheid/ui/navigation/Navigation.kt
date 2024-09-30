package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.runtime.Composable
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
        composable("info") {
            AboutScreen(onBackClicked = {
                navController.navigate("home") {
                    popUpTo(0)
                }
            })
        }
        composable("detail/{locationCode}") { backStackEntry ->
            val locationCode = backStackEntry.arguments?.getString("locationCode")!!
            DetailScreen(
                locationCode = locationCode,
                onAlternativeClicked = { alternative ->
                    // Navigate with a fade effect when clicking on an alternative
                    navController.navigate("detail/${alternative.locationCode}")
                },
                onBackClicked = {
                    navController.navigate("home") {
                        popUpTo(0)
                    }
                }
            )
        }
    }
}