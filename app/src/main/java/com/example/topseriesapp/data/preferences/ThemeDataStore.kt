package com.example.topseriesapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.topseriesapp.ui.configuration.ThemeSetting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemeDataStore(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        val THEME_KEY = stringPreferencesKey("theme_preference")
    }

    val themeSetting: Flow<ThemeSetting> = appContext.dataStore.data
        .map { preferences ->
            val themeName = preferences[THEME_KEY] ?: ThemeSetting.SYSTEM_DEFAULT.name
            try {
                ThemeSetting.valueOf(themeName)
            } catch (e: IllegalArgumentException) {
                ThemeSetting.SYSTEM_DEFAULT
            }
        }

    suspend fun setThemeSetting(theme: ThemeSetting) {
        appContext.dataStore.edit { settings ->
            settings[THEME_KEY] = theme.name
        }
    }
}

