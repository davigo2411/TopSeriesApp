package com.example.topseriesapp.ui.popularshows

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.domain.model.PopularTvShowsResult
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.utils.NetworkResponse
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
class PopularTvShowsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val getPopularTvShowsUseCase: GetPopularTvShowsUseCase = mockk()
    private lateinit var viewModel: PopularTvShowsViewModel

    // Mock data
    private val mockTvShow1 = TvShow(
        id = 1,
        name = "Test Show 1",
        overview = "Test overview 1",
        posterPath = "/test1.jpg",
        backdropPath = "/backdrop1.jpg",
        firstAirDate = "2023-01-01",
        voteAverage = 8.5,
        voteCount = 1000,
        genreIds = listOf(18, 35),
        originCountry = listOf("US"),
        originalLanguage = "en",
        originalName = "Test Show Original Name",
        popularity = 150.0
    )

    private val mockTvShow2 = TvShow(
        id = 2,
        name = "Test Show 2",
        overview = "Test overview 2",
        posterPath = "/test2.jpg",
        backdropPath = "/backdrop2.jpg",
        firstAirDate = "2023-02-01",
        voteAverage = 7.8,
        voteCount = 800,
        genreIds = listOf(18, 80),
        originCountry = listOf("UK"),
        originalLanguage = "en",
        originalName = "Test Show 2 Original Name",
        popularity = 120.0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deberia cargar series exitosamente al inicializar`() = runTest {
        // Given
        coEvery { getPopularTvShowsUseCase(1) } returns NetworkResponse.Success(
            PopularTvShowsResult(
                currentPage = 1,
                totalPages = 5,
                tvShows = listOf(mockTvShow1, mockTvShow2)
            )
        )

        // When
        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(2, state.tvShows.size)
        assertEquals(mockTvShow1, state.tvShows[0])
        assertEquals(mockTvShow2, state.tvShows[1])
        assertEquals(1, state.currentPage)
        assertEquals(5, state.totalPages)
        assertNull(state.error)
        assertTrue(state.canLoadMore)
    }

    @Test
    fun `deberia manejar errores de red correctamente`() = runTest {
        // Given
        val errorMessage = "Error de conexión"
        coEvery { getPopularTvShowsUseCase(1) } returns NetworkResponse.Error(errorMessage)

        // When
        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertTrue(state.tvShows.isEmpty())
        assertEquals(errorMessage, state.error)
        assertEquals(1, state.currentPage)
        assertEquals(0, state.totalPages)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `deberia cargar siguiente pagina y agregar series`() = runTest {
        // Given
        val firstPageResponse = PopularTvShowsResult(
            currentPage = 1,
            totalPages = 5,
            tvShows = listOf(mockTvShow1)
        )
        val secondPageResponse = PopularTvShowsResult(
            currentPage = 2,
            totalPages = 5,
            tvShows = listOf(mockTvShow2)
        )

        coEvery { getPopularTvShowsUseCase(1) } returns NetworkResponse.Success(firstPageResponse)
        coEvery { getPopularTvShowsUseCase(2) } returns NetworkResponse.Success(secondPageResponse)

        // When
        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase)
        advanceUntilIdle()

        // Verificar primera página
        val currentState = viewModel.uiState.first()
        assertEquals(1, currentState.tvShows.size)

        // Cargar siguiente página
        viewModel.loadNextPage()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
        assertEquals(2, finalState.tvShows.size)
        assertEquals(mockTvShow1, finalState.tvShows[0])
        assertEquals(mockTvShow2, finalState.tvShows[1])
        assertEquals(2, finalState.currentPage)
        assertTrue(finalState.canLoadMore)
        assertNull(finalState.error)
    }

    @Test
    fun `deberia reintentar carga inicial despues de error`() = runTest {
        // Given - error inicial
        coEvery { getPopularTvShowsUseCase(1) } returns NetworkResponse.Error("Error inicial")

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase)
        advanceUntilIdle()

        // Verificar estado de error
        val currentState = viewModel.uiState.first()
        assertEquals("Error inicial", currentState.error)
        assertTrue(currentState.tvShows.isEmpty())

        // Given - respuesta exitosa para reintento
        coEvery { getPopularTvShowsUseCase(1) } returns NetworkResponse.Success(
            PopularTvShowsResult(
                currentPage = 1,
                totalPages = 3,
                tvShows = listOf(mockTvShow1, mockTvShow2)
            )
        )

        // When
        viewModel.retryInitialLoad()
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
        assertEquals(2, finalState.tvShows.size)
        assertEquals(1, finalState.currentPage)
        assertEquals(3, finalState.totalPages)
        assertNull(finalState.error)
        assertTrue(finalState.canLoadMore)
    }

    @Test
    fun `no deberia cargar mas cuando esta en la ultima pagina`() = runTest {
        // Given - última página
        coEvery { getPopularTvShowsUseCase(1) } returns NetworkResponse.Success(
            PopularTvShowsResult(
                currentPage = 5,
                totalPages = 5,
                tvShows = listOf(mockTvShow1)
            )
        )

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertFalse(state.canLoadMore)
        assertEquals(5, state.currentPage)
        assertEquals(5, state.totalPages)
    }
}