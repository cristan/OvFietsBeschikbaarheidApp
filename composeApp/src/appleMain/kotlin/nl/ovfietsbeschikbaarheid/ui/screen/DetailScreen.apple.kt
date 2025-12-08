package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import io.ktor.http.URLBuilder

@Composable
actual fun onLocationClicked(): (String) -> Unit {
    val uriHandler = LocalUriHandler.current
    val onLocationClicked: (String) -> Unit = { address ->
        val url = URLBuilder("http://maps.apple.com/").apply {
            parameters.append("q", address)
        }.build()
        uriHandler.openUri(url.toString())
    }
    return onLocationClicked
}