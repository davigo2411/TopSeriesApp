package com.example.topseriesapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.topseriesapp.data.preferences.ThemeDataStore
import com.example.topseriesapp.ui.configuration.ThemeSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val themeDataStore: ThemeDataStore) : ViewModel() {

    val currentThemeSetting: StateFlow<ThemeSetting> = themeDataStore.themeSetting
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeSetting.SYSTEM_DEFAULT // Un valor inicial antes de que DataStore cargue
        )

    fun updateThemeSetting(newSetting: ThemeSetting) {
        viewModelScope.launch {
            themeDataStore.setThemeSetting(newSetting)
        }
    }
}