package com.hbacakk.fintrack.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.hbacakk.fintrack.core.ui.theme.FinTrackTheme
import com.hbacakk.fintrack.core.ui.theme.Surface
import com.hbacakk.fintrack.feature.auth.navigation.AuthDestinations
import com.hbacakk.fintrack.feature.auth.navigation.authNavGraph
import com.hbacakk.fintrack.feature.budget.navigation.budgetNavGraph
import com.hbacakk.fintrack.feature.home.navigation.HomeDestinations
import com.hbacakk.fintrack.feature.home.navigation.homeNavGraph
import com.hbacakk.fintrack.feature.transactions.navigation.transactionsNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinTrackApp()
        }
    }
}

@Composable
fun FinTrackApp(){
    FinTrackTheme {
        Surface(modifier = Modifier.fillMaxSize()){
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = AuthDestinations.AUTH_GRAPH_ROUTE
            ){
                authNavGraph(
                    navController = navController,
                    onAuthSuccess = {
                        navController.navigate(HomeDestinations.HOME_ROUTE) {
                            popUpTo(AuthDestinations.AUTH_GRAPH_ROUTE) { inclusive = true }
                        }
                    },
                )
                homeNavGraph()
                transactionsNavGraph(navController)
                budgetNavGraph(navController)

            }
        }
    }
}