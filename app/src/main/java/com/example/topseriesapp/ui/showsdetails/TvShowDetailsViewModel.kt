package com.example.topseriesapp.ui.showsdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCase
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.topseriesapp.utils.SERIES_ID_KEY


class TvShowDetailsViewModel(
    private val getTvShowDetailsUseCase: GetTvShowDetailsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsScreenUiState>(DetailsScreenUiState.Loading)
    val uiState: StateFlow<DetailsScreenUiState> = _uiState.asStateFlow()

    init {
        val seriesId: Int? = savedStateHandle[SERIES_ID_KEY]
        if (seriesId != null) {
            fetchTvShowDetails(seriesId)
        } else {
            _uiState.value = DetailsScreenUiState.Error("Series ID not provided.")
        }
    }

    fun fetchTvShowDetails(seriesId: Int) {
        _uiState.value = DetailsScreenUiState.Loading
        viewModelScope.launch {
            // Llamada al caso de uso
            when (val response = getTvShowDetailsUseCase(seriesId)) {
                is NetworkResponse.Success -> {
                    _uiState.value = response.data?.let { DetailsScreenUiState.Success(it) }
                        ?: DetailsScreenUiState.Error("Show details data is null.")
                }
                is NetworkResponse.Error -> {
                    _uiState.value = DetailsScreenUiState.Error(response.message ?: "Unknown error fetching details.")
                }
            }
        }
    }

    fun retry() {
        val seriesId: Int? = savedStateHandle[SERIES_ID_KEY]
        if (seriesId != null) {
            fetchTvShowDetails(seriesId)
        } else {
            _uiState.value = DetailsScreenUiState.Error("Series ID not available for retry.")
        }
    }
}


// Clase para representar los diferentes estados de la UI de la pantalla de detalles
sealed interface DetailsScreenUiState {
    data object Loading : DetailsScreenUiState
    data class Success(val tvShowDetails: TvShowDetails) : DetailsScreenUiState
    data class Error(val message: String) : DetailsScreenUiState
}