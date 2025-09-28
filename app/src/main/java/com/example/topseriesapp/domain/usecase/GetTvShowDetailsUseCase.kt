package com.example.topseriesapp.domain.usecase

import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.Flow

interface GetTvShowDetailsUseCase {
    /**
     * Ejecuta el caso de uso para obtener los detalles de una serie de TV.
     * @param seriesId El ID de la serie de TV.
     * @return Un Flow que emite NetworkResponse conteniendo TvShowDetails en caso de éxito o un error.
     */
    // Cambiado para devolver Flow y no ser suspend
    operator fun invoke(seriesId: Int): Flow<NetworkResponse<TvShowDetails>>
}


// Implementación del caso de uso para obtener los detalles de una serie de TV.
class GetTvShowDetailsUseCaseImpl(
    private val tvShowRepository: TvShowRepository
) : GetTvShowDetailsUseCase {

    // Cambiado para devolver Flow y no ser suspend
    override operator fun invoke(seriesId: Int): Flow<NetworkResponse<TvShowDetails>> {

        return tvShowRepository.getTvShowDetails(seriesId)
    }
}

