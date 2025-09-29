package com.example.topseriesapp.di

import androidx.lifecycle.SavedStateHandle
import com.example.topseriesapp.coroutines.CoroutineDispatchers
import com.example.topseriesapp.coroutines.DefaultCoroutineDispatchers
import com.example.topseriesapp.data.connectivity.NetworkConnectivityChecker
import com.example.topseriesapp.data.database.AppDatabase
import com.example.topseriesapp.data.network.TMDBApiService
import com.example.topseriesapp.data.preferences.ThemeDataStore
import com.example.topseriesapp.data.repository.TvShowRepository
import com.example.topseriesapp.data.repository.TvShowRepositoryImpl
import com.example.topseriesapp.domain.usecase.GetPopularTvShowsUseCase
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCase
import com.example.topseriesapp.domain.usecase.GetTvShowDetailsUseCaseImpl
import com.example.topseriesapp.ui.MainViewModel
import com.example.topseriesapp.ui.popularshows.PopularTvShowsViewModel
import com.example.topseriesapp.ui.showsdetails.ConnectivityChecker
import com.example.topseriesapp.ui.showsdetails.TvShowDetailsViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// URL base para el servicio de API de The Movie Database (TMDB)
private const val BASE_URL = "https://api.themoviedb.org/3/"

/**
 * Módulo principal de Koin para las dependencias de la capa de datos (red, repositorios)
 * y casos de uso.
 */
val appModule = module {

    // Provee una instancia única (singleton) de ThemeDataStore para gestionar las preferencias del tema.
    single { ThemeDataStore(androidContext()) }

    // Configuración de red con Retrofit y OkHttp.
    // Provee un interceptor para logging de las peticiones HTTP (solo útil en debug).
    single {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    // Provee el cliente OkHttp con el interceptor de logging.
    single {
        OkHttpClient.Builder()
            .addInterceptor(get<HttpLoggingInterceptor>()) // Obtiene el interceptor definido arriba
            .build()
    }
    // Provee la instancia de Retrofit configurada.
    single {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(get<OkHttpClient>()) // Obtiene el OkHttpClient definido arriba
            .addConverterFactory(GsonConverterFactory.create()) // Usa Gson para convertir JSON
            .build()
    }
    // Provee la implementación del servicio de API (TMDBApiService) creado por Retrofit.
    single {
        get<Retrofit>().create(TMDBApiService::class.java) // Obtiene Retrofit y crea el servicio
    }

    // Provee una implementación para los dispatchers de corutinas.
    single<CoroutineDispatchers> { DefaultCoroutineDispatchers() }

    // Provee la implementación del repositorio para los shows de TV.
    single<TvShowRepository> {
        TvShowRepositoryImpl(
            apiService = get(), // Inyecta TMDBApiService
            popularTvShowDao = get(), // Inyecta PopularTvShowDao (del databaseModule)
            tvShowDetailsDao = get(), // Inyecta TvShowDetailsDao (del databaseModule)
            dispatchers = get(), // Inyecta CoroutineDispatchers
            applicationContext = androidContext() // Provee el contexto de la aplicación
        )
    }

    // Definiciones para los Casos de Uso.
    // 'factory' crea una nueva instancia cada vez que se solicita.
    factory { GetPopularTvShowsUseCase(get()) } // Inyecta TvShowRepository
    factory<GetTvShowDetailsUseCase> { GetTvShowDetailsUseCaseImpl(get()) } // Inyecta TvShowRepository
}

/**
 * Módulo de Koin para las dependencias de la base de datos Room.
 */
val databaseModule = module {

    // Provee la instancia única de la base de datos AppDatabase.
    single { AppDatabase.getDatabase(androidApplication()) }

    // Provee el DAO para los shows populares.
    single {
        val database = get<AppDatabase>() // Obtiene la instancia de la BD
        database.popularTvShowDao()
    }

    // Provee el DAO para los detalles de los shows.
    single {
        val database = get<AppDatabase>() // Obtiene la instancia de la BD
        database.tvShowDetailsDao()
    }
}

/**
 * Módulo de Koin para utilidades varias de la aplicación.
 */
val utilsModule = module {
    // Provee una implementación para verificar la conectividad de red.
    single<ConnectivityChecker> { NetworkConnectivityChecker(androidApplication()) }
}

/**
 * Módulo de Koin para los ViewModels de la aplicación.
 */
val viewModelModule = module {

    // MainViewModel es un singleton para mantener su estado y eventos (como el cambio de idioma)
    // a través de recreaciones de Activity/Fragmentos.
    single { MainViewModel(get()) } // Inyecta ThemeDataStore

    // PopularTvShowsViewModel se crea como factory (nueva instancia cada vez)
    // y recibe la instancia singleton de MainViewModel.
    viewModel {
        PopularTvShowsViewModel(
            getPopularTvShowsUseCase = get(), // Inyecta GetPopularTvShowsUseCase
            mainViewModel = get()             // Inyecta el MainViewModel singleton
        )
    }

    // TvShowDetailsViewModel se crea como factory y recibe un SavedStateHandle
    // para gestionar el estado guardado, además de otras dependencias.
    viewModel { (handle: SavedStateHandle) ->
        TvShowDetailsViewModel(
            getTvShowDetailsUseCase = get(), // Inyecta GetTvShowDetailsUseCase
            savedStateHandle = handle,
            connectivityChecker = get()      // Inyecta ConnectivityChecker
        )
    }
}

/**
 * Lista que agrupa todos los módulos de Koin para ser iniciados en la clase Application.
 */
val allAppModules = listOf(appModule, viewModelModule, databaseModule, utilsModule)

