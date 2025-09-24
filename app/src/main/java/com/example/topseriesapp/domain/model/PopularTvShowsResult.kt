package com.example.topseriesapp.domain.model

import com.example.topseriesapp.data.model.TvShow

data class PopularTvShowsResult(
    val tvShows: List<TvShow>,
    val currentPage: Int,
    val totalPages: Int
)