package com.example.topseriesapp.domain.usecase

import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.utils.NetworkResponse

interface GetTvShowDetailsUseCase {
    /**
     * Ejecuta el caso de uso para obtener los detalles de una serie de TV.
     * @param seriesId El ID de la serie de TV.
     * @return Un NetworkResponse que contiene TvShowDetails en caso de éxito o un error.
     */
    suspend operator fun invoke(seriesId: Int): NetworkResponse<TvShowDetails>
}


// Implementación del caso de uso para obtener los detalles de una serie de TV.
class GetTvShowDetailsUseCaseImpl(
    private val tvShowRepository: TvShowRepository
) : GetTvShowDetailsUseCase {

    override suspend operator fun invoke(seriesId: Int): NetworkResponse<TvShowDetails> {
        return tvShowRepository.getTvShowDetails(seriesId)
    }
}

