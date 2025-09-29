package com.example.topseriesapp.domain.usecase

import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.Genre
import com.example.topseriesapp.data.model.CreatedBy
import com.example.topseriesapp.data.model.EpisodeToAir
import com.example.topseriesapp.data.model.Network
import com.example.topseriesapp.data.model.ProductionCompany
import com.example.topseriesapp.data.model.ProductionCountry
import com.example.topseriesapp.data.model.Season
import com.example.topseriesapp.data.model.SpokenLanguage
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.utils.NetworkResponse
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTvShowDetailsUseCaseImplTest {

 private lateinit var mockTvShowRepository: TvShowRepository
 private lateinit var getTvShowDetailsUseCase: GetTvShowDetailsUseCaseImpl

 private val testSeriesIdConstant = 123 // ID constante para los datos falsos

 @Before
 fun setUp() {
  mockTvShowRepository = mockk()
  getTvShowDetailsUseCase = GetTvShowDetailsUseCaseImpl(mockTvShowRepository)
 }

 // Función helper para crear un objeto TvShowDetails falso para los tests.
 private fun createFakeTvShowDetails(): TvShowDetails {
  return TvShowDetails(
   id = testSeriesIdConstant,
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
    seasonNumber = 2, showId = testSeriesIdConstant, stillPath = "/lastEpisode.jpg"
   ),
   nextEpisodeToAir = null,
   networks = listOf(Network(id = 1, logoPath = "/networklogo.png", name = "Test Network", originCountry = "US")),
   originCountry = listOf("US", "CA"),
   originalLanguage = "en",
   originalName = "Original Test Show Name",
   popularity = 75.5,
   productionCompanies = listOf(ProductionCompany(id = 1, logoPath = "/companylogo.png", name = "Test Production Co", originCountry = "US")),
   productionCountries = listOf(ProductionCountry(iso31661 = "US", name = "United States of America")),
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

 @Test
 fun `invoke DEBERIA llamar a getTvShowDetails del repositorio Y devolver Flow con Success CUANDO repositorio devuelve Flow con Success`() = runTest {
  val fakeTvShowDetails = createFakeTvShowDetails()
  val expectedResponseFromRepo = NetworkResponse.Success(fakeTvShowDetails)

  // Configura el mock del repositorio para que devuelva un Flow con la respuesta esperada.
  every { mockTvShowRepository.getTvShowDetails(testSeriesIdConstant) } returns flowOf(expectedResponseFromRepo)

  // Ejecuta el caso de uso y obtiene el primer valor emitido por el Flow.
  val actualResponseValue = getTvShowDetailsUseCase(testSeriesIdConstant).first()

  // Verifica que el metodo del repositorio fue llamado.
  coVerify(exactly = 1) { mockTvShowRepository.getTvShowDetails(testSeriesIdConstant) }

  // Verifica que la respuesta es de tipo Success y contiene los datos correctos.
  assertTrue(actualResponseValue is NetworkResponse.Success<*>)
  assertEquals(fakeTvShowDetails, (actualResponseValue as NetworkResponse.Success<TvShowDetails>).data)
  assertEquals(expectedResponseFromRepo, actualResponseValue)
 }

 @Test
 fun `invoke DEBERIA llamar a getTvShowDetails del repositorio Y devolver Flow con Error CUANDO repositorio devuelve Flow con Error`() = runTest {
  val seriesIdForErrorTest = 456
  val errorMessage = "Error de red desde el repositorio"
  val expectedResponseFromRepo = NetworkResponse.Error<TvShowDetails>(errorMessage)

  // Configura el mock del repositorio para que devuelva un Flow con un error.
  every { mockTvShowRepository.getTvShowDetails(seriesIdForErrorTest) } returns flowOf(expectedResponseFromRepo)

  // Ejecuta el caso de uso y obtiene el primer valor emitido por el Flow.
  val actualResponseValue = getTvShowDetailsUseCase(seriesIdForErrorTest).first()

  // Verifica que el metodo del repositorio fue llamado.
  coVerify(exactly = 1) { mockTvShowRepository.getTvShowDetails(seriesIdForErrorTest) }

  // Verifica que la respuesta es de tipo Error y contiene el mensaje correcto.
  assertTrue(actualResponseValue is NetworkResponse.Error<*>)
  assertEquals(errorMessage, (actualResponseValue as NetworkResponse.Error<TvShowDetails>).message)
  assertEquals(expectedResponseFromRepo, actualResponseValue)
 }
}

