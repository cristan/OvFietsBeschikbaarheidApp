package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.buildAnnotatedString
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_text_5
import nl.ovfietsbeschikbaarheid.resources.about_app_text_6_ios
import nl.ovfietsbeschikbaarheid.resources.about_app_text_7
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ReviewCallToAction()  {
    Text(
        text = buildAnnotatedString {
            append("\n")
            append(stringResource(Res.string.about_app_text_5))
            withStyledLink(
                url = "https://apps.apple.com/app/id6755495730?action=write-review",
                text = stringResource(Res.string.about_app_text_6_ios)
            )
            append(stringResource(Res.string.about_app_text_7))
        },
        style = MaterialTheme.typography.bodyLarge
    )
}
