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

/**
 * Objeto ayudante para gestionar la configuración de idioma (Locale) de la aplicación.
 * Permite persistir la selección de idioma del usuario y aplicarla.
 */
object LocaleHelper {

    // Claves para SharedPreferences
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"
    const val LANGUAGE_CHANGED_FLAG = "Locale.Helper.Language.Changed.Flag" // Usada para señalar un cambio de idioma
    private const val PREFS_NAME = "AppSettingsPrefs"

    // Lista de códigos de idioma soportados por la aplicación.
    val supportedLanguages = listOf("en", "es")

    /**
     * Se llama desde `Activity.attachBaseContext()` para envolver el contexto base
     * con la configuración de idioma seleccionada o por defecto.
     *
     * @param context El contexto base de la Activity.
     * @return Un ContextWrapper con el Locale actualizado.
     */
    fun onAttach(context: Context): ContextWrapper {
        val currentLang = getPersistedLocale(context)
        return updateLocaleConfiguration(context, Locale(currentLang))
    }

    /**
     * Obtiene el código de idioma persistido por el usuario.
     * Si no hay ninguno, intenta usar el idioma del sistema si es soportado.
     * Como último recurso, devuelve "en" (inglés).
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @return El código de idioma (ej. "en", "es") a usar.
     */
    fun getPersistedLocale(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val systemLang = getSystemLocale().language
        val persistedLang = prefs.getString(SELECTED_LANGUAGE, null)

        return persistedLang
            ?: if (systemLang in supportedLanguages) {
                systemLang
            } else {
                "en" // Idioma por defecto si el del sistema no es soportado o no hay persistido.
            }
    }

    /**
     * Establece el nuevo idioma para la aplicación.
     * Guarda la selección, activa una bandera para notificar el cambio
     * y actualiza el Locale a nivel de aplicación usando AppCompatDelegate.
     *
     * @param context Contexto para acceder a SharedPreferences.
     * @param languageCode El nuevo código de idioma a establecer (ej. "en", "es").
     */
    fun setLocale(context: Context, languageCode: String) {
        persistLocalePreference(context, languageCode)
        updateApplicationLocale(languageCode)

        // Establece una bandera para indicar que el idioma ha cambiado.
        // Esto puede ser usado por otras partes de la app (ej. repositorios) para reaccionar.
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(LANGUAGE_CHANGED_FLAG, true) }
    }

    /**
     * Guarda el código de idioma seleccionado en SharedPreferences.
     */
    private fun persistLocalePreference(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putString(SELECTED_LANGUAGE, languageCode) }
    }

    /**
     * Actualiza el Locale de la aplicación usando AppCompatDelegate.
     * Esto afecta a cómo se cargan los recursos en futuras recreaciones de Activity/Context.
     */
    private fun updateApplicationLocale(languageCode: String) {
        val locale = Locale(languageCode)
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    /**
     * Actualiza la configuración del contexto proporcionado con el nuevo Locale.
     * Este metodo es usado principalmente por `onAttach` para el contexto inicial de la Activity.
     */
    private fun updateLocaleConfiguration(context: Context, locale: Locale): ContextWrapper {
        Locale.setDefault(locale) // Establece el Locale por defecto para la JVM.
        val resources: Resources = context.resources
        val configuration = Configuration(resources.configuration)

        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList) // Establece el LocaleList por defecto para la JVM.
        configuration.setLocales(localeList)

        // Crea un nuevo contexto con la configuración actualizada o actualiza el existente.
        val newContext =
            context.createConfigurationContext(configuration)
        return ContextWrapper(newContext)
    }

    /**
     * Obtiene el Locale primario configurado en el sistema operativo del dispositivo.
     */
    @SuppressLint("ObsoleteSdkInt")
    private fun getSystemLocale(): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            Resources.getSystem().configuration.locale
        }
    }

    /**
     * Obtiene el Locale actual de la aplicación según lo gestiona AppCompatDelegate.
     * Si AppCompatDelegate no tiene un Locale específico, recurre al persistido.
     *
     * @param context Contexto para acceder al Locale persistido si es necesario.
     * @return El Locale actual de la aplicación.
     */
    fun getCurrentAppLocale(context: Context): Locale {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return if (!appLocales.isEmpty) {
            appLocales.get(0) ?: Locale(getPersistedLocale(context)) // Si get(0) es null, usa el persistido
        } else {
            Locale(getPersistedLocale(context)) // Si está vacío, usa el persistido
        }
    }
}

