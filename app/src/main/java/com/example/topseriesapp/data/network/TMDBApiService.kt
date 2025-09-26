package com.example.topseriesapp.data.network

import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.TvShowResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Path

interface TMDBApiService {
    //GET popular TV shows
    @GET("tv/popular")
    suspend fun getPopularTvShows(
        // Devuelve una lista de TvShows
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): Response<TvShowResponse>

    //GET TV shows details by ID
    @GET("tv/{series_id}")
    suspend fun getTvShowDetails(
        @Path("series_id") seriesId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES"
    ): Response<TvShowDetails>
}