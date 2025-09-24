package com.example.topseriesapp.domain.usecase

import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.utils.NetworkResponse // O como lo hayas llamado

class GetPopularTvShowsUseCase(
    private val tvShowRepository: TvShowRepository
) {
    suspend operator fun invoke(page: Int): NetworkResponse<List<TvShow>> {
        return tvShowRepository.getPopularTvShows(page)
    }
}