package com.example.topseriesapp.ui.popularshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.ui.MainViewModel
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla que muestra la lista de series de TV populares.
 * Gestiona la carga de datos, la paginación y las actualizaciones de estado de la UI.
 *
 * @param getPopularTvShowsUseCase Caso de uso para obtener las series populares.
 * @param mainViewModel ViewModel principal para observar cambios globales como el idioma.
 */
class PopularTvShowsViewModel(
    private val getPopularTvShowsUseCase: GetPopularTvShowsUseCase,
    private val mainViewModel: MainViewModel
) : ViewModel() {

    // Estado de la UI que será observado por el Composable.
    private val _uiState = MutableStateFlow(PopularTvShowsUiState())
    val uiState: StateFlow<PopularTvShowsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null // Job para la corutina de carga de datos, permite cancelarla.

    init {
        // Observa el trigger de cambio de idioma desde MainViewModel.
        viewModelScope.launch {
            mainViewModel.languageUpdateTrigger
                .drop(1) // Ignora el valor inicial para no recargar al suscribirse por primera vez.
                .collect {
                    // Si el idioma cambia, resetea el estado y fuerza la recarga de la página inicial.
                    _uiState.value = PopularTvShowsUiState()
                    loadPopularTvShows(isInitialLoad = true)
                }
        }
        // Carga inicial de las series populares.
        loadPopularTvShows(isInitialLoad = true)
    }

    /**
     * Carga las series populares, ya sea la página inicial o la siguiente página.
     * @param isInitialLoad true si es la primera carga o una recarga forzada, false para paginación.
     */
    private fun loadPopularTvShows(isInitialLoad: Boolean) {
        val pageToLoad = if (isInitialLoad || _uiState.value.currentPage == 0) 1 else _uiState.value.currentPage + 1

        // Evita cargas múltiples simultáneas o cargar más allá del total de páginas.
        if (_uiState.value.isLoading) return
        if (!isInitialLoad && !_uiState.value.canLoadMore && _uiState.value.totalPages > 0) return
        if (_uiState.value.totalPages > 0 && pageToLoad > _uiState.value.totalPages) {
            if (_uiState.value.isNextPageLoading) _uiState.update { it.copy(isNextPageLoading = false) }
            return
        }

        fetchJob?.cancel() // Cancela cualquier carga anterior en progreso.
        fetchJob = viewModelScope.launch {
            getPopularTvShowsUseCase(pageToLoad)
                .onStart {
                    // Actualiza el estado de carga en la UI.
                    _uiState.update {
                        it.copy(
                            isFirstPageLoading = isInitialLoad,
                            isNextPageLoading = !isInitialLoad && pageToLoad > 1,
                            error = null
                        )
                    }
                }
                .catch { e ->
                    // Maneja errores durante la obtención de datos.
                    _uiState.update {
                        it.copy(
                            isFirstPageLoading = false,
                            isNextPageLoading = false,
                            error = "Error: ${e.localizedMessage}"
                        )
                    }
                }
                .collect { response ->
                    when (response) {
                        is NetworkResponse.Success -> {
                            val result = response.data
                            if (result != null) {
                                // Actualiza el estado con los nuevos datos recibidos.
                                _uiState.update { currentState ->
                                    val newShows = result.tvShows
                                    // Si es carga inicial, reemplaza la lista; sino, añade los nuevos shows.
                                    val combinedShows = if (isInitialLoad) {
                                        newShows
                                    } else {
                                        (currentState.tvShows + newShows).distinctBy { it.id }
                                    }
                                    currentState.copy(
                                        isFirstPageLoading = false,
                                        isNextPageLoading = false,
                                        tvShows = combinedShows,
                                        currentPage = result.currentPage,
                                        totalPages = result.totalPages,
                                        error = null
                                    )
                                }
                            } else {
                                // Maneja el caso de éxito pero con datos nulos.
                                _uiState.update {
                                    it.copy(
                                        isFirstPageLoading = false,
                                        isNextPageLoading = false,
                                        error = if (it.tvShows.isEmpty()) "No se encontraron series." else null
                                    )
                                }
                            }
                        }
                        is NetworkResponse.Error -> {
                            // Maneja errores de red o de la API.
                            _uiState.update { currentState ->
                                // Muestra error solo si la lista está vacía o es la primera página.
                                val finalError = if (currentState.tvShows.isEmpty() || pageToLoad == 1) {
                                    response.message
                                } else {
                                    null // No muestra error si ya hay datos y falla la paginación.
                                }
                                currentState.copy(
                                    isFirstPageLoading = false,
                                    isNextPageLoading = false,
                                    error = finalError
                                )
                            }
                        }
                    }
                }
        }
    }

    /**
     * Inicia la carga de la siguiente página de series populares si es posible.
     */
    fun loadNextPage() {
        if (_uiState.value.canLoadMore && !_uiState.value.isLoading) {
            loadPopularTvShows(isInitialLoad = false)
        } else {
            // Si no se puede cargar más, asegura que el indicador de carga de siguiente página esté desactivado.
            if (_uiState.value.isNextPageLoading) {
                _uiState.update { it.copy(isNextPageLoading = false) }
            }
        }
    }

    /**
     * Reinicia el estado y reintenta la carga inicial de las series.
     * Útil para un botón de "Reintentar" en caso de error en la carga inicial.
     */
    fun retryInitialLoad() {
        _uiState.value = PopularTvShowsUiState() // Resetea completamente el estado.
        loadPopularTvShows(isInitialLoad = true)
    }
}

/**
 * Data class que representa el estado de la UI para la pantalla de series populares.
 */
data class PopularTvShowsUiState(
    val isFirstPageLoading: Boolean = false,
    val isNextPageLoading: Boolean = false,
    val tvShows: List<TvShow> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
) {
    // Indica si alguna operación de carga está en progreso.
    val isLoading: Boolean get() = isFirstPageLoading || isNextPageLoading
    // Determina si se pueden cargar más páginas.
    val canLoadMore: Boolean get() = !isLoading && currentPage < totalPages && totalPages > 0 && currentPage > 0
}

