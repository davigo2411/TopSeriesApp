package com.example.topseriesapp.di

import com.example.topseriesapp.data.database.AppDatabase
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.coroutines.DefaultCoroutineDispatchers
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.data.repository.TvShowRepositoryImpl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.ui.popularshows.PopularTvShowsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import androidx.lifecycle.SavedStateHandle
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCase
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCaseImpl
import com.example.topseriesapp.ui.showsdetails.ConnectivityChecker
import com.example.topseriesapp.ui.showsdetails.TvShowDetailsViewModel
import com.example.topseriesapp.data.connectivity.NetworkConnectivityChecker

private const val BASE_URL = "https://api.themoviedb.org/3/"

val appModule = module {

    // Definición para OkHttpClient
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>())
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //Definición para TMBDApiService
    single{
        get<Retrofit>().create(TMDBApiService::class.java)
    }


    single<CoroutineDispatchers> {DefaultCoroutineDispatchers() }

    single<TvShowRepository> {
        TvShowRepositoryImpl(
            apiService = get(),
            popularTvShowDao = get(),
            tvShowDetailsDao = get(),
            dispatchers = get()
        )
    }

    // Definición de los Casos de Uso
    factory {
        GetPopularTvShowsUseCase(get())
    }

    factory<GetTvShowDetailsUseCase> {
        GetTvShowDetailsUseCaseImpl(get())
    }
}

val databaseModule = module {

    single {
        AppDatabase.getDatabase(androidApplication())
    }

    single {
        val database = get<AppDatabase>()
        database.popularTvShowDao()
    }


    single {
        val database = get<AppDatabase>()
        database.tvShowDetailsDao()
    }
}

val utilsModule = module {
    single<ConnectivityChecker> {
        NetworkConnectivityChecker(androidApplication())
    }
}

val viewModelModule = module {
    viewModel {
        PopularTvShowsViewModel(getPopularTvShowsUseCase = get())
    }

    viewModel { (handle: SavedStateHandle) ->
        TvShowDetailsViewModel(
            getTvShowDetailsUseCase = get(),
            savedStateHandle = handle,
            connectivityChecker = get()
        )
    }
}

// Lista de todos los modulos de la aplicación
val allAppModules = listOf(appModule, viewModelModule, databaseModule, utilsModule)