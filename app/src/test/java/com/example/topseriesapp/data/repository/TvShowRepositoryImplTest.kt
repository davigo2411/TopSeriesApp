package com.example.topseriesapp.data.repository

import com.example.topseriesapp.BuildConfig // Asegúrate de tener esta importación
import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.data.model.CreatedBy
import com.example.topseriesapp.data.model.EpisodeToAir
import com.example.topseriesapp.data.model.Genre
import com.example.topseriesapp.data.model.Network
import com.example.topseriesapp.data.model.ProductionCompany
import com.example.topseriesapp.data.model.ProductionCountry
import com.example.topseriesapp.data.model.Season
import com.example.topseriesapp.data.model.SpokenLanguage
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.utils.NetworkResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull // Importante para errorBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

// Tu TestCoroutineDispatchers es correcta
class TestCoroutineDispatchers(
 scheduler: TestCoroutineScheduler = TestCoroutineScheduler()
) : CoroutineDispatchers {
 private val dispatcher = StandardTestDispatcher(scheduler)
 override val main: CoroutineDispatcher = dispatcher
 override val default: CoroutineDispatcher = dispatcher
 override val io: CoroutineDispatcher = dispatcher
 override val unconfined: CoroutineDispatcher = dispatcher
}

class TvShowRepositoryImplTest {

 private val apiService: TMDBApiService = mockk()
 private val scheduler = TestCoroutineScheduler() // Scheduler para el dispatcher de test
 private val dispatchers = TestCoroutineDispatchers(scheduler) // Usar el scheduler

 private lateinit var repository: TvShowRepositoryImpl

 // Constantes para los tests
 private val testPage = 1
 private val testSeriesId = 123
 private val testApiKey = BuildConfig.TMDB_API_KEY // Usa la clave real o mockea BuildConfig

 @Before
 fun setUp() {
  repository = TvShowRepositoryImpl(apiService, dispatchers)
 }

 // --- Tests para getPopularTvShows ---

 @Test
 fun `getPopularTvShows devuelve Success cuando API responde con body valido`() = runTest(scheduler) { // runTest con scheduler
  val fakeTvShowItems = listOf( // Crea algunos items si results no debe ser vacío
   TvShow(
       id = 1,
       name = "Show 1",
       overview = "Overview 1",
       posterPath = "/p1.jpg",
       voteAverage = 7.5,
       firstAirDate = "2023-01-01",
       backdropPath = null,
       genreIds = emptyList(),
       originalLanguage = "en",
       originalName = "Show 1 O",
       popularity = 10.0,
       voteCount = 100,
       originCountry = listOf("US")
   ),
   TvShow(
       id = 2,
       name = "Show 2",
       overview = "Overview 2",
       posterPath = "/p2.jpg",
       voteAverage = 8.0,
       firstAirDate = "2023-02-01",
       backdropPath = null,
       genreIds = emptyList(),
       originalLanguage = "en",
       originalName = "Show 2 O",
       popularity = 12.0,
       voteCount = 120,
       originCountry = listOf("US")
   )
  )
  val mockApiResponse = TvShowResponse(page = testPage, results = fakeTvShowItems, totalPages = 1, totalResults = fakeTvShowItems.size)
  // Sé específico con los parámetros
  coEvery { apiService.getPopularTvShows(apiKey = testApiKey, page = testPage) } returns Response.success(mockApiResponse)

  val result = repository.getPopularTvShows(testPage)

  assertTrue(result is NetworkResponse.Success)
  assertEquals(mockApiResponse, (result as NetworkResponse.Success).data)
 }

 @Test
 fun `getPopularTvShows devuelve Error cuando API responde con exito pero body null`() = runTest(scheduler) {
  val httpSuccessCode = 200
  val mockHttpResponse = mockk<Response<TvShowResponse>>()
  every { mockHttpResponse.isSuccessful } returns true
  every { mockHttpResponse.body() } returns null
  every { mockHttpResponse.code() } returns httpSuccessCode
  every { mockHttpResponse.message() } returns "OK" // Mensaje HTTP

  coEvery { apiService.getPopularTvShows(apiKey = testApiKey, page = testPage) } returns mockHttpResponse

  val result = repository.getPopularTvShows(testPage)

  assertTrue(result is NetworkResponse.Error)
  // Comprueba el mensaje exacto según tu lógica en TvShowRepositoryImpl
  assertEquals(
   "La respuesta de la API está vacía pero fue exitosa (código $httpSuccessCode).",
   (result as NetworkResponse.Error).message
  )
 }

