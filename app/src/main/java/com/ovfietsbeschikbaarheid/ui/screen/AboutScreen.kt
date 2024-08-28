package com.ovfietsbeschikbaarheid.ui.screen

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
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ovfietsbeschikbaarheid.ext.withStyledLink
import com.ovfietsbeschikbaarheid.ui.components.OvCard
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
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)
            ) {
                OvCard {
                    Text(
                        text = "Over de OV Fiets",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    val annotatedString = buildAnnotatedString {
                        append("Met je OV-chipkaart kun je een OV-fiets huren. Je hebt hiervoor een abonnement nodig dat je gratis online kunt afsluiten. Meer informatie vind je op ")
                        withStyledLink(text = "ns.nl/ov-fiets", url = "https://ns.nl/ov-fiets")
                        append(".")
                    }

                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = annotatedString
                    )
                }
                OvCard {
                    Text(
                        text = "Over deze app",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "Deze app is ontwikkeld op eigen initiatief en is dan ook niet gelieerd aan de Nederlandse Spoorwegen (de exploitant van de OV fiets)."
                    )
                }
                OvCard {
                    Text(
                        text = "Bij niet-kloppende data",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "De data van deze app komt van OpenOV, maar de bron hiervan is de NS. Als er iets mis is met data als het aantal beschikbare OV fietsen, de openingstijden of het adres, neem dan op met de NS klantenservice."
                    )
                }
                OvCard {
                    Text(
                        text = "Credits",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    val annotatedString = buildAnnotatedString {
                        append("Kaart icoon gemaakt door ")
                        withLink(
                            LinkAnnotation.Url(
                                url = "https://www.freepik.com/free-vector/map-white-background_4485469.htm", styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                        ) {
                            append("brgfx op Freepik")
                        }
                        append(".")
                    }
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = annotatedString
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun AboutPreview() {
    AboutView(
        onBackClicked = {})
}