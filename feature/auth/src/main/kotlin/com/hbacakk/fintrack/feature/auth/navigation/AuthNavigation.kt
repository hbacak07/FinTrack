package com.hbacakk.fintrack.feature.auth.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.hbacakk.fintrack.feature.auth.login.LoginScreen

/**
 * Auth feature'ının navigation graph'ı.
 *
 * Her feature modülü, kendi navigation graph'ını tanımlar.
 * :app modülü, bunları birleştirip tek bir NavHost oluşturur.
 * Bu sayede feature modülleri birbirini TANIMAZ — sadece
 * route string'leri üzerinden iletişim kurar.
 */
object AuthDestinations {
    const val AUTH_GRAPH_ROUTE = "auth_graph"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
}

fun NavGraphBuilder.authNavGraph(
    navController: NavController,
    onAuthSuccess: () -> Unit,
) {
    navigation(
        startDestination = AuthDestinations.LOGIN_ROUTE,
        route = AuthDestinations.AUTH_GRAPH_ROUTE,
    ) {
        composable(AuthDestinations.LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = onAuthSuccess,
                onNavigateToRegister = {
                    navController.navigate(AuthDestinations.REGISTER_ROUTE)
                },
            )
        }

        // Register ekranı sonraki adımda eklenecek
    }
}
