package com.example.topseriesapp.ui.popularshows

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PopularTvShowsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()


    // Mock data
    private val mockTvShow1 = TvShow(
        id = 1,
        name = "Breaking Bad",
        overview = "Un profesor de química se convierte en fabricante de drogas",
        posterPath = "/test1.jpg",
        backdropPath = "/backdrop1.jpg",
        firstAirDate = "2008-01-20",
        voteAverage = 9.5,
        voteCount = 5000,
        genreIds = listOf(18, 80),
        originCountry = listOf("US"),
        originalLanguage = "en",
        originalName = "Breaking Bad",
        popularity = 150.0
    )

    private val mockTvShow2 = TvShow(
        id = 2,
        name = "The Office",
        overview = "Una comedia sobre oficina",
        posterPath = "/test2.jpg",
        backdropPath = "/backdrop2.jpg",
        firstAirDate = "2005-03-24",
        voteAverage = 8.7,
        voteCount = 3000,
        genreIds = listOf(35),
        originCountry = listOf("US"),
        originalLanguage = "en",
        originalName = "The Office",
        popularity = 120.0
    )

    @Test
    fun deberia_mostrar_loading_inicial() {
        // Given - Estado de carga inicial
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = true,
                tvShows = emptyList(),
                error = null
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("initialLoadingIndicator")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Popular TV Shows")
            .assertIsDisplayed()
    }

    @Test
    fun deberia_mostrar_error_inicial_con_boton_reintentar() {
        // Given - Estado de error inicial
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = false,
                tvShows = emptyList(),
                error = "Error de conexión"
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("initialErrorMessage")
            .assertIsDisplayed()
            .assertTextContains("Error: Error de conexión")

        composeTestRule
            .onNodeWithTag("initialRetryButton")
            .assertIsDisplayed()
            .assertTextEquals("Retry")
    }

    @Test
    fun deberia_mostrar_lista_de_series() {
        // Given - Estado con series cargadas
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = false,
                tvShows = listOf(mockTvShow1, mockTvShow2),
                error = null,
                currentPage = 1,
                totalPages = 3
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then - Verificar contenedor de la lista
        composeTestRule
            .onNodeWithTag("popularTvShowList_container")
            .assertIsDisplayed()

        // Verificar que se muestran las series por texto (más confiable)
        composeTestRule
            .onNodeWithText("Breaking Bad")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("The Office")
            .assertIsDisplayed()

        // Verificar años de las series
        composeTestRule
            .onNodeWithText("2008")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("2005")
            .assertIsDisplayed()
    }

    @Test
    fun deberia_mostrar_estado_vacio() {
        // Given - Estado sin series pero sin error
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = false,
                tvShows = emptyList(),
                error = null
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("emptyStateText")
            .assertIsDisplayed()
            .assertTextEquals("No popular TV shows to display.")
    }

    @Test
    fun deberia_mostrar_indicador_carga_paginacion() {
        // Given - Estado cargando más series
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = true,
                tvShows = listOf(mockTvShow1),
                error = null,
                currentPage = 1,
                totalPages = 3
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then - Debe mostrar la serie existente
        composeTestRule
            .onNodeWithText("Breaking Bad")
            .assertIsDisplayed()

        // Y el indicador de carga para más contenido
        composeTestRule
            .onNodeWithTag("loadMoreIndicator")
            .assertIsDisplayed()
    }

    @Test
    fun deberia_mostrar_error_paginacion_con_boton_reintentar() {
        // Given - Error al cargar más series
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = false,
                tvShows = listOf(mockTvShow1),
                error = "Error al cargar más series",
                currentPage = 1,
                totalPages = 3
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithTag("loadMoreErrorMessage")
            .assertIsDisplayed()
            .assertTextContains("Error loading more: Error al cargar más series")

        composeTestRule
            .onNodeWithTag("loadMoreRetryButton")
            .assertIsDisplayed()
    }

    @Test
    fun deberia_ejecutar_accion_click_en_serie() {
        // Given
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = false,
                tvShows = listOf(mockTvShow1),
                error = null
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then - Click en la serie
        composeTestRule
            .onNodeWithTag("tvShowItem_1")
            .performClick()

        composeTestRule
            .onNodeWithTag("tvShowItem_1")
            .assertIsDisplayed()
    }

    @Test
    fun deberia_mostrar_detalles_correctos_en_tvshow_card() {
        // Given
        val viewModel = createMockViewModel(
            PopularTvShowsUiState(
                isLoading = false,
                tvShows = listOf(mockTvShow1),
                error = null
            )
        )

        // When
        composeTestRule.setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(
                    viewModel = viewModel,
                    onNavigateToDetails = { /* Mock navigation */ }
                )
            }
        }

        // Then - Verificar elementos por texto
        composeTestRule
            .onNodeWithText("Breaking Bad")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("2008")
            .assertIsDisplayed()

        // Verificar que se muestra la valoración correctamente
        composeTestRule
            .onNodeWithText("9.5")
            .assertIsDisplayed()

        // Verificar que la card es clickeable
        composeTestRule
            .onNodeWithText("Breaking Bad")
            .performClick()
    }

    private fun createMockViewModel(uiState: PopularTvShowsUiState): PopularTvShowsViewModel {
        val mockViewModel = mockk<PopularTvShowsViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(uiState)

        coEvery { mockViewModel.uiState } returns stateFlow as StateFlow<PopularTvShowsUiState>

        return mockViewModel
    }
}