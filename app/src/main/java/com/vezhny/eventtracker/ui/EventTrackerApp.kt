package com.vezhny.eventtracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vezhny.eventtracker.ui.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable object HomeRoute

@Composable
fun EventTrackerApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = HomeRoute) {
        composable<HomeRoute> { HomeScreen() }
    }
}
