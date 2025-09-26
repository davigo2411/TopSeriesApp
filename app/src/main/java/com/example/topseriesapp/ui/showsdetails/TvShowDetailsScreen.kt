package com.example.topseriesapp.ui.showsdetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.topseriesapp.data.model.TvShowDetails
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvShowDetailsScreen(
    navController: NavController,
    viewModel: TvShowDetailsViewModel = koinViewModel()
) {
    val uiState: DetailsScreenUiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val titleText = when (uiState) {
                        is DetailsScreenUiState.Success -> (uiState as DetailsScreenUiState.Success).tvShowDetails.name
                        else -> "Details"
                    }
                    Text(text = titleText)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            // Manejo de la UI basada en el tipo de DetailsScreenUiState
            when (val currentState = uiState) {
                is DetailsScreenUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is DetailsScreenUiState.Error -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${currentState.message}")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
                is DetailsScreenUiState.Success -> {
                    // Acceso a tvShowDetails desde currentState
                    val show: TvShowDetails = currentState.tvShowDetails
                    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                        Text(text = show.name , style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        // AsyncImage(model = "https://image.tmdb.org/t/p/w500${show.posterPath}", contentDescription = show.name)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Overview:", style = MaterialTheme.typography.titleMedium)
                        Text(text = show.overview ?: "No overview available.")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "First Aired: ${show.firstAirDate ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "Rating: ${show.voteAverage ?: "N/A"} (${show.voteCount ?: 0} votes)", style = MaterialTheme.typography.bodyMedium)
                        // ... más detalles
                    }
                }
            }
        }
    }
}
