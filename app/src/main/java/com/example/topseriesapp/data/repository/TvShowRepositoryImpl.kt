package com.example.topseriesapp.data.repository

import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.BuildConfig
import com.example.topseriesapp.data.model.TvShowDetails
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
                    apiKey = BuildConfig.TMDB_API_KEY,
                    page = page
                )

                if (response.isSuccessful) {
                    val fullTvShowResponse = response.body()
                    if (fullTvShowResponse != null) {
                        NetworkResponse.Success(fullTvShowResponse)
                    } else {
                        NetworkResponse.Error("La respuesta de la API está vacía pero fue exitosa (código ${response.code()}).")
                    }
                } else {
                    response.errorBody()?.string()
                    NetworkResponse.Error("Error de API: ${response.code()} ${response.message()}")
                }
            } catch (e: IOException) {
                NetworkResponse.Error("Error de red: ${e.message ?: "Error desconocido"}")
            } catch (e: Exception) {
                NetworkResponse.Error("Error inesperado: ${e.message ?: "Error desconocido"}")
            }
        }
    }
    override suspend fun getTvShowDetails(seriesId: Int): NetworkResponse<TvShowDetails> {
        return withContext(dispatchers.io) {
            try {
                val response = tmdbApiService.getTvShowDetails(
                    seriesId = seriesId,
                    apiKey = BuildConfig.TMDB_API_KEY
                )

                if (response.isSuccessful) {
                    val details = response.body()
                    if (details != null) {
                        NetworkResponse.Success(details)
                    } else {
                        NetworkResponse.Error("La respuesta de la API para detalles está vacía pero fue exitosa (código ${response.code()}).")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    NetworkResponse.Error("Error de API al obtener detalles: ${response.code()} ${response.message()}. Cuerpo: $errorBody")
                }
            } catch (e: IOException) {
                NetworkResponse.Error("Error de red al obtener detalles: ${e.message ?: "Error desconocido"}")
            } catch (e: Exception) {
                NetworkResponse.Error("Error inesperado al obtener detalles: ${e.message ?: "Error desconocido"}")
            }
        }
    }
}