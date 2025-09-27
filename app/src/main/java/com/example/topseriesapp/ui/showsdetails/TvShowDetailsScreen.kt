package com.example.topseriesapp.ui.showsdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
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
                    Text(
                        text = titleText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
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
            contentAlignment = Alignment.Center
        ) {
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
                    val show: TvShowDetails = currentState.tvShowDetails

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Encabezado con backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                        ) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w780${show.backdropPath}",
                                contentDescription = show.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                            startY = 50f
                                        )
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = show.name,
                                    style = MaterialTheme.typography.titleLarge.copy(color = Color.White)
                                )
                            }
                        }

                        Row(modifier = Modifier.padding(16.dp)) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w500${show.posterPath}",
                                contentDescription = show.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(120.dp)
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("⭐ ${show.voteAverage} (${show.voteCount} votos)",
                                    style = MaterialTheme.typography.bodyLarge)
                                Text("Estado: ${show.status}", style = MaterialTheme.typography.bodyMedium)
                                Text("Inicio: ${show.firstAirDate}", style = MaterialTheme.typography.bodyMedium)
                                Text("Temporadas: ${show.numberOfSeasons}", style = MaterialTheme.typography.bodyMedium)
                                Text("Episodios: ${show.numberOfEpisodes}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Géneros
                        if (!show.genres.isNullOrEmpty()) {
                            FlowRow(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp), // Reemplaza mainAxisSpacing
                                verticalArrangement = Arrangement.spacedBy(4.dp)    // Reemplaza crossAxisSpacing
                            ) {
                                show.genres.forEach { genre ->
                                    AssistChip(
                                        onClick = {},
                                        label = { genre.name?.let { Text(it) } }
                                    )
                                }
                            }
                        }

                        // Sinopsis
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Sinopsis", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(show.overview ?: "No disponible", style = MaterialTheme.typography.bodyMedium)
                        }

                        // Creadores
                        if (!show.createdBy.isNullOrEmpty()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Creadores", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    show.createdBy.forEach { creator ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w200${creator.profilePath}",
                                                contentDescription = creator.name,
                                                modifier = Modifier
                                                    .size(80.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            creator.name?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                        }
                                    }
                                }
                            }
                        }

                        // Temporadas
                        if (!show.seasons.isNullOrEmpty()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Temporadas", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                show.seasons.forEach { season ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(12.dp)) {
                                            AsyncImage(
                                                model = "https://image.tmdb.org/t/p/w200${season.posterPath}",
                                                contentDescription = season.name,
                                                modifier = Modifier
                                                    .width(80.dp)
                                                    .height(120.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                season.name?.let { Text(it, style = MaterialTheme.typography.titleSmall) }
                                                Text("Episodios: ${season.episodeCount}",
                                                    style = MaterialTheme.typography.bodySmall)
                                                Text(
                                                    season.overview?.takeIf { it.isNotEmpty() } ?: "Sin descripción",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
