package com.example.topseriesapp

import android.app.Application
import com.example.topseriesapp.di.appModule // Asegúrate que la ruta sea correcta
import com.example.topseriesapp.di.viewModelModule // Asegúrate que la ruta sea correcta
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            // Usar el contexto de Android
            androidContext(this@MainApplication)
            // Habilitar el logger de Koin (opcional)
            androidLogger(Level.DEBUG)
            // Cargar los módulos de Koin
            modules(appModule, viewModelModule)
        }
    }
}