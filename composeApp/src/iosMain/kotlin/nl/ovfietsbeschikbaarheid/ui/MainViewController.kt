package nl.ovfietsbeschikbaarheid.ui

import androidx.compose.ui.window.ComposeUIViewController
import nl.ovfietsbeschikbaarheid.di.commonModule
import nl.ovfietsbeschikbaarheid.di.iosModule
import nl.ovfietsbeschikbaarheid.ui.screen.AboutScreen
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { AboutScreen(pricePer24Hours = null, onBackClicked = {}) }

fun doInitKoin() = startKoin {
    modules(
        commonModule(),
        iosModule()
    )
}
