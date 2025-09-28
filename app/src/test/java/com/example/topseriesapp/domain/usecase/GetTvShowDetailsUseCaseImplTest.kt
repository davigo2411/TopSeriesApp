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
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetTvShowDetailsUseCaseImplTest {

 private lateinit var mockTvShowRepository: TvShowRepository
 private lateinit var getTvShowDetailsUseCase: GetTvShowDetailsUseCaseImpl

 @Before
 fun setUp() {
  mockTvShowRepository = mockk()
  getTvShowDetailsUseCase = GetTvShowDetailsUseCaseImpl(mockTvShowRepository)
 }

 @Test
 fun `invoke DEBERIA llamar a getTvShowDetails del repositorio Y devolver Success CUANDO repositorio devuelve Success`() = runTest {
  val seriesId = 123
  val fakeTvShowDetails = TvShowDetails(
   id = seriesId,
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
    id = 101,
    name = "The Last Test",
    overview = "The final test episode.",
    voteAverage = 9.0,
    voteCount = 50,
    airDate = "2024-03-01",
    episodeNumber = 10,
    productionCode = "S02E10",
    runtime = 45,
    seasonNumber = 2,
    showId = seriesId,
    stillPath = "/lastEpisode.jpg"
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
  val expectedResponse = NetworkResponse.Success(fakeTvShowDetails)

  coEvery { mockTvShowRepository.getTvShowDetails(seriesId) } returns expectedResponse

  val actualResponse = getTvShowDetailsUseCase(seriesId)

  coVerify(exactly = 1) { mockTvShowRepository.getTvShowDetails(seriesId) }
  assertEquals(expectedResponse, actualResponse)
 }

 @Test
 fun `invoke DEBERIA llamar a getTvShowDetails del repositorio Y devolver Error CUANDO repositorio devuelve Error`() = runTest {
  val seriesId = 456
  val errorMessage = "Error de red desde el repositorio"
  val expectedResponse = NetworkResponse.Error<TvShowDetails>(errorMessage)

  coEvery { mockTvShowRepository.getTvShowDetails(seriesId) } returns expectedResponse

  val actualResponse = getTvShowDetailsUseCase(seriesId)

  coVerify(exactly = 1) { mockTvShowRepository.getTvShowDetails(seriesId) }
  assertEquals(expectedResponse, actualResponse)
  assertTrue(actualResponse is NetworkResponse.Error)
  assertEquals(errorMessage, (actualResponse as NetworkResponse.Error).message)
 }
}
