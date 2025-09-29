package nl.ovfietsbeschikbaarheid.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.R
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50

@Composable
fun AboutScreen(
    pricePer24Hours: String?,
    onBackClicked: () -> Unit
) {
    AboutView(
        pricePer24Hours,
        onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutView(
    pricePer24Hours: String?,
    onBackClicked: () -> Unit
) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = Yellow50,
                        navigationIconContentColor = Yellow50
                    ),
                    title = {
                        Text(stringResource(R.string.about_title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.content_description_back)
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_ov_fiets_title),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        if (pricePer24Hours != null) {
                            append(stringResource(R.string.about_ov_fiets_text_1_with_amount, pricePer24Hours))
                        } else {
                            append(stringResource(R.string.about_ov_fiets_text_1_without_amount))
                        }
                        append(stringResource(R.string.about_ov_fiets_text_2))

                        pushLink(
                            LinkAnnotation.Url("https://ns.nl/ov-fiets")
                        )
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("ns.nl/ov-fiets")
                        }
                        append(stringResource(R.string.about_ov_fiets_text_3))
                        pop()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.about_lock_title),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.about_lock_text_1))

                        pushLink(
                            LinkAnnotation.Url("https://ov-fiets.nl/slot")
                        )
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("ov-fiets.nl/slot")
                        }
                        pop()

                        append(stringResource(R.string.about_lock_text_2))
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(R.string.about_app_title),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.about_app_text_1))

                        pushLink(
                            LinkAnnotation.Url("https://github.com/cristan/OvFietsBeschikbaarheidApp")
                        )
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append(stringResource(R.string.about_app_text_1_on_github))
                        }
                        pop()

                        append(stringResource(R.string.about_app_text_2))
                        withStyledLink(
                            text = stringResource(R.string.about_app_text_3),
                            url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm"
                        )
                        append(stringResource(R.string.about_app_text_4))
                        append("\n\n")
                        append(stringResource(R.string.about_app_text_5))
                        withStyledLink(
                            text = stringResource(R.string.about_app_text_6),
                            url = "https://play.google.com/store/apps/details?id=nl.ovfietsbeschikbaarheid"
                        )
                        append(stringResource(R.string.about_app_text_7))
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark mode")
@Composable
fun AboutPreview() {
    AboutView(
        "4,65",
        onBackClicked = {}
    )
}