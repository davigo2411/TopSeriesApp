package com.example.topseriesapp.ui.showsdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCase
import com.example.topseriesapp.utils.NetworkResponse
import com.example.topseriesapp.utils.SERIES_ID_KEY
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

interface ConnectivityChecker {
    suspend fun isOnline(): Boolean
}

class TvShowDetailsViewModel(
    private val getTvShowDetailsUseCase: GetTvShowDetailsUseCase,
    private val savedStateHandle: SavedStateHandle,
    private val connectivityChecker: ConnectivityChecker
) : ViewModel() {
    private val _uiState = MutableStateFlow<DetailsScreenUiState>(DetailsScreenUiState.Loading)
    val uiState: StateFlow<DetailsScreenUiState> = _uiState.asStateFlow()

    private var currentDetailsJob: Job? = null
    private var currentSeriesId: Int? = null

    init {
        getSeriesIdAndFetchDetails()
    }

    private fun getSeriesIdAndFetchDetails() {
        val seriesId: Int? = savedStateHandle[SERIES_ID_KEY]
        currentSeriesId = seriesId
        if (seriesId != null) {
            fetchTvShowDetails(seriesId)
        } else {
            _uiState.value = DetailsScreenUiState.Error(
                message = "ID de serie no proporcionado.",
                isOfflineAndNoData = false
            )
        }
    }

    private fun fetchTvShowDetails(seriesId: Int) {
        currentDetailsJob?.cancel()
        currentDetailsJob = viewModelScope.launch {
            _uiState.value = DetailsScreenUiState.Loading

            getTvShowDetailsUseCase(seriesId)
                .catch { e ->
                    val isOnline = connectivityChecker.isOnline()
                    _uiState.value = DetailsScreenUiState.Error(
                        message = if (!isOnline) "Detalles no disponibles sin conexión."
                        else "Error en el flujo de datos: ${e.localizedMessage}",
                        isOfflineAndNoData = !isOnline
                    )
                }
                .collect { response ->
                    val isOnline = connectivityChecker.isOnline()
                    when (response) {
                        is NetworkResponse.Success -> {
                            _uiState.value = response.data?.let { DetailsScreenUiState.Success(it) }
                                ?: DetailsScreenUiState.Error(
                                    message = "Los datos de los detalles están vacíos.",
                                    isOfflineAndNoData = !isOnline && _uiState.value !is DetailsScreenUiState.Success
                                )
                        }
                        is NetworkResponse.Error -> {
                            if (_uiState.value !is DetailsScreenUiState.Success) {
                                val isNetworkRelatedError =
                                    response.message?.contains("host", ignoreCase = true) == true ||
                                            response.message?.contains("network", ignoreCase = true) == true ||
                                            response.message?.contains("IOEx", ignoreCase = true) == true

                                if (!isOnline && isNetworkRelatedError) {
                                    _uiState.value = DetailsScreenUiState.Error(
                                        message = "Detalles no disponibles sin conexión.",
                                        isOfflineAndNoData = true
                                    )
                                } else {
                                    _uiState.value = DetailsScreenUiState.Error(
                                        message = response.message ?: "Error desconocido al obtener detalles.",
                                        isOfflineAndNoData = false
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    fun retry() {
        currentSeriesId?.let {
            fetchTvShowDetails(it)
        } ?: getSeriesIdAndFetchDetails()
    }
}

sealed interface DetailsScreenUiState {
    data object Loading : DetailsScreenUiState
    data class Success(val tvShowDetails: TvShowDetails) : DetailsScreenUiState
    data class Error(val message: String, val isOfflineAndNoData: Boolean) : DetailsScreenUiState
}