 @Test
 fun `getPopularTvShows devuelve Error cuando API responde con error HTTP`() = runTest(scheduler) {
  val httpErrorCode = 404
  val httpErrorMessage = "Not Found"
  // Tu repo para getPopularTvShows no usa el errorBody en el mensaje, así que uno simple es suficiente
  val errorBody = "Resource not found".toResponseBody("text/plain".toMediaTypeOrNull())

  val mockHttpResponse = mockk<Response<TvShowResponse>>()
  every { mockHttpResponse.isSuccessful } returns false
  every { mockHttpResponse.code() } returns httpErrorCode
  every { mockHttpResponse.message() } returns httpErrorMessage
  every { mockHttpResponse.errorBody() } returns errorBody // Aunque no se use en el mensaje, es bueno mockearlo

  coEvery { apiService.getPopularTvShows(apiKey = testApiKey, page = testPage) } returns mockHttpResponse

  val result = repository.getPopularTvShows(testPage)

  assertTrue(result is NetworkResponse.Error)
  // Comprueba el mensaje exacto
  assertEquals(
   "Error de API: $httpErrorCode $httpErrorMessage",
   (result as NetworkResponse.Error).message
  )
 }

 @Test
 fun `getPopularTvShows devuelve Error cuando ocurre IOException`() = runTest(scheduler) {
  val exceptionMessage = "No hay internet"
  coEvery { apiService.getPopularTvShows(apiKey = testApiKey, page = testPage) } throws IOException(exceptionMessage)

  val result = repository.getPopularTvShows(testPage)

  assertTrue(result is NetworkResponse.Error)
  assertEquals(
   "Error de red: $exceptionMessage",
   (result as NetworkResponse.Error).message
  )
 }

 @Test
 fun `getPopularTvShows devuelve Error cuando ocurre Exception generica`() = runTest(scheduler) {
  val exceptionMessage = "Error genérico de servidor"
  coEvery { apiService.getPopularTvShows(apiKey = testApiKey, page = testPage) } throws RuntimeException(exceptionMessage)

  val result = repository.getPopularTvShows(testPage)

  assertTrue(result is NetworkResponse.Error)
  assertEquals(
   "Error inesperado: $exceptionMessage",
   (result as NetworkResponse.Error).message
  )
 }

 // --- Tests para getTvShowDetails ---

 // Tu test original de éxito para getTvShowDetails ya es bastante bueno y completo
 @Test
 fun `getTvShowDetails devuelve Success cuando API responde con body valido`() = runTest(scheduler) {
  val mockDetails = createFullMockTvShowDetails() // Usar una función helper
  coEvery { apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey) } returns Response.success(mockDetails)

  val result = repository.getTvShowDetails(testSeriesId)

