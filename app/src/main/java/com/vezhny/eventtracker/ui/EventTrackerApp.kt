package com.vezhny.eventtracker.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.vezhny.eventtracker.R
import com.vezhny.eventtracker.ui.events.EventEditScreen
import com.vezhny.eventtracker.ui.events.EventsScreen
import com.vezhny.eventtracker.ui.home.HomeScreen
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable object HomeRoute
@Serializable object EventsRoute
@Serializable data class EventEditRoute(val eventId: Long = 0)

private data class TopLevelDestination(
    val route: Any,
    val routeClass: KClass<*>,
    @StringRes val label: Int,
    val icon: ImageVector,
)

@Suppress("DEPRECATION") // Icons.Filled.List: AutoMirrored variant lives in material-icons-extended.
private val topLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, HomeRoute::class, R.string.nav_home, Icons.Filled.Home),
    TopLevelDestination(EventsRoute, EventsRoute::class, R.string.nav_events, Icons.Filled.List),
)

@Composable
fun EventTrackerApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val showBottomBar = topLevelDestinations.any { currentDestination?.hasRoute(it.routeClass) == true }

    fun navigateTopLevel(route: Any) = navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    topLevelDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(dest.routeClass) == true,
                            onClick = { navigateTopLevel(dest.route) },
                            icon = { Icon(dest.icon, contentDescription = null) },
                            label = { Text(stringResource(dest.label)) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = HomeRoute,
            modifier = Modifier.padding(padding),
        ) {
            composable<HomeRoute> { HomeScreen() }
            composable<EventsRoute> {
                EventsScreen(onEditEvent = { id -> navController.navigate(EventEditRoute(id)) })
            }
            composable<EventEditRoute> { entry ->
                EventEditScreen(
                    eventId = entry.toRoute<EventEditRoute>().eventId,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
