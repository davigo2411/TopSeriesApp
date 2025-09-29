package com.example.topseriesapp.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.topseriesapp.data.database.converters.AppTypeConverters

@Entity(tableName = "popular_tv_shows")
@TypeConverters(AppTypeConverters::class)
data class PopularTvShowEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val voteAverage: Double,
    val firstAirDate: String?,
    val voteCount: Int,
    val genreIds: List<Int>?,
    val originCountry: List<String>?,
    val originalLanguage: String?,
    val originalName: String?,
    val popularity: Double?,

    @ColumnInfo(name = "api_order_index")
    var apiOrderIndex: Int = 0,

    val lastRefreshed: Long = System.currentTimeMillis()
)

