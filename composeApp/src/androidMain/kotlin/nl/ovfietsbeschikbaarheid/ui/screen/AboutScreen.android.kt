package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_text_5
import nl.ovfietsbeschikbaarheid.resources.about_app_text_6
import nl.ovfietsbeschikbaarheid.resources.about_app_text_7
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ReviewCallToAction()  {
    Text(
        text = buildAnnotatedString {
            append("\n")
            append(stringResource(Res.string.about_app_text_5))
            withStyledLink(
                url = "https://play.google.com/store/apps/details?id=nl.ovfietsbeschikbaarheid",
                text = stringResource(Res.string.about_app_text_6)
            )
            append(stringResource(Res.string.about_app_text_7))
        },
        style = MaterialTheme.typography.bodyLarge
    )
}