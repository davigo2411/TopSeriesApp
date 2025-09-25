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
import androidx.compose.ui.platform.testTag // Asegúrate de tener este import
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topseriesapp.data.model.TvShow // O tu paquete correcto para TvShow
// Asume que PopularTvShowsUiState y PopularTvShowsViewModel están disponibles/importados

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PopularTvShowsScreen(
    viewModel: PopularTvShowsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.testTag("popularTvShowsScreen_scaffold"), // Tag para el Scaffold
        topBar = {
            TopAppBar(
                title = { Text("Popular Tv shows") }, // El texto se puede buscar directamente
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
                                Text("Reintentar")
                            }
                        }
                    }
                }

                uiState.tvShows.isEmpty() -> { // Estado vacío después de carga, sin error
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay series populares para mostrar.",
                            modifier = Modifier.testTag("emptyStateText")
                        )
                    }
                }

                else -> { // Mostrar la lista de series
                    PopularTvShowList(
                        uiState = uiState,
                        onLoadMore = { viewModel.loadNextPage() },
                        onItemClick = { tvShow ->
                            println("Clicked on: ${tvShow.name}")
                            // Aquí manejarías la navegación en la app real
                        },
                        // Pasamos un modifier con testTag si es necesario para el contenedor de la lista
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
    modifier: Modifier = Modifier // Recibe el modifier del llamador
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag("tvShowLazyList"), // Aplicar testTag a LazyColumn
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(
            items = uiState.tvShows,
            key = { _, tvShow -> tvShow.id }
        ) { _, tvShow ->
            // Asumiendo que TvShowCard es un Composable tuyo.
            // Es bueno si TvShowCard internamente tiene elementos con testTag
            // o si TvShowCard mismo tiene un testTag más específico si es necesario.
            // Para encontrar items individuales, buscar por texto (nombre de la serie)
            // o un testTag dinámico en TvShowCard es una buena estrategia.
            TvShowCard(
                tvShow = tvShow,
                onItemClick = onItemClick,
                modifier = Modifier.fillMaxWidth().testTag("tvShowItem_${tvShow.id}") // Tag dinámico por item
            )
        }

        // Mostrar indicador de carga o error al final de la lista para paginación
        if (uiState.isLoading) { // Asumo que isLoading se usa para carga de "más" cuando ya hay items
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                        .testTag("loadMoreIndicatorContainer"), // Tag para el contenedor del indicador
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loadMoreIndicator"))
                }
            }
        } else if (uiState.error != null && uiState.canLoadMore) { // Error específico de paginación
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("loadMoreErrorContainer"), // Tag para el contenedor del error de paginación
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error al cargar más: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("loadMoreErrorMessage")
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onLoadMore, // Debería ser viewModel.retryLoadMore() o similar si tienes lógica separada
                        modifier = Modifier.testTag("loadMoreRetryButton")
                    ) {
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

