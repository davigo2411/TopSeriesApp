package com.example.topseriesapp.ui.popularshows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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

                uiState.error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${uiState.error}")
                    }
                }

                uiState.tvShows.isEmpty() && !uiState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay series populares para mostrar.")
                    }
                }

                uiState.tvShows.isNotEmpty() -> {
                    PopularTvShowList(
                        tvShows = uiState.tvShows,
                        onItemClick = { tvShow ->
                            // Futuro manejo de detalles de la serie
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
    tvShows: List<com.example.topseriesapp.data.model.TvShow>,
    onItemClick: (com.example.topseriesapp.data.model.TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre TvShowCards
    ) {
        items(
            items = tvShows,
            key = { tvShow -> tvShow.id }
        ) { tvShow ->
            TvShowCard(
                tvShow = tvShow,
                onItemClick = onItemClick
            )
        }
    }
}