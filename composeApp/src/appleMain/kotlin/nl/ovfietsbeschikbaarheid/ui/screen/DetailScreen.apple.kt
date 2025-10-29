package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.runtime.Composable
import io.ktor.http.URLBuilder
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun onLocationClicked(): (String) -> Unit = { address ->
    val url = URLBuilder("http://maps.apple.com/").apply {
        parameters.append("q", address)
    }.build()
    val nsUrl = NSURL.URLWithString(url.toString())!!
    UIApplication.sharedApplication.openURL(nsUrl)
}