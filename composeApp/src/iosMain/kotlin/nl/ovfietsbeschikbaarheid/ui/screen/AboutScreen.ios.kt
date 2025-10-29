package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_text_5_ios
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ReviewCallToAction()  {
    Text(
        text = "\n",
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = stringResource(Res.string.about_app_text_5_ios),
        style = MaterialTheme.typography.bodyLarge
    )
}