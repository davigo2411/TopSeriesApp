package com.example.topseriesapp.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.topseriesapp.data.database.entities.TvShowDetailsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TvShowDetailsDao {

    /**
     * Inserta o actualiza los detalles de una serie en la base de datos.
     * Si los detalles de la serie (basado en 'id') ya existen, serán reemplazados.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTvShowDetails(details: TvShowDetailsEntity)

    /**
     * Obtiene los detalles de una serie específica por su ID.
     * Devuelve un Flow, permitiendo observar cambios en los detalles de esta serie.
     * El Flow emitirá 'null' si no se encuentran detalles para el ID dado.
     */
    @Query("SELECT * FROM tv_show_details WHERE id = :showId")
    fun getTvShowDetailsById(showId: Int): Flow<TvShowDetailsEntity?> // Nulable, puede que aún no esté en caché

    /**
     * Obtiene los detalles de una serie específica por su ID para una sola operación.
     * No devuelve un Flow, útil para una comprobación rápida o carga inicial si no se necesita observar.
     */
    @Query("SELECT * FROM tv_show_details WHERE id = :showId")
    suspend fun getTvShowDetailsByIdOnce(showId: Int): TvShowDetailsEntity? // Nulable
}

