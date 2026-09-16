package nl.ovfietsbeschikbaarheid.ui

import androidx.compose.ui.window.ComposeUIViewController
import nl.ovfietsbeschikbaarheid.di.iosAppModule
import nl.ovfietsbeschikbaarheid.ui.navigation.MainView
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { MainView() }

fun doInitKoin() = startKoin {
    modules(
        iosAppModule()
    )
}
