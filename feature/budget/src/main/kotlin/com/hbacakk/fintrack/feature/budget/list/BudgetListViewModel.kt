package com.hbacakk.fintrack.feature.budget.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.usecase.budget.ObserveBudgetsUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class BudgetListViewModel(
    observeBudgetsUseCase: ObserveBudgetsUseCase,
) : ViewModel() {

    val uiState: StateFlow<BudgetListUiState> = observeBudgetsUseCase(Unit)
        .map { result ->
            when (result) {
                is Result.Success -> BudgetListUiState(
                    isLoading = false,
                    budgets = result.data,
                )
                is Result.Error -> BudgetListUiState(
                    isLoading = false,
                    errorMessage = result.exception.message,
                )
                is Result.Loading -> BudgetListUiState(isLoading = true)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetListUiState(),
        )
}
