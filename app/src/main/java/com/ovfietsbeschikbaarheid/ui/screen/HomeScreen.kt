package com.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.ui.unit.dp
import com.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.ovfietsbeschikbaarheid.viewmodel.LocationsViewModel
import com.ovfietsbeschikbaarheid.model.LocationOverviewModel

@Composable
fun HomeScreen(viewModel: LocationsViewModel = viewModel(), onLocationClick: (LocationOverviewModel) -> Unit) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val searchTerm by viewModel.searchTerm.collectAsState()
                val keyboardController = LocalSoftwareKeyboardController.current
                OutlinedTextField(
                    value = searchTerm,
                    onValueChange = {
                        viewModel.onSearchTermChanged(it)
                    },
                    label = { Text("Zoekterm") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        if(searchTerm.isNotEmpty()) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "clear text",
                                modifier = Modifier
                                    .clickable {
                                        keyboardController?.hide()
                                        viewModel.onSearchTermChanged("")
                                    }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                val locations by viewModel.filteredLocations.collectAsState(emptyList())
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Text(
            "${location.title} ${location.rentalBikesAvailable?.toString() ?: "?"}",
            modifier = Modifier.padding(16.dp)
        )
    }
}