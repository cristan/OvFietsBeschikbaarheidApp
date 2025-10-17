package nl.ovfietsbeschikbaarheid.ext

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink

@Suppress("ComposableNaming")
@Composable
fun AnnotatedString.Builder.withStyledLink(
    url: String,
    text: String,
) {
    withLink(
        LinkAnnotation.Url(
            url = url, styles = TextLinkStyles(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            )
        )
    ) {
        append(text)
    }
}