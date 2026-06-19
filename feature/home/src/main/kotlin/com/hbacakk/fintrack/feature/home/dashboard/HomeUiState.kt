package com.hbacakk.fintrack.feature.home.dashboard

import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.repository.MonthlySummary

data class HomeUiState(
    val isLoading: Boolean = true,
    val monthlySummary: MonthlySummary? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val errorMessage: String? = null,
) {
    val hasExceededBudgets: Boolean
        get() = budgets.any { it.isExceeded }
}