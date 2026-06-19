package com.hbacakk.fintrack.feature.budget.list

import com.hbacakk.fintrack.domain.model.Budget

data class BudgetListUiState(
    val isLoading: Boolean = true,
    val budgets: List<Budget> = emptyList(),
    val errorMessage: String? = null,
)