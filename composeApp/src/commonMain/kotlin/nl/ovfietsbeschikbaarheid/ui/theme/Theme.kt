package nl.ovfietsbeschikbaarheid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Blue70,
    secondary = BlueGrey80,
    tertiary = Pink80,
    primaryContainer = Blue90,
    secondaryContainer = secondaryContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = Blue90,
    secondary = BlueGrey40,
    tertiary = Pink40,
    primaryContainer = Blue90,
    background = Grey05,
    surface = Grey05,
    surfaceContainerHigh = Yellow05,
    secondaryContainer = secondaryContainerLight,

    /* Other default colors to override
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun OVFietsBeschikbaarheidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}