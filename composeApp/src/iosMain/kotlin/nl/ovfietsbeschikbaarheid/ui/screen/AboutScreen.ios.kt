package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.runtime.Composable
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_review_app_store
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ReviewCallToAction()  {
    ReviewButton(
        url = "https://apps.apple.com/app/id6755495730?action=write-review",
        text = stringResource(Res.string.about_app_review_app_store)
    )
}
