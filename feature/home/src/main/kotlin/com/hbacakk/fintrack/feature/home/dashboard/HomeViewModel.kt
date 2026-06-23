package com.hbacakk.fintrack.feature.home.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.usecase.budget.ObserveBudgetsUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveMonthlySummaryUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveTransactionsUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.SyncTransactionsUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(
    observeMonthlySummaryUseCase: ObserveMonthlySummaryUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeBudgetsUseCase: ObserveBudgetsUseCase,
    private val syncTransactionsUseCase: SyncTransactionsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1

        viewModelScope.launch {
            // Önce sync'i bitir, SONRA Room'u gözlemlemeye başla
            val syncResult = syncTransactionsUseCase(Unit)
            Log.d("FinTrackSync", "Sync result: $syncResult")

            combine(
                observeMonthlySummaryUseCase(
                    ObserveMonthlySummaryUseCase.Params(year, month)
                ),
                observeTransactionsUseCase(ObserveTransactionsUseCase.Params()),
                observeBudgetsUseCase(Unit),
            ) { summaryResult, transactionsResult, budgetsResult ->
                buildUiState(summaryResult, transactionsResult, budgetsResult)
            }.collect { newState ->
                Log.d(
                    "FinTrackHome",
                    "New state: transactions=${newState.recentTransactions.size}, " +
                            "summary=${newState.monthlySummary}, " +
                            "error=${newState.errorMessage}",
                )
                _uiState.update { newState }
            }
        }
    }

    private fun buildUiState(
        summaryResult: Result<com.hbacakk.fintrack.domain.repository.MonthlySummary>,
        transactionsResult: Result<List<com.hbacakk.fintrack.domain.model.Transaction>>,
        budgetsResult: Result<List<com.hbacakk.fintrack.domain.model.Budget>>,
    ): HomeUiState {
        val error = listOf(summaryResult, transactionsResult, budgetsResult)
            .filterIsInstance<Result.Error>()
            .firstOrNull()

        if (error != null) {
            return HomeUiState(isLoading = false, errorMessage = error.exception.message)
        }

        val summary = (summaryResult as? Result.Success)?.data
        val transactions = (transactionsResult as? Result.Success)?.data.orEmpty()
        val budgets = (budgetsResult as? Result.Success)?.data.orEmpty()

        return HomeUiState(
            isLoading = false,
            monthlySummary = summary,
            recentTransactions = transactions.take(5),
            budgets = budgets,
        )
    }
}