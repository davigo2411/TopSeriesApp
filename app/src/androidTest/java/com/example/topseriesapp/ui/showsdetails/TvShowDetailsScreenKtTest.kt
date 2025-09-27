package com.example.topseriesapp.ui.showsdetails

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.topseriesapp.data.model.*
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TvShowDetailsScreenTest {

 @get:Rule
 val composeTestRule = createComposeRule()

 private val mockNavController: NavController = mockk(relaxed = true)

 // Mock data
 private val mockTvShowDetails = TvShowDetails(
  id = 1,
  name = "Breaking Bad",
  overview = "A high school chemistry teacher turned methamphetamine manufacturer",
  posterPath = "/poster.jpg",
  backdropPath = "/backdrop.jpg",
  firstAirDate = "2008-01-20",
  lastAirDate = "2013-09-29",
  numberOfSeasons = 5,
  numberOfEpisodes = 62,
  voteAverage = 9.5,
  voteCount = 15000,
  genres = listOf(
   Genre(id = 18, name = "Drama"),
   Genre(id = 80, name = "Crime")
  ),
  status = "Ended",
  tagline = "Remember my name",
  homepage = "https://breakingbad.com",
  adult = false,
  createdBy = listOf(
   CreatedBy(
    id = 1,
    creditId = "52542282760ee313280017f9",
    name = "Vince Gilligan",
    gender = 2,
    profilePath = "/vince.jpg"
   )
  ),
  episodeRunTime = listOf(45, 47),
  inProduction = false,
  languages = listOf("en"),
  lastEpisodeToAir = null,
  nextEpisodeToAir = null,
  networks = listOf(
   Network(
    id = 174,
    logoPath = "/amc.png",
    name = "AMC",
    originCountry = "US"
   )
  ),
  originCountry = listOf("US"),
  originalLanguage = "en",
  originalName = "Breaking Bad",
  popularity = 150.0,
  productionCompanies = listOf(
   ProductionCompany(
    id = 11073,
    logoPath = "/sony.png",
    name = "Sony Pictures Television",
    originCountry = "US"
   )
  ),
  productionCountries = listOf(
   ProductionCountry(
    iso31661 = "US",
    name = "United States of America"
   )
  ),
  seasons = listOf(
   Season(
    airDate = "2008-01-20",
    episodeCount = 7,
    id = 3572,
    name = "Season 1",
    overview = "Walter White begins his transformation",
    posterPath = "/season1.jpg",
    seasonNumber = 1,
    voteAverage = 8.2
   ),
   Season(
    airDate = "2009-03-08",
    episodeCount = 13,
    id = 3573,
    name = "Season 2",
    overview = "The consequences unfold",
    posterPath = "/season2.jpg",
    seasonNumber = 2,
    voteAverage = 8.5
   )
  ),
  spokenLanguages = listOf(
   SpokenLanguage(
    englishName = "English",
    iso6391 = "en",
    name = "English"
   )
  ),
  type = "Scripted"
 )

 @Test
 fun deberia_mostrar_loading_inicial() {
  // Given
  val viewModel = createMockViewModel(DetailsScreenUiState.Loading)

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then - Solo verificar que hay un indicador de progreso
  composeTestRule
   .onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate))
   .assertIsDisplayed()
 }

 @Test
 fun deberia_mostrar_error_con_boton_retry() {
  // Given
  val errorMessage = "Error al cargar detalles"
  val viewModel = createMockViewModel(DetailsScreenUiState.Error(errorMessage))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then
  composeTestRule
   .onNodeWithText("Error: $errorMessage")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Retry")
   .assertIsDisplayed()
 }

 @Test
 fun deberia_mostrar_detalles_de_la_serie() {
  // Given
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(mockTvShowDetails))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then
  composeTestRule
   .onAllNodesWithText("Breaking Bad")
   .onFirst()
   .assertIsDisplayed()

  // Verificar información básica
  composeTestRule
   .onNodeWithText("⭐ 9.5 (15000 votos)")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Estado: Ended")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Inicio: 2008-01-20")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Temporadas: 5")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Episodios: 62")
   .assertIsDisplayed()
 }

 @Test
 fun deberia_mostrar_sinopsis() {
  // Given
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(mockTvShowDetails))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then
  composeTestRule
   .onNodeWithText("Sinopsis")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("A high school chemistry teacher turned methamphetamine manufacturer")
   .assertIsDisplayed()
 }

 @Test
 fun deberia_navegar_hacia_atras_al_presionar_boton() {
  // Given
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(mockTvShowDetails))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then
  composeTestRule
   .onNodeWithContentDescription("Back")
   .performClick()

  // Verificar que se llamó popBackStack
  verify { mockNavController.popBackStack() }
 }

 @Test
 fun deberia_mostrar_generos() {
  // Given
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(mockTvShowDetails))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then
  composeTestRule
   .onNodeWithText("Drama")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Crime")
   .assertIsDisplayed()
 }

 @Test
 fun deberia_mostrar_creadores() {
  // Given
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(mockTvShowDetails))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then
  composeTestRule
   .onNodeWithText("Creadores")
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Vince Gilligan")
   .assertIsDisplayed()
 }

 @Test
 fun deberia_mostrar_temporadas() {
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(mockTvShowDetails))

  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  composeTestRule
   .onNodeWithText(text = "Temporadas", useUnmergedTree = true)
   .assertIsDisplayed()

  val season1Node = composeTestRule.onNodeWithText("Season 1")
  season1Node.performScrollTo()
  season1Node.assertIsDisplayed()

  val season2Node = composeTestRule.onNodeWithText("Season 2")
  season2Node.performScrollTo()
  season2Node.assertIsDisplayed()

  val episodes7Node = composeTestRule.onNodeWithText("Episodios: 7")
  episodes7Node.performScrollTo()
  episodes7Node.assertIsDisplayed()

  val episodes13Node = composeTestRule.onNodeWithText("Episodios: 13")
  episodes13Node.performScrollTo()
  episodes13Node.assertIsDisplayed()
 }

 @Test
 fun deberia_manejar_listas_vacias_correctamente() {
  // Given
  val showWithEmptyLists = mockTvShowDetails.copy(
   genres = emptyList(),
   createdBy = emptyList(),
   seasons = emptyList()
  )
  val viewModel = createMockViewModel(DetailsScreenUiState.Success(showWithEmptyLists))

  // When
  composeTestRule.setContent {
   TopSeriesAppTheme {
    TvShowDetailsScreen(
     navController = mockNavController,
     viewModel = viewModel
    )
   }
  }

  // Then - Verificar que no se muestran secciones vacías
  composeTestRule
   .onNodeWithText("Creadores")
   .assertDoesNotExist()

  composeTestRule
   .onNodeWithText("Temporadas")
   .assertDoesNotExist()

  // Pero sí se muestra el título y sinopsis
  composeTestRule
   .onAllNodesWithText("Breaking Bad")
   .onFirst()
   .assertIsDisplayed()

  composeTestRule
   .onNodeWithText("Sinopsis")
   .assertIsDisplayed()
 }

 // Helper function para crear mock del ViewModel
 private fun createMockViewModel(uiState: DetailsScreenUiState): TvShowDetailsViewModel {
  val mockViewModel = mockk<TvShowDetailsViewModel>(relaxed = true)
  val stateFlow = MutableStateFlow(uiState)

  io.mockk.every { mockViewModel.uiState } returns stateFlow as StateFlow<DetailsScreenUiState>

  return mockViewModel
 }
}