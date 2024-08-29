package com.ovfietsbeschikbaarheid.ui.screen

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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ovfietsbeschikbaarheid.ext.withStyledLink
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import com.ovfietsbeschikbaarheid.ui.theme.Yellow50

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
                        Text("Info")
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClicked) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Terug")
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
                    text = "Over de OV-fiets",
                    style = MaterialTheme.typography.headlineLarge,
                )

                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append("Met je OV-chipkaart kun je een OV-fiets huren. Je hebt hiervoor een abonnement nodig dat je gratis online kunt afsluiten. Meer informatie vind je op ")
                        withStyledLink(text = "ns.nl/ov-fiets", url = "https://ns.nl/ov-fiets")
                        append(".")
                    }
                )

                Text(
                    text = "Over deze app",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append("Deze app is open source! Bekijk de broncode op ")
                        withStyledLink(text = "github.com/cristan/OvFietsBeschikbaarheidApp", url = "https://github.com/cristan/OvFietsBeschikbaarheidApp")
                        append(". De app is niet gelieerd aan de NS (de exploitant van de OV-fiets).")
                    }
                )
                Text(
                    text = "Credits",
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(top = 20.dp)
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "De data wordt geleverd door OpenOV, met de NS als uiteindelijke bron."
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append("Het totale aantal fietsen per locatie komt van ")
                        withStyledLink(text = "ovfietsbeschikbaar.nl", url = "https://ovfietsbeschikbaar.nl")
                        append(".")
                    }
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = buildAnnotatedString {
                        append("Het kaarticoon is gemaakt door ")
                        withStyledLink(
                            text = "brgfx op Freepik",
                            url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm"
                        )
                        append(".")
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