  assertTrue(result is NetworkResponse.Success)
  assertEquals(mockDetails, (result as NetworkResponse.Success).data)
 }

 @Test
 fun `getTvShowDetails devuelve Error cuando API responde con exito pero body null`() = runTest(scheduler) {
  val httpSuccessCode = 200
  val mockHttpResponse = mockk<Response<TvShowDetails>>()
  every { mockHttpResponse.isSuccessful } returns true
  every { mockHttpResponse.body() } returns null
  every { mockHttpResponse.code() } returns httpSuccessCode
  every { mockHttpResponse.message() } returns "OK"

  coEvery { apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey) } returns mockHttpResponse

  val result = repository.getTvShowDetails(testSeriesId)

  assertTrue(result is NetworkResponse.Error)
  assertEquals(
   "La respuesta de la API para detalles está vacía pero fue exitosa (código $httpSuccessCode).",
   (result as NetworkResponse.Error).message
  )
 }

 @Test
 fun `getTvShowDetails devuelve Error cuando API responde con error HTTP`() = runTest(scheduler) {
  val httpErrorCode = 401
  val httpErrorMessage = "Unauthorized"
  val errorJson = """{"status_code":7,"status_message":"Invalid API key"}"""
  val errorBody = errorJson.toResponseBody("application/json".toMediaTypeOrNull())

  val mockHttpResponse = mockk<Response<TvShowDetails>>()
  every { mockHttpResponse.isSuccessful } returns false
  every { mockHttpResponse.code() } returns httpErrorCode
  every { mockHttpResponse.message() } returns httpErrorMessage
  every { mockHttpResponse.errorBody() } returns errorBody

  coEvery { apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey) } returns mockHttpResponse

  val result = repository.getTvShowDetails(testSeriesId)

  assertTrue(result is NetworkResponse.Error)
  assertEquals(
   "Error de API al obtener detalles: $httpErrorCode $httpErrorMessage. Cuerpo: $errorJson",
   (result as NetworkResponse.Error).message
  )
 }

 @Test
 fun `getTvShowDetails devuelve Error cuando ocurre IOException`() = runTest(scheduler) {
  val exceptionMessage = "Fallo de conexión"
  coEvery { apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey) } throws IOException(exceptionMessage)

  val result = repository.getTvShowDetails(testSeriesId)

  assertTrue(result is NetworkResponse.Error)
  assertEquals(
   "Error de red al obtener detalles: $exceptionMessage",
   (result as NetworkResponse.Error).message
  )
 }

 @Test
 fun `getTvShowDetails devuelve Error cuando ocurre Exception generica`() = runTest(scheduler) {
  val exceptionMessage = "Error inesperado del servidor"
  coEvery { apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey) } throws RuntimeException(exceptionMessage)

  val result = repository.getTvShowDetails(testSeriesId)

  assertTrue(result is NetworkResponse.Error)
  assertEquals(
   "Error inesperado al obtener detalles: $exceptionMessage",
   (result as NetworkResponse.Error).message
  )
 }

 // Helper para crear un TvShowDetails completo, similar a tu test original
 private fun createFullMockTvShowDetails(): TvShowDetails {
  return TvShowDetails(
   id = 123,
   name = "Test Show",
   overview = "Test Overview in English",
   posterPath = "/testPoster.jpg",
   backdropPath = "/testBackdrop.jpg",
   voteAverage = 8.5,
   firstAirDate = "2023-01-01",
   genres = listOf(Genre(1, "Action"), Genre(2, "Drama")),
   numberOfEpisodes = 20,
   numberOfSeasons = 2,
   homepage = "http://testshow.com",
   status = "Returning Series",
   lastAirDate = "2024-03-01",
   tagline = "An epic test tagline.",
   voteCount = 1500,
   adult = false,
   createdBy = listOf(
    CreatedBy(id = 1, creditId = "credit001", name = "Creator One", gender = 1, profilePath = "/creator1.jpg")
   ),
   episodeRunTime = listOf(45, 42, 44),
   inProduction = true,
   languages = listOf("en", "es"),
   lastEpisodeToAir = EpisodeToAir(
    id = 101, name = "The Last Test", overview = "The final test episode.",
    voteAverage = 9.0, voteCount = 50, airDate = "2024-03-01",
    episodeNumber = 10, productionCode = "S02E10", runtime = 45,
    seasonNumber = 2, showId = 123, stillPath = "/lastEpisode.jpg"
   ),
   nextEpisodeToAir = null,
   networks = listOf(
    Network(id = 1, logoPath = "/networklogo.png", name = "Test Network", originCountry = "US")
   ),
   originCountry = listOf("US", "CA"),
   originalLanguage = "en",
   originalName = "Original Test Show Name",
   popularity = 75.5,
   productionCompanies = listOf(
    ProductionCompany(id = 1, logoPath = "/companylogo.png", name = "Test Production Co", originCountry = "US")
   ),
   productionCountries = listOf(
    ProductionCountry(iso31661 = "US", name = "United States of America")
   ),
   seasons = listOf(
    Season(airDate = "2023-01-01", episodeCount = 10, id = 1, name = "Season 1", overview = "First season overview.", posterPath = "/season1.jpg", seasonNumber = 1, voteAverage = 8.2),
    Season(airDate = "2024-01-01", episodeCount = 10, id = 2, name = "Season 2", overview = "Second season overview.", posterPath = "/season2.jpg", seasonNumber = 2, voteAverage = 8.8)
   ),
   spokenLanguages = listOf(
    SpokenLanguage(englishName = "English", iso6391 = "en", name = "English"),
    SpokenLanguage(englishName = "Spanish", iso6391 = "es", name = "Español")
   ),
   type = "Scripted"
  )
 }
}
