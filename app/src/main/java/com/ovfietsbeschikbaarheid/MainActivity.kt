package com.ovfietsbeschikbaarheid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel
import com.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import com.ovfietsbeschikbaarheid.ui.screen.DetailScreen
import com.ovfietsbeschikbaarheid.ui.screen.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavController()
        }
    }
}

@Composable
private fun NavController() {
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
            DetailScreen(locationCode, { alternative ->
                run {
                    navController.navigate("detail/${alternative.locationCode}")
                }
            }) {
                navController.navigate("home") {
                    popUpTo(0)
                }
            }
        }
    }
}