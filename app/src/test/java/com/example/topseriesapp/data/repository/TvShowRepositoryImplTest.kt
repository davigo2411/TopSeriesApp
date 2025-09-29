package com.example.topseriesapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.topseriesapp.BuildConfig
import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.data.database.dao.PopularTvShowDao
import com.example.topseriesapp.data.database.dao.TvShowDetailsDao
import com.example.topseriesapp.data.database.entities.PopularTvShowEntity
import com.example.topseriesapp.data.database.entities.TvShowDetailsEntity
import com.example.topseriesapp.data.model.Genre
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.utils.LocaleHelper
import com.example.topseriesapp.utils.NetworkResponse
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException
import java.util.Locale

@ExperimentalCoroutinesApi
class TestCoroutineDispatchers(
 val scheduler: TestCoroutineScheduler = TestCoroutineScheduler() // Exponer scheduler
) : CoroutineDispatchers {
 private val dispatcher = StandardTestDispatcher(scheduler)
 override val main: CoroutineDispatcher = dispatcher
 override val default: CoroutineDispatcher = dispatcher
 override val io: CoroutineDispatcher = dispatcher
 override val unconfined: CoroutineDispatcher = dispatcher
}

@ExperimentalCoroutinesApi
class TvShowRepositoryImplTest {

 private val apiService: TMDBApiService = mockk()
 private val popularTvShowDao: PopularTvShowDao = mockk(relaxUnitFun = true) // relaxUnitFun para insert/delete
 private val tvShowDetailsDao: TvShowDetailsDao = mockk(relaxUnitFun = true) // relaxUnitFun para insert/update
 private val applicationContext: Context = mockk()
 private val sharedPreferences: SharedPreferences = mockk()
 private val sharedPreferencesEditor: SharedPreferences.Editor = mockk(relaxUnitFun = true)

 private val dispatchers = TestCoroutineDispatchers()

 private lateinit var repository: TvShowRepositoryImpl

 private val testPage = 1
 private val testSeriesId = 123
 private val testApiKey = BuildConfig.TMDB_API_KEY
 private val testLanguage = "en"
 private val testLocale = Locale.forLanguageTag(testLanguage)
 @Before
 fun setUp() {
  // Mockear SharedPreferences
  every { applicationContext.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE) } returns sharedPreferences
  every { sharedPreferences.edit() } returns sharedPreferencesEditor
  every { sharedPreferencesEditor.putBoolean(any(), any()) } returns sharedPreferencesEditor

  // Mockear LocaleHelper
  mockkObject(LocaleHelper) // Mockear el objeto LocaleHelper
  every { LocaleHelper.getCurrentAppLocale(applicationContext) } returns testLocale

