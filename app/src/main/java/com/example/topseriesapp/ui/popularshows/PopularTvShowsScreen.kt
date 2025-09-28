package com.example.topseriesapp.ui.popularshows

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topseriesapp.R
import com.example.topseriesapp.data.model.TvShow


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularTvShowsScreen(
    viewModel: PopularTvShowsViewModel,
    onNavigateToDetails: (seriesId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.testTag("popularTvShowsScreen_scaffold"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.popular_tv_shows)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when {
                uiState.isFirstPageLoading && uiState.tvShows.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("initialLoadingIndicator"))
                    }
                }

                uiState.error != null && uiState.tvShows.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.error_prefix) + uiState.error,
                                modifier = Modifier.testTag("initialErrorMessage")
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.retryInitialLoad() },
                                modifier = Modifier.testTag("initialRetryButton")
                            ) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
                !uiState.isLoading && uiState.tvShows.isEmpty() && uiState.error == null && (uiState.currentPage == 0 && uiState.totalPages == 0) -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.no_popular_shows),
                            modifier = Modifier.testTag("emptyStateText")
                        )
                    }
                }

                else -> {
                    PopularTvShowGrid(
                        uiState = uiState,
                        onLoadMore = { viewModel.loadNextPage() },
                        onItemClick = { tvShow ->
                            onNavigateToDetails(tvShow.id)
                        },
                        modifier = Modifier.testTag("popularTvShowGrid_container")
                    )
                }
            }
        }
    }
}

@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
fun PopularTvShowGrid(
    uiState: PopularTvShowsUiState,
    onLoadMore: () -> Unit,
    onItemClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyGridState()
    val configuration = LocalConfiguration.current

    val columns = when (configuration.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            if (configuration.screenWidthDp.dp > 720.dp) {
                4
            } else {
                3
            }
        }
        else -> {
            1
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        state = listState,
        modifier = modifier.fillMaxSize().testTag("tvShowLazyContent"),
        contentPadding = PaddingValues(
            horizontal = if (columns > 1) 8.dp else 0.dp,
            vertical = 8.dp
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = if (columns > 1) Arrangement.spacedBy(8.dp) else Arrangement.Start
    ) {
        itemsIndexed(
            items = uiState.tvShows,
            key = { _, tvShow -> tvShow.id }
        ) { _, tvShow ->
            TvShowCard(
                tvShow = tvShow,
                onItemClick = onItemClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tvShowItem_${tvShow.id}")
            )
        }

        if (uiState.isNextPageLoading && uiState.tvShows.isNotEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(columns) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .testTag("loadMoreIndicatorContainer"),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loadMoreIndicator"))
                }
            }
        } else if (uiState.error != null && uiState.canLoadMore && uiState.tvShows.isNotEmpty()) {
            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(columns) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("loadMoreErrorContainer"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.error_loading_more) + uiState.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("loadMoreErrorMessage")
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier.testTag("loadMoreRetryButton")
                    ) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }

    val isScrolledToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo

            if (layoutInfo.totalItemsCount == 0 || uiState.tvShows.isEmpty()) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                if (columns == 1) {
                    lastVisibleItem != null &&
                            lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                            !listState.canScrollForward
                } else {
                    lastVisibleItem != null &&
                            lastVisibleItem.index >= layoutInfo.totalItemsCount - (columns * 1.5).toInt() &&
                            (layoutInfo.totalItemsCount < columns * 2 || listState.canScrollForward)
                }
            }
        }
    }

    LaunchedEffect(isScrolledToEnd, uiState.canLoadMore, uiState.isLoading) {
        if (isScrolledToEnd && uiState.canLoadMore && !uiState.isLoading && uiState.tvShows.isNotEmpty()) {
            onLoadMore()
        }
    }
}

