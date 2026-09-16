package nl.ovfietsbeschikbaarheid

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests whether the app even starts.
 *
 * Was introduced because a change resulted in a crash on startup because of missing Android resources (fixed in 6321904).
 * This will find problems like that.
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainActivityComposesSuccessfully() {
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }
}
