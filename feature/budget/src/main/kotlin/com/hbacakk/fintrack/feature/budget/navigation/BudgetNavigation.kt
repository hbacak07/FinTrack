package com.hbacakk.fintrack.feature.budget.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.hbacakk.fintrack.feature.budget.create.CreateBudgetScreen
import com.hbacakk.fintrack.feature.budget.list.BudgetListScreen

object BudgetDestinations {
    const val LIST_ROUTE = "budget_list"
    const val CREATE_ROUTE = "budget_create"
}

fun NavGraphBuilder.budgetNavGraph(navController: NavController) {
    composable(BudgetDestinations.LIST_ROUTE) {
        BudgetListScreen(
            onAddBudgetClick = {
                navController.navigate(BudgetDestinations.CREATE_ROUTE)
            },
        )
    }

    composable(BudgetDestinations.CREATE_ROUTE) {
        CreateBudgetScreen(
            onBudgetSaved = { navController.popBackStack() },
        )
    }
}