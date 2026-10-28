package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.runtime.Composable
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_review_play_store
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ReviewCallToAction()  {
    ReviewButton(
        url = "https://play.google.com/store/apps/details?id=nl.ovfietsbeschikbaarheid",
        text = stringResource(Res.string.about_app_review_play_store)
    )
}