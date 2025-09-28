package com.example.topseriesapp.ui.popularshows

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PopularTvShowsUiState(
    val isLoading: Boolean = false,
    val tvShows: List<TvShow> = emptyList(),
    val error: String? = null,
    val currentPage: Int = 1, // Página actual cargada
    val totalPages: Int = 0
){
    val canLoadMore: Boolean
        get() = currentPage < totalPages && totalPages > 0 && !isLoading
}

class PopularTvShowsViewModel(
    private val getPopularTvShowsUseCase: GetPopularTvShowsUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(PopularTvShowsUiState(isLoading = false))
    val uiState: StateFlow<PopularTvShowsUiState> = _uiState.asStateFlow()

    init {
        loadPopularTvShows(pageToLoad = 1)
    }

    private fun loadPopularTvShows(pageToLoad: Int) {
        if (_uiState.value.isLoading) return
        if (_uiState.value.totalPages in 1..<pageToLoad) return
        if (pageToLoad > 1 && pageToLoad <= _uiState.value.currentPage && _uiState.value.totalPages > 0) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val response = getPopularTvShowsUseCase(pageToLoad)) {
                is NetworkResponse.Success -> {
                    val result = response.data
                    if (result != null) {

                        _uiState.update { currentState ->

                            val combinedShows = if (result.currentPage == 1) {
                                result.tvShows
                            } else {
                                currentState.tvShows + result.tvShows
                            }

                            // Filtrar duplicados por ID
                            val uniqueShows = combinedShows.distinctBy { it.id }

                            currentState.copy(
                                isLoading = false,
                                tvShows = uniqueShows,
                                currentPage = result.currentPage,
                                totalPages = result.totalPages
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, error = "Respuesta exitosa pero sin datos.")
                        }
                    }
                }
                is NetworkResponse.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = response.message)
                    }
                }
            }
        }
    }

    fun loadNextPage() {
        if (_uiState.value.canLoadMore && !_uiState.value.isLoading) {
            val nextPage = _uiState.value.currentPage + 1
            loadPopularTvShows(pageToLoad = nextPage)
        }
    }

    fun retryInitialLoad() {
        _uiState.update { it.copy(tvShows = emptyList(), currentPage = 0, totalPages = 0, error = null) }
        loadPopularTvShows(pageToLoad = 1)
    }
}

