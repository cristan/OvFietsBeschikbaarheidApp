package nl.ovfietsbeschikbaarheid.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.buildAnnotatedString
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
        onBackClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutView(
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.content_description_back))
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.about_ov_fiets_title),
                    style = MaterialTheme.typography.headlineLarge,
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append(stringResource(R.string.about_ov_fiets_text_1))
                        withStyledLink(text = "ns.nl/ov-fiets", url = "https://ns.nl/ov-fiets")
                        append(stringResource(R.string.about_ov_fiets_text_2))
                    }
                )

                Text(
                    text = stringResource(R.string.about_app_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append(stringResource(R.string.about_app_text_1))
                        withStyledLink(text = "github.com/cristan/OvFietsBeschikbaarheidApp", url = "https://github.com/cristan/OvFietsBeschikbaarheidApp")
                        append(stringResource(R.string.about_app_text_2))
                    }
                )
                Text(
                    text = stringResource(R.string.about_credits_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = stringResource(R.string.about_credits_text_1)
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append(stringResource(R.string.about_credits_text_4))
                        withStyledLink(
                            text = stringResource(R.string.about_credits_text_5),
                            url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm"
                        )
                        append(stringResource(R.string.about_credits_text_6))
                    }
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
        onBackClicked = {})
}