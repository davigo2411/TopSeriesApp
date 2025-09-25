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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topseriesapp.data.model.TvShow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularTvShowsScreen(
    viewModel: PopularTvShowsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Series Populares") },
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
                uiState.isLoading && uiState.tvShows.isEmpty() -> { // Carga inicial
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null && uiState.tvShows.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Error: ${uiState.error}")
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { viewModel.retryInitialLoad() }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.tvShows.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay series populares para mostrar.")
                    }
                }

                else -> { // Mostrar la lista de series
                    PopularTvShowList(
                        uiState = uiState,
                        onLoadMore = { viewModel.loadNextPage() },
                        onItemClick = { tvShow ->
                            println("Clicked on: ${tvShow.name}")
                        }
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
        modifier = modifier.fillMaxSize(),
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mostrar indicador de carga o error al final de la lista
        if (uiState.isLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else if (uiState.error != null && uiState.canLoadMore) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error al cargar más: ${uiState.error}", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onLoadMore) {
                        Text("Reintentar")
                    }
                }
            }
        }
    }

    //Detectar el scroll al final de la lista
    val isScrolledToEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (layoutInfo.totalItemsCount == 0) {
                false
            } else {
                val lastVisibleItem = visibleItemsInfo.lastOrNull()
                // Detecta el último item visible y si no se puede scrollear más
                lastVisibleItem != null && lastVisibleItem.index == layoutInfo.totalItemsCount - 1 &&
                        !listState.canScrollForward
            }
        }
    }

    LaunchedEffect(isScrolledToEnd, uiState.canLoadMore, uiState.isLoading) {
        if (isScrolledToEnd && uiState.canLoadMore && !uiState.isLoading) {
            onLoadMore()
        }
    }
}