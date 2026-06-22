package com.hbacakk.fintrack.feature.budget.create

import com.hbacakk.fintrack.domain.model.BudgetPeriod
import com.hbacakk.fintrack.domain.model.Category

data class CreateBudgetUiState(
    val name: String = "",
    val limit: String = "",
    val selectedCategory: Category = Category.FOOD,
    val selectedPeriod: BudgetPeriod = BudgetPeriod.MONTHLY,
    val isLoading: Boolean = false,
    val nameError: String? = null,
    val limitError: String? = null,
    val isSaved: Boolean = false,
)
