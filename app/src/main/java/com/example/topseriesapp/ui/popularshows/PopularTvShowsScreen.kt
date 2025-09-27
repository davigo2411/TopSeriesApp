package com.example.topseriesapp.ui.popularshows

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
                title = { Text("Popular TV Shows") },
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
                uiState.isLoading && uiState.tvShows.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.testTag("initialLoadingIndicator"))
                    }
                }

                uiState.error != null && uiState.tvShows.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Error: ${uiState.error}",
                                modifier = Modifier.testTag("initialErrorMessage")
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.retryInitialLoad() },
                                modifier = Modifier.testTag("initialRetryButton")
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }

                uiState.tvShows.isEmpty()  -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No popular TV shows to display.",
                            modifier = Modifier.testTag("emptyStateText")
                        )
                    }
                }

                else -> {
                    PopularTvShowList(
                        uiState = uiState,
                        onLoadMore = { viewModel.loadNextPage() },
                        onItemClick = { tvShow ->
                            onNavigateToDetails(tvShow.id)
                        },
                        modifier = Modifier.testTag("popularTvShowList_container")
                    )
                }
            }
        }
    }
}

@Composable
fun PopularTvShowList(
    uiState: PopularTvShowsUiState,
    onLoadMore: () -> Unit,
    onItemClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag("tvShowLazyList"),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = uiState.tvShows,
            key = { _, tvShow -> tvShow.id }
        ) { _, tvShow ->
            TvShowCard(
                tvShow = tvShow,
                onItemClick = onItemClick,
                modifier = Modifier.fillMaxWidth().testTag("tvShowItem_${tvShow.id}")
            )
        }

        if (uiState.isLoading && uiState.tvShows.isNotEmpty()) {
            item {
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
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("loadMoreErrorContainer"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error loading more: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("loadMoreErrorMessage")
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onLoadMore,
                        modifier = Modifier.testTag("loadMoreRetryButton")
                    ) {
                        Text("Retry")
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
                lastVisibleItem != null && lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                        !listState.canScrollForward
            }
        }
    }

    LaunchedEffect(isScrolledToEnd, uiState.canLoadMore, uiState.isLoading) {
        if (isScrolledToEnd && uiState.canLoadMore && !uiState.isLoading && uiState.tvShows.isNotEmpty()) {
            onLoadMore()
        }
    }
}
