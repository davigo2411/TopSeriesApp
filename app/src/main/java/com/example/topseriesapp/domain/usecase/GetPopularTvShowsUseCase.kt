package com.example.topseriesapp.domain.usecase

import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.utils.NetworkResponse
import com.example.topseriesapp.domain.model.PopularTvShowsResult

class GetPopularTvShowsUseCase(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(page: Int): NetworkResponse<PopularTvShowsResult> {
        return when (val repoResponse = tvShowRepository.getPopularTvShows(page)) {
            is NetworkResponse.Success -> {
                val tvShowResponseData = repoResponse.data
                tvShowResponseData?.let { nonNullData ->
                    NetworkResponse.Success(
                        PopularTvShowsResult(
                            tvShows = nonNullData.results,
                            currentPage = nonNullData.page,
                            totalPages = nonNullData.totalPages
                        )
                    )
                } ?: NetworkResponse.Error("Los datos recibidos del servidor son nulos en una respuesta exitosa.")
            }
            is NetworkResponse.Error -> {
                NetworkResponse.Error(repoResponse.message ?: "Error desconocido en la respuesta del repositorio")
            }
        }
    }
}