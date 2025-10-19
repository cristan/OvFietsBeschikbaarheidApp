package nl.ovfietsbeschikbaarheid.ui

import androidx.compose.ui.window.ComposeUIViewController
import nl.ovfietsbeschikbaarheid.di.commonModule
import nl.ovfietsbeschikbaarheid.di.iosModule
import nl.ovfietsbeschikbaarheid.ui.navigation.Navigation
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { Navigation() }

fun doInitKoin() = startKoin {
    modules(
        commonModule(),
        iosModule()
    )
}
