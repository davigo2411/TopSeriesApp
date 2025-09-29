package com.example.topseriesapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.preferences.ThemeDataStore
import com.example.topseriesapp.ui.configuration.ThemeSetting
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel principal para gestionar estados y eventos globales de la UI.
 * Incluye la configuración del tema y notificaciones de cambio de idioma.
 */
class MainViewModel(private val themeDataStore: ThemeDataStore) : ViewModel() {

    // Configuración actual del tema, obtenida de ThemeDataStore.
    val currentThemeSetting: StateFlow<ThemeSetting> = themeDataStore.themeSetting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.SYSTEM_DEFAULT
        )

    // --- Evento de cambio de idioma ---
    private val _languageChangedEvent = MutableSharedFlow<Unit>(replay = 1, extraBufferCapacity = 1)

    private val _languageUpdateTrigger = MutableStateFlow(0) // Trigger para cambios de idioma.
    val languageUpdateTrigger: StateFlow<Int> = _languageUpdateTrigger.asStateFlow()

    /**
     * Notifica un cambio de idioma en la aplicación.
     */
    fun signalLanguageChange() {
        _languageUpdateTrigger.value += 1
        _languageChangedEvent.tryEmit(Unit)
    }
    // --- Fin evento de cambio de idioma ---

    /**
     * Actualiza y persiste la configuración del tema.
     */
    fun updateThemeSetting(newSetting: ThemeSetting) {
        viewModelScope.launch {
            themeDataStore.setThemeSetting(newSetting)
        }
    }
}
