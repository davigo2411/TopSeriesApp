package com.example.topseriesapp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.topseriesapp.R
import com.example.topseriesapp.ui.navigation.AppNavGraph
import com.example.topseriesapp.ui.navigation.AppDestinations

/**
 * Define la estructura de un ítem de la barra de navegación inferior.
 */
data class BottomNavItem(
    val route: String,
    val labelResId: Int,
    val iconPainterResId: Int
)

// Lista de ítems para la barra de navegación inferior.
val bottomNavItemsList = listOf(
    BottomNavItem(
        route = AppDestinations.POPULAR_SHOWS,
        labelResId = R.string.bottom_nav_home,
        iconPainterResId = R.drawable.ic_home
    ),
    BottomNavItem(
        route = AppDestinations.CONFIGURATION,
        labelResId = R.string.bottom_nav_configuration,
        iconPainterResId = R.drawable.ic_settings
    )
)

/**
 * Composable principal que estructura la pantalla de la aplicación.
 * Incluye una barra de navegación inferior y el grafo de navegación para el contenido.
 *
 * @param mainViewModel ViewModel principal para acceder a estados globales y acciones.
 * @param onLanguageChanged Callback invocado cuando se necesita recrear la actividad por cambio de idioma.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    mainViewModel: MainViewModel,
    onLanguageChanged: () -> Unit
) {
    val navController = rememberNavController() // Controlador para la navegación Compose.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination // Destino actual en la navegación.

    Scaffold(
        bottomBar = {
            NavigationBar { // Barra de navegación inferior de Material 3.
                bottomNavItemsList.forEach { item ->
                    // Determina si el ítem actual está seleccionado en la jerarquía de navegación.
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = item.iconPainterResId),
                                contentDescription = stringResource(item.labelResId),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(item.labelResId),
                                fontSize = 10.sp
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors( // Personaliza los colores del ítem.
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        // Contenedor para el contenido de las diferentes pantallas.
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding),
            mainViewModel = mainViewModel,
            onLanguageChanged = onLanguageChanged
        )
    }
}
