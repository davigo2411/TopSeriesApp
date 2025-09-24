package com.example.topseriesapp.data.network

import com.example.topseriesapp.data.model.TvShowResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface TMDBApiService {
    //GET top rated TV shows
    @GET("tv/popular")
    suspend fun getTopPopularTvShows(
        // Returns a list of TV shows
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Response<TvShowResponse>
}