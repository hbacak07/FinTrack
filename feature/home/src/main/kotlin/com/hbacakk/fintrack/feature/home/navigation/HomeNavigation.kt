package com.hbacakk.fintrack.feature.home.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hbacakk.fintrack.feature.home.dashboard.HomeScreen

object HomeDestinations {
    const val HOME_ROUTE = "home"
}

fun NavGraphBuilder.homeNavGraph() {
    composable(HomeDestinations.HOME_ROUTE) {
        HomeScreen()
    }
}
