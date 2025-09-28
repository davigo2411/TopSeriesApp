package com.example.topseriesapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.topseriesapp.ui.popularshows.PopularTvShowsScreen
import com.example.topseriesapp.utils.SERIES_ID_KEY
import org.koin.androidx.compose.koinViewModel
import com.example.topseriesapp.ui.showsdetails.TvShowDetailsScreen


// Rutas de navegación
object AppDestinations {
    const val POPULAR_SHOWS = "popularShows"
    const val TV_SHOW_DETAILS_ROUTE = "tvShowDetails"
    const val TV_SHOW_DETAILS_PATH = "$TV_SHOW_DETAILS_ROUTE/{$SERIES_ID_KEY}"
}

@Composable
fun AppNavGraph(
    navController: androidx.navigation.NavHostController = androidx.navigation.compose.rememberNavController(),
    startDestination: String = AppDestinations.POPULAR_SHOWS
){
    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        composable(route = AppDestinations.POPULAR_SHOWS) {
            PopularTvShowsScreen(
                viewModel = koinViewModel(),
                onNavigateToDetails = { seriesId ->
                    navController.navigate("${AppDestinations.TV_SHOW_DETAILS_ROUTE}/$seriesId")
                }
            )
        }

        composable(
            route = AppDestinations.TV_SHOW_DETAILS_PATH,
            arguments = listOf(
                navArgument(SERIES_ID_KEY) {
                    type = NavType.IntType
                }
            )
        ) {
            TvShowDetailsScreen(
                navController = navController
            )
        }
    }

}