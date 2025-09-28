package com.example.topseriesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.topseriesapp.ui.MainAppScreen
import com.example.topseriesapp.ui.MainViewModel
import com.example.topseriesapp.ui.configuration.ThemeSetting
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val mainViewModel: MainViewModel = getViewModel()
            val currentThemeSetting by mainViewModel.currentThemeSetting.collectAsStateWithLifecycle()

            val useDarkTheme = when (currentThemeSetting) {
                ThemeSetting.LIGHT -> false
                ThemeSetting.DARK -> true
                ThemeSetting.SYSTEM_DEFAULT -> isSystemInDarkTheme()
            }

            TopSeriesAppTheme(darkTheme = useDarkTheme) {
                // Llama a MainAppScreen, que contendrá el Scaffold principal y AppNavGraph
                MainAppScreen(mainViewModel = mainViewModel)
            }
        }
    }
}