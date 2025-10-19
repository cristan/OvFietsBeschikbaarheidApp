package nl.ovfietsbeschikbaarheid

import nl.ovfietsbeschikbaarheid.di.commonModule
import nl.ovfietsbeschikbaarheid.di.iosModule
import org.koin.core.context.startKoin

fun initKoin() = startKoin {
    modules(
        commonModule(),
        iosModule()
    )
}