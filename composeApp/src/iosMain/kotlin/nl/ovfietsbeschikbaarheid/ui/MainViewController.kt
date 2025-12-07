package nl.ovfietsbeschikbaarheid.ui

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeUIViewController
import nl.ovfietsbeschikbaarheid.di.commonModule
import nl.ovfietsbeschikbaarheid.di.iosModule
import nl.ovfietsbeschikbaarheid.ui.navigation.Navigation
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun MainViewController() = ComposeUIViewController(configure = { parallelRendering = true }) { Navigation() }

fun doInitKoin() = startKoin {
    modules(
        commonModule(),
        iosModule()
    )
}
