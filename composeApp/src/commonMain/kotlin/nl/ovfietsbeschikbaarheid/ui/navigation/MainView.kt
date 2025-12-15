package nl.ovfietsbeschikbaarheid.ui.navigation

import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

val LocalSpacing = staticCompositionLocalOf {
    16.dp
}

@Composable
fun MainView() {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val spacing = if (windowSizeClass.isAtLeastBreakpoint(600, 600)) {
        24.dp
    } else {
        16.dp
    }

    CompositionLocalProvider(LocalSpacing provides spacing) {
        Navigation()
    }
}