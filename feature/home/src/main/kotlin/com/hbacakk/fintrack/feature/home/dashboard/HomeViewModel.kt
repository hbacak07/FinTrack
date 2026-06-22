package com.hbacakk.fintrack.feature.home.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.usecase.budget.ObserveBudgetsUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveMonthlySummaryUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveTransactionsUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * HomeViewModel — dashboard'ın beyni.
 *
 * Üç farklı Flow'u (özet, işlemler, bütçeler) combine() ile
 * tek bir UI state'e birleştiriyoruz.
 *
 * Neden combine()?
 * Her Flow ayrı ayrı collect edilseydi, 3 farklı yerden
 * state güncellemesi gelirdi — yarış durumları (race condition)
 * ve tutarsız UI riski olurdu. combine(), tüm kaynaklar
 * her güncellendiğinde TEK bir state objesi üretir.
 */
class HomeViewModel(
    observeMonthlySummaryUseCase: ObserveMonthlySummaryUseCase,
    observeTransactionsUseCase: ObserveTransactionsUseCase,
    observeBudgetsUseCase: ObserveBudgetsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH 0-indexli

        viewModelScope.launch {
            combine(
                observeMonthlySummaryUseCase(
                    ObserveMonthlySummaryUseCase.Params(year, month)
                ),
                observeTransactionsUseCase(ObserveTransactionsUseCase.Params()),
                observeBudgetsUseCase(Unit),
            ) { summaryResult, transactionsResult, budgetsResult ->
                buildUiState(summaryResult, transactionsResult, budgetsResult)
            }.collect { newState ->
                _uiState.update { newState }
            }
        }
    }

    private fun buildUiState(
        summaryResult: Result<com.hbacakk.fintrack.domain.repository.MonthlySummary>,
        transactionsResult: Result<List<com.hbacakk.fintrack.domain.model.Transaction>>,
        budgetsResult: Result<List<com.hbacakk.fintrack.domain.model.Budget>>,
    ): HomeUiState {
        // Herhangi biri hata verirse, genel bir hata state'i göster
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
