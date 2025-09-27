package com.example.topseriesapp.data.repository

import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.utils.NetworkResponse

interface TvShowRepository{
    suspend fun getPopularTvShows(page: Int): NetworkResponse<TvShowResponse>

    suspend fun getTvShowDetails(seriesId: Int): NetworkResponse<TvShowDetails>
}