package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass

val LocalSpacing = staticCompositionLocalOf {
    16.dp
}

@Composable
fun MainView() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val isTabletSized =
        windowSizeClass.isAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND, WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val spacing = if (isTabletSized) {
        24.dp
    } else {
        16.dp
    }

    CompositionLocalProvider(LocalSpacing provides spacing) {
        Navigation()
    }
}