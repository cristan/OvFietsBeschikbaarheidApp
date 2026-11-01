package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import nl.ovfietsbeschikbaarheid.repository.AppCompatibilityRepository
import nl.ovfietsbeschikbaarheid.ui.screen.UpdateRequiredScreen
import org.koin.compose.koinInject

val LocalSpacing = staticCompositionLocalOf {
    16.dp
}

@Composable
fun MainView() {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass

    val isTabletSized =
        windowSizeClass.isAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND, WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val spacing = if (isTabletSized) {
        24.dp
    } else {
        16.dp
    }

    val isUpdateRequired by koinInject<AppCompatibilityRepository>().isUpdateRequired.collectAsState()

    CompositionLocalProvider(LocalSpacing provides spacing) {
        if (isUpdateRequired) {
            UpdateRequiredScreen()
        } else {
            Navigation()
        }
    }
}