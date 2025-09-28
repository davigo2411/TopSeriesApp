package com.example.topseriesapp.ui.showsdetails

import android.content.res.Configuration
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.topseriesapp.R
import com.example.topseriesapp.data.model.TvShowDetails
import org.koin.androidx.compose.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
                    val titleText = when (val currentUiState = uiState) {
                        is DetailsScreenUiState.Success -> currentUiState.tvShowDetails.name
                        else -> stringResource(R.string.details)
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
                            contentDescription = stringResource(R.string.back)
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
                        Text(stringResource(R.string.error_prefix) + currentState.message)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
                is DetailsScreenUiState.Success -> {
                    val show: TvShowDetails = currentState.tvShowDetails
                    val configuration = LocalConfiguration.current
                    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

                    if (isLandscape) {
                        TvShowDetailsLandscapeLayout(show = show)
                    } else {
                        TvShowDetailsPortraitLayout(show = show)
                    }
                }
            }
        }
    }
}

@Composable
fun TvShowDetailsPortraitLayout(show: TvShowDetails) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        ShowDetailsHeader(show)
        ShowDetailsMainInfoRow(show, modifier = Modifier.padding(16.dp))
        ShowDetailsGenres(show, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
        ShowDetailsSynopsis(show, modifier = Modifier.padding(16.dp))
        ShowDetailsCreators(show, modifier = Modifier.padding(16.dp))
        ShowDetailsSeasons(show, modifier = Modifier.padding(16.dp))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TvShowDetailsLandscapeLayout(show: TvShowDetails) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.weight(0.4f)
        ) {
            AsyncImage(
                model = "https://image.tmdb.org/t/p/w500${show.posterPath}",
                contentDescription = stringResource(R.string.poster_of, show.name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            ShowDetailsGenres(show)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(0.6f)
        ) {
            Text(
                text = show.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShowDetailsCoreInfo(show)
            Spacer(modifier = Modifier.height(16.dp))
            ShowDetailsSynopsis(show)
            Spacer(modifier = Modifier.height(16.dp))
            ShowDetailsCreators(show)
            Spacer(modifier = Modifier.height(16.dp))
            ShowDetailsSeasons(show)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ShowDetailsHeader(show: TvShowDetails, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
}

@Composable
fun ShowDetailsMainInfoRow(show: TvShowDetails, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        AsyncImage(
            model = "https://image.tmdb.org/t/p/w500${show.posterPath}",
            contentDescription = stringResource(R.string.poster_of, show.name),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(120.dp)
                .aspectRatio(2f / 3f)
                .clip(RoundedCornerShape(12.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        ShowDetailsCoreInfo(show)
    }
}

@Composable
fun ShowDetailsCoreInfo(show: TvShowDetails, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            stringResource(R.string.star) + " ${show.voteAverage} " +
                    stringResource(R.string.votes, show.voteCount ?: 0),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.state, show.status ?: stringResource(R.string.na)),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.start_date, show.firstAirDate ?: stringResource(R.string.na)),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.seasons_count, show.numberOfSeasons ?: 0),
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            stringResource(R.string.episodes_count, show.numberOfEpisodes ?: 0),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShowDetailsGenres(show: TvShowDetails, modifier: Modifier = Modifier) {
    if (!show.genres.isNullOrEmpty()) {
        Text(
            text = stringResource(R.string.genres),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier.padding(bottom = 8.dp)
        )
        FlowRow(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            show.genres.forEach { genre ->
                AssistChip(
                    onClick = { },
                    label = { genre.name?.let { Text(it) } }
                )
            }
        }
    }
}

@Composable
fun ShowDetailsSynopsis(show: TvShowDetails, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(stringResource(R.string.synopsis), style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            show.overview ?: stringResource(R.string.not_available),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ShowDetailsCreators(show: TvShowDetails, modifier: Modifier = Modifier) {
    if (!show.createdBy.isNullOrEmpty()) {
        Column(modifier = modifier) {
            Text(stringResource(R.string.creators), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                        Spacer(modifier = Modifier.height(4.dp))
                        creator.name?.let { Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    }
                }
            }
        }
    }
}

@Composable
fun ShowDetailsSeasons(show: TvShowDetails, modifier: Modifier = Modifier) {
    if (!show.seasons.isNullOrEmpty()) {
        Column(modifier = modifier) {
            Text(stringResource(R.string.seasons), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                show.seasons.filter { it.seasonNumber != null && it.seasonNumber > 0 }.forEach { season ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = "https://image.tmdb.org/t/p/w200${season.posterPath}",
                                contentDescription = season.name,
                                modifier = Modifier
                                    .width(80.dp)
                                    .aspectRatio(2f/3f)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                season.name?.let { Text(it, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
                                season.airDate?.let { airDateValue ->
                                    Text(
                                        stringResource(R.string.season_air_date, airDateValue), // Usa el nuevo string
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    stringResource(R.string.episodes_count, season.episodeCount ?: 0),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (!season.overview.isNullOrBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        season.overview,
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

