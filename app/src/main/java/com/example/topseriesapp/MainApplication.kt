package com.example.topseriesapp

import android.app.Application
import com.example.topseriesapp.di.appModule
import com.example.topseriesapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            androidLogger(Level.DEBUG)
            modules(appModule, viewModelModule)
        }
    }
}