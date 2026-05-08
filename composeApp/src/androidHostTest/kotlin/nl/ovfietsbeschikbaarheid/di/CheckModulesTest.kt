package nl.ovfietsbeschikbaarheid.di

import android.content.Context
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `check androidAppModule`() {
        androidAppModule().verify(
            extraTypes =
                listOf(
                    // Added in MyApplication.setupKoin()
                    Context::class,
                )
        )
    }
}