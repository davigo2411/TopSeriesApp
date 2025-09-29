package com.example.topseriesapp.domain.usecase

import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.domain.model.PopularTvShowsResult
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPopularTvShowsUseCase(
    private val tvShowRepository: TvShowRepository
) {
    // El caso de uso ahora devuelve un Flow
    operator fun invoke(page: Int): Flow<NetworkResponse<PopularTvShowsResult>> {
        return tvShowRepository.getPopularTvShows(page) // Obtiene el Flow del repositorio
            .map { networkResponse -> // Mapea cada emisión del Flow
                when (networkResponse) {
                    is NetworkResponse.Success -> {
                        val tvShowResponseData = networkResponse.data
                        tvShowResponseData?.let { nonNullData ->
                            NetworkResponse.Success(
                                PopularTvShowsResult(
                                    tvShows = nonNullData.results, // Asume que results es List<TvShow>
                                    currentPage = nonNullData.page,
                                    totalPages = nonNullData.totalPages
                                )
                            )
                        } ?: NetworkResponse.Error("Los datos recibidos del servidor son nulos en una respuesta exitosa.")
                    }
                    is NetworkResponse.Error -> {
                        NetworkResponse.Error(networkResponse.message ?: "Error desconocido en la respuesta del repositorio")
                    }
                }
            }
    }
}