package nl.ovfietsbeschikbaarheid.ui

import androidx.compose.ui.window.ComposeUIViewController
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen

fun MainViewController() = ComposeUIViewController { AboutScreen(pricePer24Hours = null, onBackClicked = {}) }