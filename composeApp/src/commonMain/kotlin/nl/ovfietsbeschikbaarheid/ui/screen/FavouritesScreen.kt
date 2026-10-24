package nl.ovfietsbeschikbaarheid.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import nl.ovfietsbeschikbaarheid.TestData
import nl.ovfietsbeschikbaarheid.ext.OnReturnToScreenEffect
import nl.ovfietsbeschikbaarheid.model.Favourite
import nl.ovfietsbeschikbaarheid.model.FavouritesModel
import nl.ovfietsbeschikbaarheid.model.LocationOverviewModel
import nl.ovfietsbeschikbaarheid.resources.Res
import nl.ovfietsbeschikbaarheid.resources.content_description_remove_favourite
import nl.ovfietsbeschikbaarheid.resources.favourites_empty_text
import nl.ovfietsbeschikbaarheid.resources.favourites_empty_title
import nl.ovfietsbeschikbaarheid.resources.favourites_location_not_found
import nl.ovfietsbeschikbaarheid.resources.favourites_title
import nl.ovfietsbeschikbaarheid.state.ScreenState
import nl.ovfietsbeschikbaarheid.ui.theme.OVFietsBeschikbaarheidTheme
import nl.ovfietsbeschikbaarheid.ui.theme.Yellow50
import nl.ovfietsbeschikbaarheid.ui.view.FullPageError
import nl.ovfietsbeschikbaarheid.viewmodel.FavouritesContent
import nl.ovfietsbeschikbaarheid.viewmodel.FavouritesViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun FavouritesScreen(
    viewModel: FavouritesViewModel = koinViewModel(),
    onLocationClick: (LocationOverviewModel) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.refreshIfOutdated()
    }
    OnReturnToScreenEffect(viewModel::refreshIfOutdated)

    val screenState by viewModel.screenState
    FavouritesView(
        screenState,
        viewModel::onRetryClick,
        viewModel::onPullToRefresh,
        viewModel::onRemoveFavouriteClick,
        onLocationClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavouritesView(
    screenState: ScreenState<FavouritesContent>,
    onRetry: () -> Unit,
    onPullToRefresh: () -> Unit,
    onRemoveFavouriteClick: (Favourite) -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit,
) {
    OVFietsBeschikbaarheidTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = Yellow50,
                    ),
                    title = {
                        Text(stringResource(Res.string.favourites_title))
                    },
                )
            },
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            when (screenState) {
                ScreenState.FullPageError -> FullPageError(modifier = Modifier.padding(innerPadding), onClick = onRetry)
                ScreenState.Loading -> Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                is ScreenState.Loaded -> when (screenState.data) {
                    FavouritesContent.NoFavourites -> NoFavourites(Modifier.padding(innerPadding))
                    is FavouritesContent.Content -> PullToRefreshBox(
                        state = rememberPullToRefreshState(),
                        modifier = Modifier.padding(innerPadding),
                        isRefreshing = screenState.isRefreshing,
                        onRefresh = onPullToRefresh,
                    ) {
                        FavouriteLocations(screenState.data.favourites, onRemoveFavouriteClick, onLocationClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun FavouriteLocations(
    favourites: FavouritesModel,
    onRemoveFavouriteClick: (Favourite) -> Unit,
    onLocationClick: (LocationOverviewModel) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentWidth(Alignment.CenterHorizontally)
            .widthIn(max = 800.dp)
    ) {
        items(favourites.locations) { location ->
            LocationCard(location) {
                onLocationClick(location)
            }
        }
        items(favourites.favouritesWithoutLocation) { favourite ->
            FavouriteWithoutLocation(favourite) {
                onRemoveFavouriteClick(favourite)
            }
        }
        item {
            Spacer(
                Modifier.windowInsetsBottomHeight(WindowInsets.systemBars)
            )
        }
    }
}

@Composable
private fun FavouriteWithoutLocation(favourite: Favourite, onRemoveClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = favourite.title,
            )
            Text(
                text = stringResource(Res.string.favourites_location_not_found),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        IconButton(onClick = onRemoveClick) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = stringResource(Res.string.content_description_remove_favourite)
            )
        }
    }
}

@Composable
private fun NoFavourites(modifier: Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = stringResource(Res.string.favourites_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(Res.string.favourites_empty_text),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 64.dp)
        )
    }
}

@Preview
@Composable
fun FavouritesPreview() {
    val favourites = FavouritesModel(
        locations = listOf(
            TestData.testLocationOverviewModel.copy(
                title = "Amsterdam Centraal Stationsplein",
                rentalBikesAvailable = 228
            ),
            TestData.testLocationOverviewModel.copy(
                title = "Utrecht Centraal Stationsplein",
                rentalBikesAvailable = 806
            ),
        ),
        favouritesWithoutLocation = listOf(Favourite("uto001", "Utrecht Overvecht")),
    )
    FavouritesView(ScreenState.Loaded(FavouritesContent.Content(favourites)), {}, {}, {}, {})
}

@Preview
@Composable
fun NoFavouritesPreview() {
    FavouritesView(ScreenState.Loaded(FavouritesContent.NoFavourites), {}, {}, {}, {})
}
