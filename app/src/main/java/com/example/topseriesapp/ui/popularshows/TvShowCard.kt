package com.example.topseriesapp.ui.popularshows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.topseriesapp.R
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.utils.getImageUrl
import java.util.Locale
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme



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
                .testTag("tvShowCard_rowContainer_${tvShow.id}")
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrl(tvShow.posterPath))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .build(),
                contentDescription = stringResource(R.string.poster_of, tvShow.name),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .testTag("tvShowCard_image_${tvShow.id}")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight()
                    .weight(1f)
                    .testTag("tvShowCard_detailsColumn_${tvShow.id}"),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tvShow.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("tvShowCard_title_${tvShow.id}")
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tvShow.firstAirDate?.take(4) ?: stringResource(R.string.na),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("tvShowCard_airDate_${tvShow.id}")
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Fila para la valoración y la estrella
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("tvShowCard_ratingRow_${tvShow.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = stringResource(R.string.rating),
                        modifier = Modifier
                            .size(16.dp)
                            .testTag("tvShowCard_starIcon_${tvShow.id}"),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", tvShow.voteAverage),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.testTag("tvShowCard_voteAverage_${tvShow.id}")
                    )
                }
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
                name = "Very Long TV Show Name That Should Make Ellipsis",
                overview = "This is a TV show description.",
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