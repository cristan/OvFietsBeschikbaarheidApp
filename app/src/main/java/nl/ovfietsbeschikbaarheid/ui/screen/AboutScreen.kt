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
    onBackClicked: () -> Unit
) {
    AboutView(
        "4,65",
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
                    text = "Over de OV-fiets",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        if (pricePer24Hours != null) {
                            append("Huur een OV-fiets voor €${pricePer24Hours} per 24 uur, op meer dan 300 locaties. ")
                        } else {
                            append("Huur een OV-fiets op meer dan 300 locaties. ")
                        }
                        append("Activeer hiervoor gratis een abonnement met je OV-chipkaart via ")

                        pushLink(
                            LinkAnnotation.Url("https://ns.nl/ov-fiets")
                        )
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("ns.nl/ov-fiets")
                        }
                        append(".")
                        pop()
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Over het slot",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        append("Op de meeste locaties gebruik je je OV-chipkaart als sleutel. ")
                        append("De instructies vind je op de bagagedrager of via ")

                        pushLink(
                            LinkAnnotation.Url("https://ov-fiets.nl/slot")
                        )
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("ov-fiets.nl/slot")
                        }
                        pop()

                        append(".")
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Over deze app",
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        append("Deze app is open source (broncode op ")

                        pushLink(
                            LinkAnnotation.Url("https://github.com/cristan/OvFietsBeschikbaarheidApp")
                        )
                        withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("GitHub")
                        }
                        pop()

                        append("). Het is niet door NS ontwikkeld, de data komt via OpenOV. ")
                        append(stringResource(R.string.about_credits_text_4))
                        withStyledLink(
                            text = stringResource(R.string.about_credits_text_5),
                            url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm"
                        )
                        append(stringResource(R.string.about_credits_text_6))
                        append("\n\nFeedback? Tevreden? Laat een review achter in de Play Store 🙂.")
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