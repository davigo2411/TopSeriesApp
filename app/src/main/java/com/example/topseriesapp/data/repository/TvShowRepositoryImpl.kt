package com.example.topseriesapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.topseriesapp.BuildConfig
import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.data.database.dao.PopularTvShowDao
import com.example.topseriesapp.data.database.dao.TvShowDetailsDao
import com.example.topseriesapp.data.database.entities.PopularTvShowEntity
import com.example.topseriesapp.data.database.entities.TvShowDetailsEntity
import com.example.topseriesapp.data.model.TvShow
import com.example.topseriesapp.data.model.TvShowDetails
import com.example.topseriesapp.data.model.TvShowResponse
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.utils.LocaleHelper
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Implementación del repositorio para obtener datos de series de TV.
 * Combina datos de una API remota (TMDB) y una caché local (Room).
 *
 * @param apiService Servicio Retrofit para interactuar con la API de TMDB.
 * @param popularTvShowDao DAO para acceder a la caché de series populares.
 * @param tvShowDetailsDao DAO para acceder a la caché de detalles de series.
 * @param dispatchers Proveedor de dispatchers para corutinas.
 * @param applicationContext Contexto de la aplicación para acceder a SharedPreferences y recursos.
 */
class TvShowRepositoryImpl(
    private val apiService: TMDBApiService,
    private val popularTvShowDao: PopularTvShowDao,
    private val tvShowDetailsDao: TvShowDetailsDao,
    private val dispatchers: CoroutineDispatchers,
    private val applicationContext: Context
) : TvShowRepository {

    // Almacena información sobre la paginación de la API para series populares.
    private var lastKnownApiTotalPagesForPopular: Int = 0
    private var lastKnownApiCurrentPageForPopular: Int = 0
    private val itemsPerPageFromApi: Int = 20 // Número de items por página que devuelve la API.

    // SharedPreferences para acceder a banderas de la aplicación, como el cambio de idioma.
    private val appPrefs: SharedPreferences by lazy {
        applicationContext.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
    }

    // --- Funciones de Mapeo entre Modelos de Red/UI y Entidades de Base de Datos ---

    private fun TvShow.toPopularEntity(pageReceivedFromApi: Int, indexInPage: Int): PopularTvShowEntity {
        val calculatedApiOrderIndex = ((pageReceivedFromApi - 1) * itemsPerPageFromApi) + indexInPage
        return PopularTvShowEntity(
            id = this.id, name = this.name, overview = this.overview, posterPath = this.posterPath,
            backdropPath = this.backdropPath, voteAverage = this.voteAverage, firstAirDate = this.firstAirDate,
            voteCount = this.voteCount, genreIds = this.genreIds, originCountry = this.originCountry,
            originalLanguage = this.originalLanguage, originalName = this.originalName,
            popularity = this.popularity, apiOrderIndex = calculatedApiOrderIndex
        )
    }

    private fun PopularTvShowEntity.toTvShow(): TvShow {
        return TvShow(
            id = this.id, name = this.name, overview = this.overview, posterPath = this.posterPath,
            backdropPath = this.backdropPath, voteAverage = this.voteAverage, firstAirDate = this.firstAirDate,
            voteCount = this.voteCount, genreIds = this.genreIds, originCountry = this.originCountry,
            originalLanguage = this.originalLanguage, originalName = this.originalName, popularity = this.popularity
        )
    }

    private fun TvShowDetails.toDetailsEntity(): TvShowDetailsEntity {
        return TvShowDetailsEntity(
            id = this.id, name = this.name, adult = this.adult, backdropPath = this.backdropPath,
            firstAirDate = this.firstAirDate, homepage = this.homepage, inProduction = this.inProduction,
            lastAirDate = this.lastAirDate, originalLanguage = this.originalLanguage,
            originalName = this.originalName, overview = this.overview, popularity = this.popularity,
            posterPath = this.posterPath, status = this.status, tagline = this.tagline, type = this.type,
            voteAverage = this.voteAverage, voteCount = this.voteCount, numberOfEpisodes = this.numberOfEpisodes,
            numberOfSeasons = this.numberOfSeasons, createdBy = this.createdBy,
            episodeRunTime = this.episodeRunTime, genres = this.genres, languages = this.languages,
            networks = this.networks, originCountry = this.originCountry,
            productionCompanies = this.productionCompanies, productionCountries = this.productionCountries,
            seasons = this.seasons, spokenLanguages = this.spokenLanguages,
            lastEpisodeToAir = this.lastEpisodeToAir, nextEpisodeToAir = this.nextEpisodeToAir
        )
    }

    private fun TvShowDetailsEntity.toTvShowDetails(): TvShowDetails {
        return TvShowDetails(
            id = this.id, name = this.name, adult = this.adult, backdropPath = this.backdropPath,
            firstAirDate = this.firstAirDate, homepage = this.homepage, inProduction = this.inProduction,
            lastAirDate = this.lastAirDate, originalLanguage = this.originalLanguage,
            originalName = this.originalName, overview = this.overview, popularity = this.popularity,
            posterPath = this.posterPath, status = this.status, tagline = this.tagline, type = this.type,
            voteAverage = this.voteAverage, voteCount = this.voteCount, numberOfEpisodes = this.numberOfEpisodes,
            numberOfSeasons = this.numberOfSeasons, createdBy = this.createdBy,
            episodeRunTime = this.episodeRunTime, genres = this.genres, languages = this.languages,
            networks = this.networks, originCountry = this.originCountry,
            productionCompanies = this.productionCompanies, productionCountries = this.productionCountries,
            seasons = this.seasons, spokenLanguages = this.spokenLanguages,
            lastEpisodeToAir = this.lastEpisodeToAir, nextEpisodeToAir = this.nextEpisodeToAir
        )
    }

    /**
     * Obtiene una página de series de TV populares.
     * Primero emite datos de la caché local (si existen) y luego intenta obtener datos frescos de la API.
     * Si el idioma de la aplicación ha cambiado, limpia la caché de populares antes de obtener nuevos datos.
     */
    override fun getPopularTvShows(page: Int): Flow<NetworkResponse<TvShowResponse>> = channelFlow {
        var emittedFromApiSuccessForThisRequest = false
        val languageChangedFlag = appPrefs.getBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false)

        // Si es la primera página y el idioma cambió, limpia la caché y resetea la bandera.
        if (page == 1 && languageChangedFlag) {
            popularTvShowDao.deleteAllPopularTvShows()
            appPrefs.edit().putBoolean(LocaleHelper.LANGUAGE_CHANGED_FLAG, false).apply()
            lastKnownApiCurrentPageForPopular = 0
            lastKnownApiTotalPagesForPopular = 0
        }

        // Flujo de datos desde la base de datos local.
        val dbFlow = popularTvShowDao.getPopularTvShows()
            .map { entities ->
                if (entities.isNotEmpty()) {
                    TvShowResponse(
                        page = if (lastKnownApiCurrentPageForPopular > 0) lastKnownApiCurrentPageForPopular else 1,
                        results = entities.map { it.toTvShow() },
                        totalPages = if (lastKnownApiTotalPagesForPopular > 0) lastKnownApiTotalPagesForPopular else 1,
                        totalResults = if (lastKnownApiTotalPagesForPopular > 0) lastKnownApiTotalPagesForPopular * itemsPerPageFromApi else entities.size
                    )
                } else {
                    null // No hay datos en caché.
                }
            }

        // Lanza una corutina para observar y emitir datos de la caché.
        launch(dispatchers.io) {
            dbFlow.filterNotNull().collectLatest { cachedResponse ->
                // Emite desde caché solo si no se ha emitido desde la API para esta petición
                // y si no estamos en el caso de que el idioma cambió y es la página 1 (se espera API).
                if (!emittedFromApiSuccessForThisRequest && !(languageChangedFlag && page == 1)) {
                    send(NetworkResponse.Success(cachedResponse))
                }
            }
        }

        // Intenta obtener datos de la API.
        try {
            val currentLang = LocaleHelper.getCurrentAppLocale(applicationContext).language
            val apiResponseCall = apiService.getPopularTvShows(apiKey = BuildConfig.TMDB_API_KEY, currentLang, page = page)

            if (apiResponseCall.isSuccessful) {
                apiResponseCall.body()?.let { responseBody ->
                    // Si es la primera página y el idioma no ha cambiado recientemente (la bandera ya se reseteó),
                    // limpia la caché para asegurar que los datos de la API sean la única fuente para la página 1.
                    if (responseBody.page == 1 && !languageChangedFlag) {
                        popularTvShowDao.deleteAllPopularTvShows()
                        lastKnownApiCurrentPageForPopular = 0
                        lastKnownApiTotalPagesForPopular = 0
                    }

                    // Mapea y guarda los nuevos datos en la base de datos.
                    val entitiesToInsert = responseBody.results.mapIndexed { indexInPage, tvShowFromApi ->
                        tvShowFromApi.toPopularEntity(pageReceivedFromApi = responseBody.page, indexInPage = indexInPage)
                    }
                    popularTvShowDao.insertPopularTvShows(entitiesToInsert)

                    // Actualiza la información de paginación y emite los datos de la API.
                    lastKnownApiTotalPagesForPopular = responseBody.totalPages
                    lastKnownApiCurrentPageForPopular = responseBody.page
                    emittedFromApiSuccessForThisRequest = true
                    send(NetworkResponse.Success(responseBody))

                } ?: run {
                    // Cuerpo de respuesta vacío: emite error si no hay caché.
                    if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                        send(NetworkResponse.Error("Respuesta de API vacía (código ${apiResponseCall.code()})"))
                    }
                }
            } else {
                // Error de API: emite error si no hay caché.
                if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                    send(NetworkResponse.Error("Error de API: ${apiResponseCall.code()} ${apiResponseCall.message()}"))
                }
            }
        } catch (e: IOException) {
            // Error de red: emite error si no hay caché.
            if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                send(NetworkResponse.Error("Error de red: ${e.localizedMessage ?: "No se pudo conectar"}"))
            }
        } catch (e: Exception) {
            // Otro error: emite error si no hay caché.
            if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                send(NetworkResponse.Error("Error inesperado: ${e.localizedMessage ?: "Ocurrió un problema"}"))
            }
        }
    }.flowOn(dispatchers.io) // Ejecuta el flujo en el dispatcher de IO.

    /**
     * Obtiene los detalles de una serie de TV específica.
     * Emite datos de la caché local primero, luego intenta obtener datos frescos de la API.
     * Si el idioma de la aplicación cambió, idealmente se invalidaría la caché de detalles,
     * aunque esta lógica específica no está implementada aquí para los detalles (solo para populares).
     */
    override fun getTvShowDetails(seriesId: Int): Flow<NetworkResponse<TvShowDetails>> = channelFlow {
        var emittedFromApiSuccessForDetails = false
        var initialCacheEmissionDone = false // Para saber si ya se emitió desde caché.

        // Flujo de datos desde la base de datos local para los detalles.
        val dbFlow = tvShowDetailsDao.getTvShowDetailsById(seriesId)
            .mapNotNull { entity -> entity?.toTvShowDetails() }

        // Lanza una corutina para observar y emitir detalles de la caché.
        launch(dispatchers.io) {
            dbFlow.collectLatest { cachedDetails ->
                if (!emittedFromApiSuccessForDetails) { // Emite solo si la API aún no ha respondido con éxito.
                    send(NetworkResponse.Success(cachedDetails))
                    initialCacheEmissionDone = true
                }
            }
        }

        // Intenta obtener detalles de la API.
        try {
            val currentLang = LocaleHelper.getCurrentAppLocale(applicationContext).language
            val response = apiService.getTvShowDetails(seriesId = seriesId, apiKey = BuildConfig.TMDB_API_KEY, currentLang)

            if (response.isSuccessful) {
                response.body()?.let { apiDetails ->
                    // Guarda los nuevos detalles en la base de datos.
                    tvShowDetailsDao.insertOrUpdateTvShowDetails(apiDetails.toDetailsEntity())
                    emittedFromApiSuccessForDetails = true
                    send(NetworkResponse.Success(apiDetails)) // Emite los datos frescos de la API.
                } ?: run {
                    // Cuerpo de respuesta vacío: emite error si no hubo emisión inicial de caché.
                    if (!initialCacheEmissionDone && tvShowDetailsDao.getTvShowDetailsById(seriesId).firstOrNull() == null) {
                        send(NetworkResponse.Error("Respuesta de API vacía para detalles (código ${response.code()})"))
                    }
                }
            } else {
                // Error de API: emite error si no hubo emisión inicial de caché.
                if (!initialCacheEmissionDone && tvShowDetailsDao.getTvShowDetailsById(seriesId).firstOrNull() == null) {
                    send(NetworkResponse.Error("Error de API para detalles: ${response.code()} ${response.message()}"))
                }
            }
        } catch (e: IOException) {
            // Error de red: emite error si no hubo emisión inicial de caché.
            if (!initialCacheEmissionDone && tvShowDetailsDao.getTvShowDetailsById(seriesId).firstOrNull() == null) {
                send(NetworkResponse.Error("Error de red para detalles: ${e.localizedMessage ?: "No se pudo conectar"}"))
            }
        } catch (e: Exception) {
            // Otro error: emite error si no hubo emisión inicial de caché.
            if (!initialCacheEmissionDone && tvShowDetailsDao.getTvShowDetailsById(seriesId).firstOrNull() == null) {
                send(NetworkResponse.Error("Error inesperado para detalles: ${e.localizedMessage ?: "Ocurrió un problema"}"))
            }
        }
    }.flowOn(dispatchers.io) // Ejecuta el flujo en el dispatcher de IO.
}
