package com.example.topseriesapp.data.repository

import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.BuildConfig
import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.withContext
import java.io.IOException

class TvShowRepositoryImpl(
    private val tmdbApiService: TMDBApiService,
    private val dispatchers: CoroutineDispatchers
) : TvShowRepository {

    override suspend fun getPopularTvShows(page: Int): NetworkResponse<TvShowResponse> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApiService.getPopularTvShows(
                    apiKey = BuildConfig.TMDB_API_KEY, // Asumiendo que tu API service lo necesita aquí
                    page = page
                )

                if (response.isSuccessful) {
                    val fullTvShowResponse = response.body() // <--- Obtén el objeto TvShowResponse completo
                    if (fullTvShowResponse != null) {
                        // CORRECCIÓN: Envuelve el TvShowResponse completo en Success
                        NetworkResponse.Success(fullTvShowResponse)
                    } else {
                        // Esto podría indicar un problema si la respuesta fue 2xx pero el cuerpo es null
                        NetworkResponse.Error("La respuesta de la API está vacía pero fue exitosa (código ${response.code()}).")
                    }
                } else {
                    NetworkResponse.Error("Error de API: ${response.code()} ${response.message()}")
                }
            } catch (e: IOException) {
                NetworkResponse.Error("Error de red: ${e.message ?: "Error desconocido"}")
            } catch (e: Exception) {
                NetworkResponse.Error("Error inesperado: ${e.message ?: "Error desconocido"}")
            }
        }
    }
}