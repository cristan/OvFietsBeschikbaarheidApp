package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.update_required_button
import nl.ovfietsbeschikbaarheid.resources.update_required_text
import nl.ovfietsbeschikbaarheid.resources.update_required_title
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import org.jetbrains.compose.resources.stringResource

@Composable
fun UpdateRequiredScreen() {
    val uriHandler = LocalUriHandler.current
    OVFietsBeschikbaarheidTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            FullPageError(
                title = stringResource(Res.string.update_required_title),
                message = stringResource(Res.string.update_required_text),
                buttonText = stringResource(Res.string.update_required_button),
                onClick = { uriHandler.openUri(storePageUrl()) }
            )
        }
    }
}

expect fun storePageUrl(): String
