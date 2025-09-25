package com.example.topseriesapp.ui.popularshows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size // Para el tamaño del ícono
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons // Import para los íconos base
import androidx.compose.material.icons.filled.Star // Import para el ícono de estrella
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment // Para alinear el texto y la estrella
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Para el color de la estrella
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.topseriesapp.R
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.utils.getImageUrl
import java.util.Locale // Para formatear el número decimal
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme



@Composable
fun TvShowCard(
    tvShow: TvShow,
    onItemClick: (TvShow) -> Unit,
    modifier: Modifier = Modifier // Recibe el modifier del llamador (que puede tener "tvShowItem_${tvShow.id}")
) {
    Card(
        modifier = modifier // El modifier pasado ya puede contener el testTag principal del item
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = { onItemClick(tvShow) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .testTag("tvShowCard_rowContainer_${tvShow.id}") // Tag para el Row interno
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(getImageUrl(tvShow.posterPath))
                    .crossfade(true)
                    .placeholder(R.drawable.placeholder_image) // Asegúrate que este drawable existe
                    .error(R.drawable.error_image)           // Asegúrate que este drawable existe
                    .build(),
                contentDescription = "Poster de ${tvShow.name}", // Bueno para accesibilidad y tests
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .testTag("tvShowCard_image_${tvShow.id}") // Tag para la imagen
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxHeight()
                    .weight(1f)
                    .testTag("tvShowCard_detailsColumn_${tvShow.id}"), // Tag para la columna de detalles
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = tvShow.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("tvShowCard_title_${tvShow.id}") // Tag para el título
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tvShow.firstAirDate?.take(4) ?: "N/A",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("tvShowCard_airDate_${tvShow.id}") // Tag para la fecha
                )
                Spacer(modifier = Modifier.height(6.dp)) // Más espacio antes de la valoración

                // Fila para la valoración y la estrella
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.testTag("tvShowCard_ratingRow_${tvShow.id}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Valoración",
                        modifier = Modifier
                            .size(16.dp) // Tamaño del ícono de estrella
                            .testTag("tvShowCard_starIcon_${tvShow.id}"),
                        tint = Color(0xFFFFC107) // Color dorado para la estrella
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", tvShow.voteAverage), // Valoración
                        style = MaterialTheme.typography.bodySmall, // Un poco más pequeño para la valoración
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
