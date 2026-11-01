package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.app_name
import nl.ovfietsbeschikbaarheid.resources.update_required_button
import nl.ovfietsbeschikbaarheid.resources.update_required_text
import nl.ovfietsbeschikbaarheid.resources.update_required_title
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateRequiredScreen() {
    val uriHandler = LocalUriHandler.current
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = Yellow50,
                    ),
                    title = {
                        Text(stringResource(Res.string.app_name))
                    },
                )
            },
        ) { innerPadding ->
            FullPageError(
                modifier = Modifier.padding(innerPadding),
                title = stringResource(Res.string.update_required_title),
                message = stringResource(Res.string.update_required_text),
                buttonText = stringResource(Res.string.update_required_button),
                onClick = { uriHandler.openUri(storePageUrl()) }
            )
        }
    }
}

expect fun storePageUrl(): String
