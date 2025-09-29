package com.example.topseriesapp.ui.showsdetails

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.example.topseriesapp.data.model.CreatedBy
import com.example.topseriesapp.data.model.EpisodeToAir
import com.example.topseriesapp.data.model.Genre
import com.example.topseriesapp.data.model.Network
import com.example.topseriesapp.data.model.ProductionCompany
import com.example.topseriesapp.data.model.ProductionCountry
import com.example.topseriesapp.data.model.Season
import com.example.topseriesapp.data.model.SpokenLanguage
import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCase
import com.example.topseriesapp.utils.NetworkResponse
import com.example.topseriesapp.utils.SERIES_ID_KEY
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class TvShowDetailsViewModelTest {

 @get:Rule
 val instantTaskExecutorRule = InstantTaskExecutorRule()

 private val testDispatcher = StandardTestDispatcher()
 private val getTvShowDetailsUseCase: GetTvShowDetailsUseCase = mockk()
 private val connectivityChecker: ConnectivityChecker = mockk()
 private lateinit var savedStateHandle: SavedStateHandle
 private lateinit var viewModel: TvShowDetailsViewModel

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
  lastEpisodeToAir = EpisodeToAir(
   id = 349232,
   name = "Felina",
   overview = "The final episode of Breaking Bad",
   voteAverage = 9.9,
   voteCount = 500,
   airDate = "2013-09-29",
   episodeNumber = 16,
   productionCode = "316",
   runtime = 55,
   seasonNumber = 5,
   showId = 1,
   stillPath = "/felina.jpg"
  ),
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

 private val seriesId = 123

 @Before
 fun setUp() {
  Dispatchers.setMain(testDispatcher)
 }

 @After
 fun tearDown() {
  Dispatchers.resetMain()
 }

 @Test
 fun `deberia cargar detalles exitosamente con series id valido`() = runTest {
  // Given
  savedStateHandle = SavedStateHandle(mapOf(SERIES_ID_KEY to seriesId))
  coEvery { connectivityChecker.isOnline() } returns true
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Success(mockTvShowDetails))

  // When
  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)
  advanceUntilIdle()

  // Then
  val state = viewModel.uiState.first()
  assertTrue(state is DetailsScreenUiState.Success)
  val successState = state as DetailsScreenUiState.Success
  assertEquals(mockTvShowDetails, successState.tvShowDetails)
 }

 @Test
 fun `deberia mostrar error cuando no se proporciona series id`() = runTest {
  // Given - SavedStateHandle sin SERIES_ID_KEY
  savedStateHandle = SavedStateHandle()

  // When
  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)
  advanceUntilIdle()

  // Then
  val state = viewModel.uiState.first()
  assertTrue(state is DetailsScreenUiState.Error)
  val errorState = state as DetailsScreenUiState.Error
  assertEquals("ID de serie no proporcionado.", errorState.message)
  assertFalse(errorState.isOfflineAndNoData)
 }

 @Test
 fun `deberia mostrar error cuando el use case retorna error con conexion`() = runTest {
  // Given
  val errorMessage = "Error de red al obtener detalles"
  savedStateHandle = SavedStateHandle(mapOf(SERIES_ID_KEY to seriesId))
  coEvery { connectivityChecker.isOnline() } returns true
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Error(errorMessage))

  // When
  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)
  advanceUntilIdle()

  // Then
  val state = viewModel.uiState.first()
  assertTrue(state is DetailsScreenUiState.Error)
  val errorState = state as DetailsScreenUiState.Error
  assertEquals(errorMessage, errorState.message)
  assertFalse(errorState.isOfflineAndNoData)
 }

 @Test
 fun `deberia mostrar error offline cuando no hay conexion y error de red`() = runTest {
  // Given
  val networkErrorMessage = "No host found"
  savedStateHandle = SavedStateHandle(mapOf(SERIES_ID_KEY to seriesId))
  coEvery { connectivityChecker.isOnline() } returns false
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Error(networkErrorMessage))

  // When
  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)
  advanceUntilIdle()

  // Then
  val state = viewModel.uiState.first()
  assertTrue(state is DetailsScreenUiState.Error)
  val errorState = state as DetailsScreenUiState.Error
  assertEquals("Detalles no disponibles sin conexión.", errorState.message)
  assertTrue(errorState.isOfflineAndNoData)
 }

 @Test
 fun `deberia reintentar y cargar exitosamente despues de error`() = runTest {
  // Given - inicialmente error
  savedStateHandle = SavedStateHandle(mapOf(SERIES_ID_KEY to seriesId))
  coEvery { connectivityChecker.isOnline() } returns true
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Error("Error inicial"))

  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)
  advanceUntilIdle()

  // Verificar estado de error inicial
  val currentState = viewModel.uiState.first()
  assertTrue(currentState is DetailsScreenUiState.Error)

  // Given - respuesta exitosa para el reintento
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Success(mockTvShowDetails))

  // When
  viewModel.retry()
  advanceUntilIdle()

  // Then
  val finalState = viewModel.uiState.first()
  assertTrue(finalState is DetailsScreenUiState.Success)
  val successState = finalState as DetailsScreenUiState.Success
  assertEquals(mockTvShowDetails, successState.tvShowDetails)
 }

 @Test
 fun `deberia pasar por estado loading durante la carga inicial`() = runTest {
  // Given
  savedStateHandle = SavedStateHandle(mapOf(SERIES_ID_KEY to seriesId))
  coEvery { connectivityChecker.isOnline() } returns true
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Success(mockTvShowDetails))

  // When - crear viewModel pero no avanzar corrutinas
  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)

  // Then - verificar que inicialmente está en loading
  val initialState = viewModel.uiState.first()
  assertTrue(initialState is DetailsScreenUiState.Loading)

  // When - completar la operación
  advanceUntilIdle()

  // Then - verificar que cambió a success
  val finalState = viewModel.uiState.first()
  assertTrue(finalState is DetailsScreenUiState.Success)
 }

 @Test
 fun `deberia manejar datos nulos del use case`() = runTest {
  // Given
  savedStateHandle = SavedStateHandle(mapOf(SERIES_ID_KEY to seriesId))
  coEvery { connectivityChecker.isOnline() } returns true
  coEvery { getTvShowDetailsUseCase(seriesId) } returns flowOf(NetworkResponse.Error("Los datos de los detalles están vacíos."))  // When
  viewModel = TvShowDetailsViewModel(getTvShowDetailsUseCase, savedStateHandle, connectivityChecker)
  advanceUntilIdle()

  // Then
  val state = viewModel.uiState.first()
  assertTrue(state is DetailsScreenUiState.Error)
  val errorState = state as DetailsScreenUiState.Error
  assertEquals("Los datos de los detalles están vacíos.", errorState.message)
  assertFalse(errorState.isOfflineAndNoData)
 }
}