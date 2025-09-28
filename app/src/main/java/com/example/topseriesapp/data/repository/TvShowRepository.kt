package com.example.topseriesapp.data.repository

import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.Flow // Cambiado aquí

interface TvShowRepository {
    fun getPopularTvShows(page: Int): Flow<NetworkResponse<TvShowResponse>>

    fun getTvShowDetails(seriesId: Int): Flow<NetworkResponse<TvShowDetails>>
}