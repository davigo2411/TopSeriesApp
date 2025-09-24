package com.example.topseriesapp.ui.popularshows

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.topseriesapp.R
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme
import com.example.topseriesapp.utils.getImageUrl

@Composable
fun TvShowCard(
    tvShow: TvShow,
    onItemClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onItemClick(tvShow) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrl(tvShow.posterPath))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .build(),
                contentDescription = "Poster de ${tvShow.name}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tvShow.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tvShow.firstAirDate?.take(4) ?: "N/A",
                    style = MaterialTheme.typography.bodySmall
                )
                // Se puede agregar más información aquí
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TvShowCardPreview() {
    TopSeriesAppTheme {
        TvShowCard(
            tvShow = TvShow(
                id = 1,
                name = "Nombre de Serie Muy Largo Que Debería Hacer Ellipsis",
                overview = "Esta es una descripción de la serie.",
                posterPath = "/ejMNOIsL22GPdAATn2s5xK3q2S5.jpg",
                backdropPath = null,
                voteAverage = 8.5,
                voteCount = 1000,
                firstAirDate = "2023-10-26",
                genreIds = emptyList(),
                originCountry = emptyList(),
                originalLanguage = "en",
                originalName = "Original Name",
                popularity = 100.0
            ),
            onItemClick = {}
        )
    }
}
