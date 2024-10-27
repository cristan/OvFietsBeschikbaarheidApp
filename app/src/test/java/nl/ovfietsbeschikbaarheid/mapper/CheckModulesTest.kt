package nl.ovfietsbeschikbaarheid.mapper

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import nl.ovfietsbeschikbaarheid.di.appModule
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class CheckModulesTest : KoinTest {

    @Test
    fun `check all modules`() {
        // Context is provided separately in MyApplication
        // SavedStateHandle is supported by Koin out of the box since version 3.3.0: https://insert-koin.io/docs/reference/koin-android/viewmodel/#savedstatehandle-injection-330
        // I don't know why the verify function doesn't know this.
        appModule().verify(extraTypes = listOf(Context::class, SavedStateHandle::class))
    }
}