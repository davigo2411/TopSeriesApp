package com.example.topseriesapp.data.repository

import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.utils.NetworkResponse

fun interface TvShowRepository{
    suspend fun getPopularTvShows(page: Int): NetworkResponse<TvShowResponse>
}