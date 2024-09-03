package nl.ovfietsbeschikbaarheid.mapper

import android.content.Context
import nl.ovfietsbeschikbaarheid.di.appModule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @Test
    fun `check all modules`() {
        // Context is provided separately in MyApplication
        appModule().verify(extraTypes = listOf(Context::class))
    }
}