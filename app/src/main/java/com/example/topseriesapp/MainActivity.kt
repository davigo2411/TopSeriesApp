package com.example.topseriesapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.topseriesapp.ui.popularshows.PopularTvShowsScreen
import com.example.topseriesapp.ui.popularshows.PopularTvShowsViewModel
import com.example.topseriesapp.ui.theme.TopSeriesAppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    // Obtiene el ViewModel usando Koin
    private val popularTvShowsViewModel: PopularTvShowsViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TopSeriesAppTheme {
                PopularTvShowsScreen(viewModel = popularTvShowsViewModel)
            }
        }
    }
}

