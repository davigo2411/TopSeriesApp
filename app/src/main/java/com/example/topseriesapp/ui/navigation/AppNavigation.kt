package com.example.topseriesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.topseriesapp.ui.MainViewModel
import com.example.topseriesapp.ui.configuration.ConfigurationScreen
import com.example.topseriesapp.ui.popularshows.PopularTvShowsScreen
import com.example.topseriesapp.ui.popularshows.PopularTvShowsViewModel
import com.example.topseriesapp.ui.showsdetails.TvShowDetailsScreen
import com.example.topseriesapp.ui.showsdetails.TvShowDetailsViewModel
import com.example.topseriesapp.utils.SERIES_ID_KEY
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

// Rutas de navegación
object AppDestinations {
    const val POPULAR_SHOWS = "popularShows"
    const val CONFIGURATION = "configuration"
    const val TV_SHOW_DETAILS_ROUTE = "tvShowDetails"
    const val TV_SHOW_DETAILS_PATH = "$TV_SHOW_DETAILS_ROUTE/{$SERIES_ID_KEY}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel
) {
    val currentThemeSetting by mainViewModel.currentThemeSetting.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.POPULAR_SHOWS, // "Home" es la pantalla de shows populares
        modifier = modifier
    ) {
        composable(route = AppDestinations.POPULAR_SHOWS) {
            val popularTvShowsViewModel: PopularTvShowsViewModel = koinViewModel()
            PopularTvShowsScreen(
                viewModel = popularTvShowsViewModel,
                onNavigateToDetails = { seriesId ->
                    navController.navigate(
                        AppDestinations.TV_SHOW_DETAILS_ROUTE + "/$seriesId"
                    )
                }
            )
        }

        composable(route = AppDestinations.CONFIGURATION) {
            ConfigurationScreen(
                currentThemeSetting = currentThemeSetting,
                onThemeSettingChanged = { newSetting -> mainViewModel.updateThemeSetting(newSetting) }
            )
        }

        composable(
            route = AppDestinations.TV_SHOW_DETAILS_PATH,
            arguments = listOf(
                navArgument(SERIES_ID_KEY) {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val seriesId = backStackEntry.arguments?.getInt(SERIES_ID_KEY)
            if (seriesId != null) {
                val detailsViewModel: TvShowDetailsViewModel = koinViewModel { parametersOf(seriesId) }
                TvShowDetailsScreen(viewModel = detailsViewModel, navController = navController)
            }
        }
    }
}
