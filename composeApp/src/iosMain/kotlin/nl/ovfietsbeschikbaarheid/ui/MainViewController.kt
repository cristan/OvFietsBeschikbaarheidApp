package nl.ovfietsbeschikbaarheid.ui

import androidx.compose.ui.window.ComposeUIViewController
import nl.ovfietsbeschikbaarheid.di.iosAppModule
import nl.ovfietsbeschikbaarheid.ui.navigation.MainView
import nl.ovfietsbeschikbaarheid.util.UsageStatisticsSender
import org.koin.core.context.startKoin
import org.koin.mp.KoinPlatform

fun MainViewController() = ComposeUIViewController { MainView() }

fun doInitKoin() = startKoin {
    modules(
        iosAppModule()
    )
}

fun onAppStarted() {
    KoinPlatform.getKoin().get<UsageStatisticsSender>().onAppStarted()
}
