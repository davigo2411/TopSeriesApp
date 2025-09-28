package com.example.topseriesapp.data.repository

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
import com.example.topseriesapp.utils.NetworkResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException

class TvShowRepositoryImpl(
    private val apiService: TMDBApiService,
    private val popularTvShowDao: PopularTvShowDao,
    private val tvShowDetailsDao: TvShowDetailsDao,
    private val dispatchers: CoroutineDispatchers
) : TvShowRepository {

    private var lastKnownApiTotalPagesForPopular: Int = 0
    private var lastKnownApiCurrentPageForPopular: Int = 0
    private val itemsPerPageFromApi: Int = 20

    private fun TvShow.toPopularEntity(pageReceivedFromApi: Int, indexInPage: Int): PopularTvShowEntity {
        val calculatedApiOrderIndex = ((pageReceivedFromApi - 1) * itemsPerPageFromApi) + indexInPage
        return PopularTvShowEntity(
            id = this.id,
            name = this.name,
            overview = this.overview,
            posterPath = this.posterPath,
            backdropPath = this.backdropPath,
            voteAverage = this.voteAverage,
            firstAirDate = this.firstAirDate,
            voteCount = this.voteCount,
            genreIds = this.genreIds,
            originCountry = this.originCountry,
            originalLanguage = this.originalLanguage,
            originalName = this.originalName,
            popularity = this.popularity,
            apiOrderIndex = calculatedApiOrderIndex
        )
    }

    private fun PopularTvShowEntity.toTvShow(): TvShow {
        return TvShow(
            id = this.id,
            name = this.name,
            overview = this.overview,
            posterPath = this.posterPath,
            backdropPath = this.backdropPath,
            voteAverage = this.voteAverage,
            firstAirDate = this.firstAirDate,
            voteCount = this.voteCount,
            genreIds = this.genreIds,
            originCountry = this.originCountry,
            originalLanguage = this.originalLanguage,
            originalName = this.originalName,
            popularity = this.popularity
        )
    }

    private fun TvShowDetails.toDetailsEntity(): TvShowDetailsEntity {
        return TvShowDetailsEntity(
            id = this.id,
            name = this.name,
            adult = this.adult,
            backdropPath = this.backdropPath,
            firstAirDate = this.firstAirDate,
            homepage = this.homepage,
            inProduction = this.inProduction,
            lastAirDate = this.lastAirDate,
            originalLanguage = this.originalLanguage,
            originalName = this.originalName,
            overview = this.overview,
            popularity = this.popularity,
            posterPath = this.posterPath,
            status = this.status,
            tagline = this.tagline,
            type = this.type,
            voteAverage = this.voteAverage,
            voteCount = this.voteCount,
            numberOfEpisodes = this.numberOfEpisodes,
            numberOfSeasons = this.numberOfSeasons,
            createdBy = this.createdBy,
            episodeRunTime = this.episodeRunTime,
            genres = this.genres,
            languages = this.languages,
            networks = this.networks,
            originCountry = this.originCountry,
            productionCompanies = this.productionCompanies,
            productionCountries = this.productionCountries,
            seasons = this.seasons,
            spokenLanguages = this.spokenLanguages,
            lastEpisodeToAir = this.lastEpisodeToAir,
            nextEpisodeToAir = this.nextEpisodeToAir
        )
    }

    private fun TvShowDetailsEntity.toTvShowDetails(): TvShowDetails {
        return TvShowDetails(
            id = this.id,
            name = this.name,
            adult = this.adult,
            backdropPath = this.backdropPath,
            firstAirDate = this.firstAirDate,
            homepage = this.homepage,
            inProduction = this.inProduction,
            lastAirDate = this.lastAirDate,
            originalLanguage = this.originalLanguage,
            originalName = this.originalName,
            overview = this.overview,
            popularity = this.popularity,
            posterPath = this.posterPath,
            status = this.status,
            tagline = this.tagline,
            type = this.type,
            voteAverage = this.voteAverage,
            voteCount = this.voteCount,
            numberOfEpisodes = this.numberOfEpisodes,
            numberOfSeasons = this.numberOfSeasons,
            createdBy = this.createdBy,
            episodeRunTime = this.episodeRunTime,
            genres = this.genres,
            languages = this.languages,
            networks = this.networks,
            originCountry = this.originCountry,
            productionCompanies = this.productionCompanies,
            productionCountries = this.productionCountries,
            seasons = this.seasons,
            spokenLanguages = this.spokenLanguages,
            lastEpisodeToAir = this.lastEpisodeToAir,
            nextEpisodeToAir = this.nextEpisodeToAir
        )
    }

    override fun getPopularTvShows(page: Int): Flow<NetworkResponse<TvShowResponse>> = channelFlow {
        var emittedFromApiSuccessForThisRequest = false

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
                    null
                }
            }

        launch(dispatchers.io) {
            dbFlow.filterNotNull().collectLatest { cachedResponse ->
                if (!emittedFromApiSuccessForThisRequest) {
                    send(NetworkResponse.Success(cachedResponse))
                }
            }
        }

        try {
            val apiResponse = apiService.getPopularTvShows(apiKey = BuildConfig.TMDB_API_KEY, page = page)

            if (apiResponse.isSuccessful) {
                apiResponse.body()?.let { responseBody ->
                    if (responseBody.page == 1) {
                        popularTvShowDao.deleteAllPopularTvShows()
                        lastKnownApiCurrentPageForPopular = 0
                        lastKnownApiTotalPagesForPopular = 0
                    }

                    val entitiesToInsert = responseBody.results.mapIndexed { indexInPage, tvShowFromApi ->
                        tvShowFromApi.toPopularEntity(pageReceivedFromApi = responseBody.page, indexInPage = indexInPage)
                    }
                    popularTvShowDao.insertPopularTvShows(entitiesToInsert)

                    lastKnownApiTotalPagesForPopular = responseBody.totalPages
                    lastKnownApiCurrentPageForPopular = responseBody.page
                    emittedFromApiSuccessForThisRequest = true

                    send(NetworkResponse.Success(responseBody))

                } ?: run {
                    if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                        send(NetworkResponse.Error("Respuesta de API vacía (código ${apiResponse.code()})"))
                    }
                }
            } else {
                if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                    send(NetworkResponse.Error("Error de API: ${apiResponse.code()} ${apiResponse.message()}"))
                }
            }
        } catch (e: IOException) {
            if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                send(NetworkResponse.Error("Error de red: ${e.localizedMessage ?: "No se pudo conectar"}"))
            }
        } catch (e: Exception) {
            if (popularTvShowDao.getPopularTvShows().firstOrNull().isNullOrEmpty()) {
                send(NetworkResponse.Error("Error inesperado: ${e.localizedMessage ?: "Ocurrió un problema"}"))
            }
        }
    }.flowOn(dispatchers.io)


    override fun getTvShowDetails(seriesId: Int): Flow<NetworkResponse<TvShowDetails>> = channelFlow {
        var emittedFromApiSuccessForDetails = false
        var initialCacheEmissionDone = false

        val dbFlow = tvShowDetailsDao.getTvShowDetailsById(seriesId)
            .mapNotNull { entity -> entity?.toTvShowDetails() }

        launch(dispatchers.io) {
            dbFlow.collectLatest { cachedDetails ->
                if (!emittedFromApiSuccessForDetails) {
                    send(NetworkResponse.Success(cachedDetails))
                    initialCacheEmissionDone = true
                }
            }
        }

        try {
            val response = apiService.getTvShowDetails(seriesId = seriesId, apiKey = BuildConfig.TMDB_API_KEY)
            if (response.isSuccessful) {
                response.body()?.let { apiDetails ->
                    tvShowDetailsDao.insertOrUpdateTvShowDetails(apiDetails.toDetailsEntity())
                    emittedFromApiSuccessForDetails = true
                    send(NetworkResponse.Success(apiDetails))
                } ?: run {
                    if (!initialCacheEmissionDone) {
                        send(NetworkResponse.Error("Respuesta de API vacía para detalles (código ${response.code()})"))
                    }
                }
            } else {
                if (!initialCacheEmissionDone) {
                    send(NetworkResponse.Error("Error de API para detalles: ${response.code()} ${response.message()}"))
                }
            }
        } catch (e: IOException) {
            if (!initialCacheEmissionDone) {
                send(NetworkResponse.Error("Error de red para detalles: ${e.localizedMessage ?: "No se pudo conectar"}"))
            }
        } catch (e: Exception) {
            if (!initialCacheEmissionDone) {
                send(NetworkResponse.Error("Error inesperado para detalles: ${e.localizedMessage ?: "Ocurrió un problema"}"))
            }
        }
    }.flowOn(dispatchers.io)
}