  repository = TvShowRepositoryImpl(
   apiService = apiService,
   popularTvShowDao = popularTvShowDao,
   tvShowDetailsDao = tvShowDetailsDao,
   dispatchers = dispatchers,
   applicationContext = applicationContext
  )
 }

 @After
 fun tearDown() {
  unmockkObject(LocaleHelper) // Limpiar el mock del objeto
 }


 // --- Tests para getPopularTvShows ---

 private fun createFakeTvShowList(count: Int = 2): List<TvShow> {
  return List(count) { index ->
   TvShow(
    id = index + 1,
    name = "Show ${index + 1}",
    overview = "Overview ${index + 1}",
    posterPath = "/p${index + 1}.jpg",
    voteAverage = 7.5 + index * 0.1,
    firstAirDate = "2023-01-0${index + 1}",
    backdropPath = "/b${index + 1}.jpg",
    genreIds = listOf(1, 2),
    originalLanguage = "en",
    originalName = "Original Show ${index + 1}",
    popularity = 10.0 + index,
    voteCount = 100 + index * 10,
    originCountry = listOf("US")
   )
  }
 }

 @Test
 fun `getPopularTvShows devuelve Success y datos de API cuando API responde con exito y no hay cache`() = runTest(dispatchers.scheduler) {
  val fakeTvShows = createFakeTvShowList()
  val mockApiResponse = TvShowResponse(page = testPage, results = fakeTvShows, totalPages = 1, totalResults = fakeTvShows.size)

  // Configurar mocks
  every { sharedPreferences.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false) } returns false
  coEvery { popularTvShowDao.getPopularTvShows() } returns flowOf(emptyList()) // Sin caché inicial
  coEvery {
   apiService.getPopularTvShows(apiKey = testApiKey, language = testLanguage, page = testPage)
  } returns Response.success(mockApiResponse)
  coEvery { popularTvShowDao.insertPopularTvShows(any()) } just Runs // Mockear la inserción

  // Ejecutar
  val result = repository.getPopularTvShows(testPage).first() // Tomar el primer valor emitido

  // Verificar
  assertTrue(result is NetworkResponse.Success<TvShowResponse>)
  assertEquals(mockApiResponse, (result as NetworkResponse.Success<TvShowResponse>).data)
  coVerify { popularTvShowDao.insertPopularTvShows(any()) } // Verificar que se insertó en la BD
 }

 @Test
 fun `getPopularTvShows devuelve Success y datos de cache cuando API falla pero hay cache`() = runTest(dispatchers.scheduler) {
  val cachedEntities = listOf(
   PopularTvShowEntity(id = 1, name = "Cached Show 1", apiOrderIndex = 0, overview = "", posterPath = null, backdropPath = null, voteAverage = 0.0, firstAirDate = null, voteCount = 0, genreIds = emptyList(), originCountry = emptyList(), originalLanguage = "", originalName = "", popularity = 0.0),
   PopularTvShowEntity(id = 2, name = "Cached Show 2", apiOrderIndex = 1, overview = "", posterPath = null, backdropPath = null, voteAverage = 0.0, firstAirDate = null, voteCount = 0, genreIds = emptyList(), originCountry = emptyList(), originalLanguage = "", originalName = "", popularity = 0.0)
  )
  val expectedTvShowResponse = TvShowResponse(
   page = 1, // Asume página 1 si no hay info de API
   results = cachedEntities.map { it.toTvShow() },
   totalPages = 1, // Asume 1 total page si no hay info de API
   totalResults = cachedEntities.size
  )

  every { sharedPreferences.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false) } returns false
  coEvery { popularTvShowDao.getPopularTvShows() } returns flowOf(cachedEntities) // Cache disponible
  coEvery {
   apiService.getPopularTvShows(apiKey = testApiKey, language = testLanguage, page = testPage)
  } throws IOException("Network error") // API falla

  val result = repository.getPopularTvShows(testPage).first()
  advanceUntilIdle() // Asegura que todas las corutinas (incluida la de la API) se ejecuten

  assertTrue(result is NetworkResponse.Success<TvShowResponse>)
  assertEquals(expectedTvShowResponse.results.size, (result as NetworkResponse.Success<TvShowResponse>).data?.results?.size)
 }


 @Test
 fun `getPopularTvShows devuelve Error cuando API responde con exito pero body null y no hay cache`() = runTest(dispatchers.scheduler) {
  val httpSuccessCode = 200
  val mockHttpResponse = mockk<Response<TvShowResponse>>()
  every { mockHttpResponse.isSuccessful } returns true
  every { mockHttpResponse.body() } returns null // Cuerpo nulo
  every { mockHttpResponse.code() } returns httpSuccessCode

  every { sharedPreferences.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false) } returns false
  coEvery { popularTvShowDao.getPopularTvShows() } returns flowOf(emptyList())
  coEvery {
   apiService.getPopularTvShows(apiKey = testApiKey, language = testLanguage, page = testPage)
  } returns mockHttpResponse

  val result = repository.getPopularTvShows(testPage).first()

  assertTrue(result is NetworkResponse.Error<TvShowResponse>)
  assertEquals(
   "Respuesta de API vacía (código $httpSuccessCode)",
   (result as NetworkResponse.Error<TvShowResponse>).message
  )
 }

 @Test
 fun `getPopularTvShows devuelve Error cuando API responde con error HTTP y no hay cache`() = runTest(dispatchers.scheduler) {
  val httpErrorCode = 404
  val httpErrorMessage = "Not Found"
  val errorBody = "Resource not found".toResponseBody("text/plain".toMediaTypeOrNull())
  val mockHttpResponse = mockk<Response<TvShowResponse>>()

  every { mockHttpResponse.isSuccessful } returns false
  every { mockHttpResponse.code() } returns httpErrorCode
  every { mockHttpResponse.message() } returns httpErrorMessage
  every { mockHttpResponse.errorBody() } returns errorBody

  every { sharedPreferences.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false) } returns false
  coEvery { popularTvShowDao.getPopularTvShows() } returns flowOf(emptyList())
  coEvery {
   apiService.getPopularTvShows(apiKey = testApiKey, language = testLanguage, page = testPage)
  } returns mockHttpResponse

  val result = repository.getPopularTvShows(testPage).first()

  assertTrue(result is NetworkResponse.Error<TvShowResponse>)
  assertEquals(
   "Error de API: $httpErrorCode $httpErrorMessage",
   (result as NetworkResponse.Error<TvShowResponse>).message
  )
 }

 @Test
 fun `getPopularTvShows devuelve Error cuando ocurre IOException y no hay cache`() = runTest(dispatchers.scheduler) {
  val exceptionMessage = "No hay internet"
  every { sharedPreferences.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false) } returns false
  coEvery { popularTvShowDao.getPopularTvShows() } returns flowOf(emptyList())
  coEvery {
   apiService.getPopularTvShows(apiKey = testApiKey, language = testLanguage, page = testPage)
  } throws IOException(exceptionMessage)

  val result = repository.getPopularTvShows(testPage).first()

  assertTrue(result is NetworkResponse.Error<TvShowResponse>)
  assertEquals(
   "Error de red: $exceptionMessage",
   (result as NetworkResponse.Error<TvShowResponse>).message
  )
 }

 @Test
 fun `getPopularTvShows limpia cache y resetea flag cuando idioma cambia en pagina 1`() = runTest(dispatchers.scheduler) {
  val fakeTvShows = createFakeTvShowList()
  val mockApiResponse = TvShowResponse(page = testPage, results = fakeTvShows, totalPages = 1, totalResults = fakeTvShows.size)
  val booleanSlot = slot<Boolean>()

  every { sharedPreferences.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false) } returns true // Idioma cambió
  every { sharedPreferencesEditor.putBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, capture(booleanSlot)) } returns sharedPreferencesEditor
  coEvery { popularTvShowDao.deleteAllPopularTvShows() } just Runs
  coEvery { popularTvShowDao.getPopularTvShows() } returns flowOf(emptyList()) // Simula que la caché está vacía después de borrar
  coEvery {
   apiService.getPopularTvShows(apiKey = testApiKey, language = testLanguage, page = testPage)
  } returns Response.success(mockApiResponse)
  coEvery { popularTvShowDao.insertPopularTvShows(any()) } just Runs

  // Ejecutar
  repository.getPopularTvShows(testPage).first()

  // Verificar
  coVerify { popularTvShowDao.deleteAllPopularTvShows() }
  assertEquals(false, booleanSlot.captured) // Verificar que la flag se reseteó a false
 }


 // --- Tests para getTvShowDetails ---

 private fun createFullMockTvShowDetails(): TvShowDetails {
  return TvShowDetails(
   id = testSeriesId, name = "Test Show Details", overview = "Detailed overview.",
   posterPath = "/detailPoster.jpg", backdropPath = "/detailBackdrop.jpg", voteAverage = 9.0,
   firstAirDate = "2022-01-01", genres = listOf(Genre(1, "Sci-Fi")), numberOfEpisodes = 10,
   numberOfSeasons = 1, homepage = "http://details.com", status = "Ended",
   lastAirDate = "2022-03-01", tagline = "The very details.", voteCount = 2000,
   adult = false, createdBy = emptyList(), episodeRunTime = listOf(60), inProduction = false,
   languages = listOf("en"), lastEpisodeToAir = null, nextEpisodeToAir = null,
   networks = emptyList(), originCountry = listOf("US"), originalLanguage = "en",
   originalName = "Original Test Details", popularity = 99.0, productionCompanies = emptyList(),
   productionCountries = emptyList(), seasons = emptyList(), spokenLanguages = emptyList(),
   type = "Scripted"
  )
 }

 @Test
 fun `getTvShowDetails devuelve Success y datos de API cuando API responde con exito y no hay cache`() = runTest(dispatchers.scheduler) {
  val mockDetails = createFullMockTvShowDetails()
  coEvery { tvShowDetailsDao.getTvShowDetailsById(testSeriesId) } returns flowOf(null)
  coEvery {
   apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey, language = testLanguage)
  } returns Response.success(mockDetails)
  coEvery { tvShowDetailsDao.insertOrUpdateTvShowDetails(any()) } just Runs

  val result = repository.getTvShowDetails(testSeriesId).first()

  assertTrue(result is NetworkResponse.Success<TvShowDetails>)
  assertEquals(mockDetails, (result as NetworkResponse.Success<TvShowDetails>).data)
  coVerify { tvShowDetailsDao.insertOrUpdateTvShowDetails(any()) }
 }

 @Test
 fun `getTvShowDetails devuelve Success y datos de cache cuando API falla pero hay cache`() = runTest(dispatchers.scheduler) {
  val mockEntity = createFullMockTvShowDetails().toDetailsEntity()
  coEvery { tvShowDetailsDao.getTvShowDetailsById(testSeriesId) } returns flowOf(mockEntity) // Cache disponible
  coEvery {
   apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey, language = testLanguage)
  } throws IOException("Network error for details")

  val result = repository.getTvShowDetails(testSeriesId).first()
  advanceUntilIdle()

  assertTrue(result is NetworkResponse.Success<TvShowDetails>)
  assertEquals(mockEntity.toTvShowDetails().id, (result as NetworkResponse.Success<TvShowDetails>).data?.id)
 }

 @Test
 fun `getTvShowDetails devuelve Error cuando API responde con exito pero body null y no hay cache`() = runTest(dispatchers.scheduler) {
  val httpSuccessCode = 200
  val mockHttpResponse = mockk<Response<TvShowDetails>>()
  every { mockHttpResponse.isSuccessful } returns true
  every { mockHttpResponse.body() } returns null
  every { mockHttpResponse.code() } returns httpSuccessCode

  coEvery { tvShowDetailsDao.getTvShowDetailsById(testSeriesId) } returns flowOf(null)
  coEvery {
   apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey, language = testLanguage)
  } returns mockHttpResponse

  val result = repository.getTvShowDetails(testSeriesId).first()

  assertTrue(result is NetworkResponse.Error<TvShowDetails>)
  assertEquals(
   "Respuesta de API vacía para detalles (código $httpSuccessCode)",
   (result as NetworkResponse.Error<TvShowDetails>).message
  )
 }


 @Test
 fun `getTvShowDetails devuelve Error cuando API responde con error HTTP y no hay cache`() = runTest(dispatchers.scheduler) {
  val httpErrorCode = 401
  val httpErrorMessage = "Unauthorized"
  val errorBody = """{"status_message":"Invalid API key"}""".toResponseBody("application/json".toMediaTypeOrNull())
  val mockHttpResponse = mockk<Response<TvShowDetails>>()
  every { mockHttpResponse.isSuccessful } returns false
  every { mockHttpResponse.code() } returns httpErrorCode
  every { mockHttpResponse.message() } returns httpErrorMessage
  every { mockHttpResponse.errorBody() } returns errorBody

  coEvery { tvShowDetailsDao.getTvShowDetailsById(testSeriesId) } returns flowOf(null)
  coEvery {
   apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey, language = testLanguage)
  } returns mockHttpResponse

  val result = repository.getTvShowDetails(testSeriesId).first()

  assertTrue(result is NetworkResponse.Error<TvShowDetails>)
  assertEquals(
   "Error de API para detalles: $httpErrorCode $httpErrorMessage",
   (result as NetworkResponse.Error<TvShowDetails>).message
  )
 }

 @Test
 fun `getTvShowDetails devuelve Error cuando ocurre IOException y no hay cache`() = runTest(dispatchers.scheduler) {
  val exceptionMessage = "Fallo de conexión para detalles"
  coEvery { tvShowDetailsDao.getTvShowDetailsById(testSeriesId) } returns flowOf(null)
  coEvery {
   apiService.getTvShowDetails(seriesId = testSeriesId, apiKey = testApiKey, language = testLanguage)
  } throws IOException(exceptionMessage)

  val result = repository.getTvShowDetails(testSeriesId).first()

  assertTrue(result is NetworkResponse.Error<TvShowDetails>)
  assertEquals(
   "Error de red para detalles: $exceptionMessage",
   (result as NetworkResponse.Error<TvShowDetails>).message
  )
 }

 // --- Helper para convertir TvShowDetails a TvShowDetailsEntity ---
 private fun TvShowDetails.toDetailsEntity(): TvShowDetailsEntity {
  return TvShowDetailsEntity(
   id = this.id, name = this.name, adult = this.adult, backdropPath = this.backdropPath,
   firstAirDate = this.firstAirDate, homepage = this.homepage, inProduction = this.inProduction,
   lastAirDate = this.lastAirDate, originalLanguage = this.originalLanguage,
   originalName = this.originalName, overview = this.overview, popularity = this.popularity,
   posterPath = this.posterPath, status = this.status, tagline = this.tagline, type = this.type,
   voteAverage = this.voteAverage, voteCount = this.voteCount, numberOfEpisodes = this.numberOfEpisodes,
   numberOfSeasons = this.numberOfSeasons, createdBy = this.createdBy ?: emptyList(),
   episodeRunTime = this.episodeRunTime ?: emptyList(), genres = this.genres ?: emptyList(), languages = this.languages ?: emptyList(),
   networks = this.networks ?: emptyList(), originCountry = this.originCountry ?: emptyList(),
   productionCompanies = this.productionCompanies ?: emptyList(), productionCountries = this.productionCountries ?: emptyList(),
   seasons = this.seasons ?: emptyList(), spokenLanguages = this.spokenLanguages ?: emptyList(),
   lastEpisodeToAir = this.lastEpisodeToAir, nextEpisodeToAir = this.nextEpisodeToAir
  )
 }

 private fun TvShowDetailsEntity.toTvShowDetails(): TvShowDetails {
  // Implementación inversa
  return TvShowDetails(
   id = this.id, name = this.name, adult = this.adult, backdropPath = this.backdropPath,
   firstAirDate = this.firstAirDate, homepage = this.homepage, inProduction = this.inProduction,
   lastAirDate = this.lastAirDate, originalLanguage = this.originalLanguage,
   originalName = this.originalName, overview = this.overview, popularity = this.popularity,
   posterPath = this.posterPath, status = this.status, tagline = this.tagline, type = this.type,
   voteAverage = this.voteAverage, voteCount = this.voteCount, numberOfEpisodes = this.numberOfEpisodes,
   numberOfSeasons = this.numberOfSeasons, createdBy = this.createdBy,
   episodeRunTime = this.episodeRunTime, genres = this.genres, languages = this.languages,
   networks = this.networks, originCountry = this.originCountry,
   productionCompanies = this.productionCompanies, productionCountries = this.productionCountries,
   seasons = this.seasons, spokenLanguages = this.spokenLanguages,
   lastEpisodeToAir = this.lastEpisodeToAir, nextEpisodeToAir = this.nextEpisodeToAir
  )
 }
 private fun PopularTvShowEntity.toTvShow(): TvShow { // Necesitas esta función de mapeo también
  return TvShow(
   id = this.id, name = this.name, overview = this.overview, posterPath = this.posterPath,
   backdropPath = this.backdropPath, voteAverage = this.voteAverage, firstAirDate = this.firstAirDate,
   voteCount = this.voteCount, genreIds = this.genreIds, originCountry = this.originCountry,
   originalLanguage = this.originalLanguage, originalName = this.originalName, popularity = this.popularity
  )
 }
}
