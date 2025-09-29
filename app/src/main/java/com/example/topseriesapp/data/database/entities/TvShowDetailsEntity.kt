package com.example.topseriesapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.topseriesapp.data.database.converters.AppTypeConverters
import com.example.topseriesapp.data.model.*

@Entity(tableName = "tv_show_details")
@TypeConverters(AppTypeConverters::class)
data class TvShowDetailsEntity(
    @PrimaryKey val id: Int, // id es no nulable en tu modelo API
    val name: String,         // name es no nulable en tu modelo API

    val adult: Boolean?, // Coincide con API (nulable)
    val backdropPath: String?,
    val firstAirDate: String?,
    val homepage: String?,
    val inProduction: Boolean?, // Coincide con API (nulable)
    val lastAirDate: String?,
    val originalLanguage: String?,
    val originalName: String?,
    val overview: String?, // Coincide con API (nulable)
    val popularity: Double?, // Coincide con API (nulable)
    val posterPath: String?,
    val status: String?,
    val tagline: String?,
    val type: String?,
    val voteAverage: Double?, // Coincide con API (nulable)
    val voteCount: Int?,     // Coincide con API (nulable)
    val numberOfEpisodes: Int?, // Coincide con API (nulable)
    val numberOfSeasons: Int?,  // Coincide con API (nulable)


    // Listas: Ahora las hacemos nulables en la entidad si el TypeConverter puede devolver null
    // y si tu modelo API también las tiene como nulables (List<Type>?)
    val createdBy: List<CreatedBy>?,
    val episodeRunTime: List<Int>?,
    val genres: List<Genre>?,
    val languages: List<String>?,
    val networks: List<Network>?,
    val originCountry: List<String>?,
    val productionCompanies: List<ProductionCompany>?,
    val productionCountries: List<ProductionCountry>?,
    val seasons: List<Season>?,
    val spokenLanguages: List<SpokenLanguage>?,

    // Objetos (ya eran nulables, lo cual está bien)
    val lastEpisodeToAir: EpisodeToAir?,
    val nextEpisodeToAir: EpisodeToAir?,

    val lastRefreshed: Long = System.currentTimeMillis()
)

