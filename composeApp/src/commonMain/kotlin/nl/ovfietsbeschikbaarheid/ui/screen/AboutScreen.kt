package nl.ovfietsbeschikbaarheid.ui.screen

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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.ext.withStyledLink
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.about_app_text_1
import nl.ovfietsbeschikbaarheid.resources.about_app_text_1_on_github
import nl.ovfietsbeschikbaarheid.resources.about_app_text_2
import nl.ovfietsbeschikbaarheid.resources.about_app_text_3
import nl.ovfietsbeschikbaarheid.resources.about_app_text_4
import nl.ovfietsbeschikbaarheid.resources.about_app_title
import nl.ovfietsbeschikbaarheid.resources.about_lock_text_1
import nl.ovfietsbeschikbaarheid.resources.about_lock_text_2
import nl.ovfietsbeschikbaarheid.resources.about_lock_title
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_text_1_with_amount
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_text_1_without_amount
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_text_2
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_text_3
import nl.ovfietsbeschikbaarheid.resources.about_ov_fiets_title
import nl.ovfietsbeschikbaarheid.resources.about_title
import nl.ovfietsbeschikbaarheid.resources.content_description_back
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

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
                        Text(stringResource(Res.string.about_title))
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(Res.string.content_description_back)
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
                    text = stringResource(Res.string.about_ov_fiets_title),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        if (pricePer24Hours != null) {
                            append(stringResource(Res.string.about_ov_fiets_text_1_with_amount, pricePer24Hours))
                        } else {
                            append(stringResource(Res.string.about_ov_fiets_text_1_without_amount))
                        }
                        append(stringResource(Res.string.about_ov_fiets_text_2))

                        withStyledLink("https://ns.nl/ov-fiets", "ns.nl/ov-fiets")

                        append(stringResource(Res.string.about_ov_fiets_text_3))
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(Res.string.about_lock_title),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.about_lock_text_1))

                        withStyledLink("https://ov-fiets.nl/slot", "ov-fiets.nl/slot")

                        append(stringResource(Res.string.about_lock_text_2))
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = stringResource(Res.string.about_app_title),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.about_app_text_1))

                        withStyledLink(
                            url = "https://github.com/cristan/OvFietsBeschikbaarheidApp",
                            text = stringResource(Res.string.about_app_text_1_on_github)
                        )

                        append(stringResource(Res.string.about_app_text_2))
                        withStyledLink(
                            url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm",
                            text = stringResource(Res.string.about_app_text_3)
                        )
                        append(stringResource(Res.string.about_app_text_4))
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                ReviewCallToAction()
            }
        }
    }
}

@Composable
expect fun ReviewCallToAction()

@Preview
@Composable
fun AboutPreview() {
    AboutView(
        "4,65",
        onBackClicked = {}
    )
}