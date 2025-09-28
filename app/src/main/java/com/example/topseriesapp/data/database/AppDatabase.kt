package com.example.topseriesapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.topseriesapp.data.database.converters.AppTypeConverters
import com.example.topseriesapp.data.database.dao.PopularTvShowDao
import com.example.topseriesapp.data.database.dao.TvShowDetailsDao
import com.example.topseriesapp.data.database.entities.PopularTvShowEntity
import com.example.topseriesapp.data.database.entities.TvShowDetailsEntity

@Database(
    entities = [
        PopularTvShowEntity::class,
        TvShowDetailsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class) // Registra TypeConverters a nivel de base de datos
abstract class AppDatabase : RoomDatabase() {


    abstract fun popularTvShowDao(): PopularTvShowDao
    abstract fun tvShowDetailsDao(): TvShowDetailsDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "top_series_app_database"
                )

                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

