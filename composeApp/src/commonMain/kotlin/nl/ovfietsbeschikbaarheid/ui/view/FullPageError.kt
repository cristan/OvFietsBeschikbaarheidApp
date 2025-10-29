package nl.ovfietsbeschikbaarheid.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.full_page_error_retry
import nl.ovfietsbeschikbaarheid.resources.full_page_error_text
import nl.ovfietsbeschikbaarheid.resources.full_page_error_title
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun FullPageError(
    title: String = stringResource(Res.string.full_page_error_title),
    message: String = stringResource(Res.string.full_page_error_text),
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            text = message,
            textAlign = TextAlign.Center
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            onClick = onRetry
        ) {
            Text(
                text = stringResource(Res.string.full_page_error_retry),
            )
        }
    }
}

@Preview()
@Composable
fun FullPageErrorPreview() {
    OVFietsBeschikbaarheidTheme {
        FullPageError {

        }
    }
}