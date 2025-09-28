package com.example.topseriesapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topseriesapp.data.database.entities.PopularTvShowEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PopularTvShowDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPopularTvShows(shows: List<PopularTvShowEntity>)

    @Query("SELECT * FROM popular_tv_shows ORDER BY api_order_index ASC")
    fun getPopularTvShows(): Flow<List<PopularTvShowEntity>>

    @Query("SELECT * FROM popular_tv_shows WHERE id = :showId")
    suspend fun getPopularTvShowById(showId: Int): PopularTvShowEntity?

    @Query("DELETE FROM popular_tv_shows")
    suspend fun deleteAllPopularTvShows()
}