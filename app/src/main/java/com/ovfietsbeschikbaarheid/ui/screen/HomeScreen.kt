package com.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.ui.unit.dp
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ovfietsbeschikbaarheid.R
import com.ovfietsbeschikbaarheid.TestData
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel

@Composable
fun HomeScreen(viewModel: LocationsViewModel = viewModel(), onLocationClick: (LocationOverviewModel) -> Unit) {
    val searchTerm by viewModel.searchTerm.collectAsState()
    val locations by viewModel.filteredLocations.collectAsState(emptyList())
    
    HomeView(searchTerm, locations, viewModel::onSearchTermChanged, onLocationClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeView(
    searchTerm: String,
    locations: List<LocationOverviewModel>,
    onSearchTermChanged: (String) -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit
) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text(stringResource(R.string.app_name))
                },
            )
        },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = {
                        onSearchTermChanged(it)
                    },
                    label = { Text("Zoekterm") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    singleLine = true,
                    trailingIcon = {
                        if (searchTerm.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "clear text",
                                modifier = Modifier
                                    .clickable {
                                        keyboardController?.hide()
                                        onSearchTermChanged("")
                                    }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn {
                    items(locations) { location ->
                        LocationCard(location) {
                            onLocationClick(location)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationCard(location: LocationOverviewModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            location.title,
            modifier = Modifier.weight(1f)
        )
        Row(
            modifier = Modifier.defaultMinSize(minWidth = 60.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.pedal_bike_24px),
                tint = if (isSystemInDarkTheme()) Color.White else Color.Black,
                contentDescription = "Navigeer",
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(
                location.rentalBikesAvailable?.toString() ?: "?"
            )
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    val locations = listOf(
        TestData.testLocationOverviewModel.copy(title = "Amsterdam Zuid Mahlerplein", rentalBikesAvailable = 49),
        TestData.testLocationOverviewModel.copy(title = "Amsterdam Zuid Zuidplein", rentalBikesAvailable = 148),
    )
    HomeView("Amsterdam Zuid", locations, {}, {})
}