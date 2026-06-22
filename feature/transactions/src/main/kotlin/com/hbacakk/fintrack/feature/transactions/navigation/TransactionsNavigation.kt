package com.hbacakk.fintrack.feature.transactions.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hbacakk.fintrack.feature.transactions.add.AddTransactionScreen
import com.hbacakk.fintrack.feature.transactions.list.TransactionListScreen

object TransactionsDestinations {
    const val LIST_ROUTE = "transactions_list"
    const val ADD_ROUTE = "transactions_add"
}

fun NavGraphBuilder.transactionsNavGraph(navController: NavController) {
    composable(TransactionsDestinations.LIST_ROUTE) {
        TransactionListScreen(
            onAddTransactionClick = {
                navController.navigate(TransactionsDestinations.ADD_ROUTE)
            },
        )
    }

    composable(TransactionsDestinations.ADD_ROUTE) {
        AddTransactionScreen(
            onTransactionSaved = {
                navController.popBackStack()
            },
        )
    }
}
