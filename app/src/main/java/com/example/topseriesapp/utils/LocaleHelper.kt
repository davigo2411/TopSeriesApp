@file:Suppress("DEPRECATION")

package com.example.topseriesapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import androidx.core.content.edit

object LocaleHelper {

    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    private val supportedLanguages = listOf("en", "es")

    fun onAttach(context: Context): ContextWrapper {
        val currentLang = getPersistedLocale(context)
        return updateLocale(context, Locale(currentLang))
    }

    private fun getPersistedLocale(context: Context): String {
        val prefs = context.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        // Devuelve el idioma guardado, o el idioma del sistema si es soportado, o inglés por defecto
        val systemLang = getSystemLocale().language
        return prefs.getString(SELECTED_LANGUAGE, null)
            ?: if (systemLang in supportedLanguages) systemLang else "en"
    }

    fun setLocale(context: Context, languageCode: String) {
        persistLocale(context, languageCode)
        updateAppLocale(languageCode)
    }

    private fun persistLocale(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("AppSettingsPrefs", Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_LANGUAGE, languageCode) }
    }

    private fun updateAppLocale(languageCode: String) {
        val locale = Locale(languageCode)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun updateLocale(context: Context, locale: Locale): ContextWrapper {
        Locale.setDefault(locale)
        val res: Resources = context.resources
        val config = Configuration(res.configuration)

        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)
        val newContext =
            context.createConfigurationContext(config)
        return ContextWrapper(newContext)
    }

    @SuppressLint("ObsoleteSdkInt")
    fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
    }

    fun getCurrentAppLocale(context: Context): Locale {
        val currentLangTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        return if (currentLangTag.isNotBlank() && currentLangTag != "und") {
            Locale.forLanguageTag(currentLangTag.split(',')[0])
        } else {
            Locale(getPersistedLocale(context))
        }
    }
}
