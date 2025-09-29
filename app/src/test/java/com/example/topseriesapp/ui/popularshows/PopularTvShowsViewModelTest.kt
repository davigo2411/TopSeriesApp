package com.example.topseriesapp.ui.popularshows

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.domain.model.PopularTvShowsResult
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.ui.MainViewModel
import com.example.topseriesapp.utils.NetworkResponse
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
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

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private val getPopularTvShowsUseCase: GetPopularTvShowsUseCase = mockk()
    private val mainViewModel: MainViewModel = mockk()

    private lateinit var viewModel: PopularTvShowsViewModel

    private val mockTvShow1 = TvShow(
        id = 1, name = "Test Show 1", overview = "Test overview 1", posterPath = "/test1.jpg",
        backdropPath = "/backdrop1.jpg", firstAirDate = "2023-01-01", voteAverage = 8.5,
        voteCount = 1000, genreIds = listOf(18, 35), originCountry = listOf("US"),
        originalLanguage = "en", originalName = "Test Show Original Name", popularity = 150.0
    )

    private val mockTvShow2 = TvShow(
        id = 2, name = "Test Show 2", overview = "Test overview 2", posterPath = "/test2.jpg",
        backdropPath = "/backdrop2.jpg", firstAirDate = "2023-02-01", voteAverage = 7.8,
        voteCount = 800, genreIds = listOf(18, 80), originCountry = listOf("UK"),
        originalLanguage = "en", originalName = "Test Show 2 Original Name", popularity = 120.0
    )

    private val mockTvShow3 = TvShow(
        id = 3, name = "Test Show 3", overview = "Test overview 3", posterPath = "/test3.jpg",
        backdropPath = "/backdrop3.jpg", firstAirDate = "2023-03-01", voteAverage = 8.0,
        voteCount = 900, genreIds = listOf(28), originCountry = listOf("CA"),
        originalLanguage = "en", originalName = "Test Show 3 Original Name", popularity = 130.0
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { mainViewModel.languageUpdateTrigger } returns MutableStateFlow(0)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }


    @Test
    fun `deberia cargar series exitosamente al inicializar`() = runTest(testScheduler) {
        val popularShowsResult = PopularTvShowsResult(
            currentPage = 1, totalPages = 5, tvShows = listOf(mockTvShow1, mockTvShow2)
        )
        coEvery { getPopularTvShowsUseCase(1) } returns flowOf(NetworkResponse.Success(popularShowsResult))

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isFirstPageLoading)
        assertFalse(state.isNextPageLoading)
        assertEquals(2, state.tvShows.size)
        assertEquals(mockTvShow1, state.tvShows[0])
        assertEquals(mockTvShow2, state.tvShows[1])
        assertEquals(1, state.currentPage)
        assertEquals(5, state.totalPages)
        assertNull(state.error)
        assertTrue(state.canLoadMore)
    }

    @Test
    fun `deberia manejar errores de red correctamente al inicializar`() = runTest(testScheduler) {
        val errorMessage = "Error de conexión"
        coEvery { getPopularTvShowsUseCase(1) } returns flowOf(NetworkResponse.Error(errorMessage))

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isFirstPageLoading)
        assertFalse(state.isNextPageLoading)
        assertTrue(state.tvShows.isEmpty())
        assertEquals(errorMessage, state.error)
        assertEquals(0, state.currentPage)
        assertEquals(0, state.totalPages)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `deberia cargar siguiente pagina y agregar series`() = runTest(testScheduler) {
        val firstPageResult = PopularTvShowsResult(
            currentPage = 1, totalPages = 5, tvShows = listOf(mockTvShow1)
        )
        val secondPageResult = PopularTvShowsResult(
            currentPage = 2, totalPages = 5, tvShows = listOf(mockTvShow2)
        )

        coEvery { getPopularTvShowsUseCase(1) } returns flowOf(NetworkResponse.Success(firstPageResult))
        coEvery { getPopularTvShowsUseCase(2) } returns flowOf(NetworkResponse.Success(secondPageResult))

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals(1, currentState.tvShows.size)
        assertEquals(mockTvShow1, currentState.tvShows[0])
        assertEquals(1, currentState.currentPage)

        viewModel.loadNextPage()
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isFirstPageLoading)
        assertFalse(finalState.isNextPageLoading)
        assertEquals(2, finalState.tvShows.size)
        assertTrue(finalState.tvShows.contains(mockTvShow1))
        assertTrue(finalState.tvShows.contains(mockTvShow2))
        assertEquals(2, finalState.currentPage)
        assertTrue(finalState.canLoadMore)
        assertNull(finalState.error)
    }

    @Test
    fun `deberia reintentar carga inicial despues de error`() = runTest(testScheduler) {
        coEvery { getPopularTvShowsUseCase(1) } returns flowOf(NetworkResponse.Error("Error inicial"))

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)
        advanceUntilIdle()

        val currentState = viewModel.uiState.value
        assertEquals("Error inicial", currentState.error)
        assertTrue(currentState.tvShows.isEmpty())

        val successResult = PopularTvShowsResult(
            currentPage = 1, totalPages = 3, tvShows = listOf(mockTvShow1, mockTvShow2)
        )
        coEvery { getPopularTvShowsUseCase(1) } returns flowOf(NetworkResponse.Success(successResult))

        viewModel.retryInitialLoad()
        advanceUntilIdle()

        val finalState = viewModel.uiState.value
        assertFalse(finalState.isFirstPageLoading)
        assertFalse(finalState.isNextPageLoading)
        assertEquals(2, finalState.tvShows.size)
        assertEquals(1, finalState.currentPage)
        assertEquals(3, finalState.totalPages)
        assertNull(finalState.error)
        assertTrue(finalState.canLoadMore)
    }

    @Test
    fun `no deberia cargar mas cuando esta en la ultima pagina`() = runTest(testScheduler) {
        val lastPageResult = PopularTvShowsResult(
            currentPage = 5, totalPages = 5, tvShows = listOf(mockTvShow1)
        )
        coEvery { getPopularTvShowsUseCase(1) } returns flowOf(NetworkResponse.Success(lastPageResult))

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals(5, state.currentPage)
        assertEquals(5, state.totalPages)
        assertFalse(state.canLoadMore)

        viewModel.loadNextPage()
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertFalse(state.isNextPageLoading)
        assertEquals(5, state.currentPage)
        assertFalse(state.canLoadMore)
    }

    @Test
    fun `deberia estar en estado loading durante la carga inicial`() = runTest(testScheduler) { // Pasamos el scheduler
        val popularShowsResult = PopularTvShowsResult(currentPage = 1, totalPages = 1, tvShows = listOf(mockTvShow1))
        val controlledFlow = MutableSharedFlow<NetworkResponse<PopularTvShowsResult>>()
        coEvery { getPopularTvShowsUseCase(1) } returns controlledFlow

        // WHEN: Inicializar el ViewModel. La carga se dispara en el init.
        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)


        testScheduler.advanceUntilIdle() // Ejecuta tareas encoladas en el dispatcher

        // THEN: Verificar el estado de carga
        var state = viewModel.uiState.value
        assertTrue("El ViewModel debería estar en estado isFirstPageLoading después de advanceUntilIdle. Estado: $state", state.isFirstPageLoading)

        // WHEN: Permitir que el flow emita el éxito
        controlledFlow.emit(NetworkResponse.Success(popularShowsResult))
        testScheduler.advanceUntilIdle() // Permitir que la emisión y el procesamiento se completen

        // THEN: Verificar el estado final
        state = viewModel.uiState.value
        assertFalse("El ViewModel ya no debería estar en isFirstPageLoading. Estado: $state", state.isFirstPageLoading)
        assertTrue(state.tvShows.isNotEmpty())
        assertEquals(popularShowsResult.tvShows, state.tvShows)
    }

    @Test
    fun `deberia recargar desde la pagina 1 cuando el idioma cambia`() = runTest(testScheduler) {
        val initialResult = PopularTvShowsResult(currentPage = 1, totalPages = 3, tvShows = listOf(mockTvShow1, mockTvShow2))
        val resultAfterLanguageChange = PopularTvShowsResult(currentPage = 1, totalPages = 5, tvShows = listOf(mockTvShow3))
        val languageTrigger = MutableStateFlow(0)

        every { mainViewModel.languageUpdateTrigger } returns languageTrigger

        coEvery { getPopularTvShowsUseCase(1) } returnsMany listOf(
            flowOf(NetworkResponse.Success(initialResult)),
            flowOf(NetworkResponse.Success(resultAfterLanguageChange))
        )

        viewModel = PopularTvShowsViewModel(getPopularTvShowsUseCase, mainViewModel)
        advanceUntilIdle()

        var state = viewModel.uiState.value
        assertEquals("Después de la carga inicial, el tamaño de la lista de shows debería ser 2", 2, state.tvShows.size)
        if (state.tvShows.size == 2) {
            assertEquals(mockTvShow1, state.tvShows[0])
            assertEquals(mockTvShow2, state.tvShows[1])
        }
        assertEquals("Después de la carga inicial, la página actual debería ser 1", 1, state.currentPage)

        languageTrigger.value = 1
        advanceUntilIdle()

        state = viewModel.uiState.value
        assertEquals("Después del cambio de idioma, el tamaño de la lista de shows debería ser 1", 1, state.tvShows.size)
        if (state.tvShows.isNotEmpty()) {
            assertEquals(mockTvShow3, state.tvShows[0])
        }
        assertEquals("Después del cambio de idioma, la página actual debería ser 1", 1, state.currentPage)
        assertEquals(5, state.totalPages)
    }
}
