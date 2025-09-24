package com.example.topseriesapp.di

import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.coroutines.DefaultCoroutineDispatchers
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.data.repository.TvShowRepositoryImpl
import org.koin.dsl.module
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.koin.core.qualifier.named
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase

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


    single<CoroutineDispatchers> {DefaultCoroutineDispatchers() }

    single<TvShowRepository>{
        TvShowRepositoryImpl(
            tmdbApiService = get(),
            dispatchers = get(named("IODispacther"))
        )
    }

    // Definición del Caso de Uso
    factory {
        GetPopularTvShowsUseCase(get()) // Koin inyecta TvShowRepository
    }
}

val viewModelModule = module {

}