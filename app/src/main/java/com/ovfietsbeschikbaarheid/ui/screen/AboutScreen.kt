package com.ovfietsbeschikbaarheid.ui.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ovfietsbeschikbaarheid.ui.components.OvCard
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import com.ovfietsbeschikbaarheid.ui.theme.Yellow50

@Composable
fun AboutScreen(
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current

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
                    .padding(start = 20.dp, end = 20.dp, bottom = 20.dp, top = 4.dp)
            ) {
                OvCard {
                    Text(
                        text = "Over de OV Fiets",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    val annotatedString = buildAnnotatedString {
                        append("Met je OV-chipkaart kun je een OV-fiets huren. Je hebt hiervoor een abonnement nodig dat je gratis online kunt afsluiten. Meer informatie vind je op ")
                        withLink(
                            LinkAnnotation.Url(
                                url = "https://ns.nl/ov-fiets", styles = TextLinkStyles(
                                    style = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            )
                        ) {
                            append("ns.nl/ov-fiets")
                        }
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
                        text = "Deze app is ontwikkeld op eigen initiatief en heeft dan ook geen enkele band met de Nederlandse Spoorwegen (de exploitant van de OV fiets).\n\nDe data komt van OpenOV, maar de bron hiervan is wel weer de NS, dus als er hier iets mis mee is, neem dan op met de NS klantenservice."
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