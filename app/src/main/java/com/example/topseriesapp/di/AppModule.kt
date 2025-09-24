package com.example.topseriesapp.di

import com.example.topseriesapp.data.network.TMDBApiService
import org.koin.dsl.module
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val BASE_URL = "https://api.themoviedb.org/3/"

val appModule = module {

    // Definición para OkHttpClient
    single {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // Establece la URL base para todas las llamadas de esta instancia de Retrofit
            .client(get())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //Definición para TMBDApiService
    single{
        get<Retrofit>().create(TMDBApiService::class.java)
    }
    // Más definiciones vendrán aquí...
}

val viewModelModule = module {
    // Aquí definiremos nuestros ViewModels
}