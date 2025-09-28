package com.example.topseriesapp.ui.popularshows

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PopularTvShowsViewModel(
    private val getPopularTvShowsUseCase: GetPopularTvShowsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PopularTvShowsUiState())
    val uiState: StateFlow<PopularTvShowsUiState> = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    init {
        Log.d("ViewModel_DEBUG", "PopularVM INIT block started")
        loadPopularTvShows(isInitialLoad = true)
        Log.d("ViewModel_DEBUG", "PopularVM INIT block finished")
    }

    private fun loadPopularTvShows(isInitialLoad: Boolean) {
        Log.d("ViewModel_DEBUG", "loadPopularTvShows CALLED. isInitialLoad: $isInitialLoad")
        val pageToLoad = if (isInitialLoad) 1 else _uiState.value.currentPage + 1
        Log.d("ViewModel_DEBUG", "pageToLoad: $pageToLoad")

        if (_uiState.value.isLoading) {
            Log.d("ViewModel_POP", "Load populares: Ya hay una carga en progreso.")
            return
        }

        if (!isInitialLoad && !_uiState.value.canLoadMore && _uiState.value.totalPages > 0) {
            Log.d("ViewModel_POP", "Load populares: No se puede cargar más (canLoadMore es false y no es carga inicial).")
            return
        }
        if (_uiState.value.totalPages > 0 && pageToLoad > _uiState.value.totalPages) {
            Log.d("ViewModel_POP", "Load populares: pageToLoad ($pageToLoad) > totalPages (${_uiState.value.totalPages}). No cargar.")
            return
        }

        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            getPopularTvShowsUseCase(pageToLoad)
                .onStart {
                    Log.d("ViewModel_POP", "Load populares: Iniciando carga para la página $pageToLoad. Initial: $isInitialLoad")
                    _uiState.update {
                        it.copy(
                            isFirstPageLoading = isInitialLoad,
                            isNextPageLoading = !isInitialLoad,
                            error = null
                        )
                    }
                }
                .catch { e ->
                    Log.e("ViewModel_POP", "Load populares: Error en el flujo para página $pageToLoad", e)
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
                                Log.d("ViewModel_POP", "Load populares: Éxito página ${result.currentPage}. Total API: ${result.totalPages}. Shows: ${result.tvShows.size}")
                                _uiState.update { currentState ->
                                    val newShows = result.tvShows
                                    val combinedShows = if (result.currentPage == 1) {
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
                                Log.w("ViewModel_POP", "Load populares: Éxito pero response.data es nulo para página $pageToLoad.")
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
                            Log.e("ViewModel_POP", "Load populares: Error de red página $pageToLoad: ${response.message}")
                            _uiState.update {
                                val currentShows = it.tvShows
                                val finalError = if (currentShows.isEmpty()) {
                                    response.message
                                } else if (pageToLoad > 1) {
                                    null
                                } else {
                                    response.message
                                }
                                it.copy(
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

    fun loadNextPage() {
        Log.d("ViewModel_POP", "loadNextPage() llamado. canLoadMore: ${_uiState.value.canLoadMore}")
        if (_uiState.value.canLoadMore) {
            loadPopularTvShows(isInitialLoad = false)
        } else {
            if (_uiState.value.isNextPageLoading) {
                _uiState.update { it.copy(isNextPageLoading = false) }
            }
        }
    }

    fun retryInitialLoad() {
        Log.d("ViewModel_POP", "retryInitialLoad() llamado.")
        _uiState.value = PopularTvShowsUiState()
        loadPopularTvShows(isInitialLoad = true)
    }

}

data class PopularTvShowsUiState(
    val isFirstPageLoading: Boolean = false,
    val isNextPageLoading: Boolean = false,
    val tvShows: List<TvShow> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 0,
) {
    val isLoading: Boolean get() = isFirstPageLoading || isNextPageLoading
    val canLoadMore: Boolean get() = !isLoading && currentPage < totalPages && totalPages > 0
}
