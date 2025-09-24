package com.example.topseriesapp.ui.popularshows

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PopularTvShowsScreen(
    viewModel: PopularTvShowsViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.error != null -> {
                Text("Error: ${uiState.error}")
            }
            //Carga inicial
            uiState.isLoading && uiState.tvShows.isEmpty() -> {
                CircularProgressIndicator()
            }
            //No hay series
            uiState.tvShows.isEmpty() -> {
                Text("No hay series populares para mostrar.")
            }
            else -> {
                // Mostrar la lista de series
                Text("¡Series cargadas! Mostrando ${uiState.tvShows.size} series.")
            }
        }
    }
}