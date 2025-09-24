package com.example.topseriesapp.data.repository

import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.utils.NetworkResponse

fun interface TvShowRepository{
    suspend fun getPopularTvShows(page: Int): NetworkResponse<List<TvShow>>
}