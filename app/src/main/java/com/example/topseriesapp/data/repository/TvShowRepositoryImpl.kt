package com.example.topseriesapp.data.repository

import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.BuildConfig
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.withContext
import java.io.IOException

class TvShowRepositoryImpl(
    private val tmdbApiService: TMDBApiService,
    private val dispatchers: CoroutineDispatchers
) : TvShowRepository {

    override suspend fun getPopularTvShows(page: Int): NetworkResponse<List<TvShow>> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApiService.getPopularTvShows(
                    apiKey = BuildConfig.TMDB_API_KEY,
                    page = page
                    // language se tomará del valor por defecto en TmdbApiService
                )

                if (response.isSuccessful) {
                    val tvShows = response.body()?.results
                    if (tvShows != null) {
                        NetworkResponse.Success(tvShows)
                    } else {
                        NetworkResponse.Error("La respuesta de la API no contiene series.")
                    }
                } else {
                    NetworkResponse.Error("Error de API: ${response.code()} ${response.message()}")
                }
            } catch (e: IOException) {
                // Error de red
                NetworkResponse.Error("Error de red: ${e.message ?: "Error desconocido"}")
            } catch (e: Exception) {
                // Otros errores inesperados
                NetworkResponse.Error("Error inesperado: ${e.message ?: "Error desconocido"}")
            }
        }
    }
}