package com.hbacakk.fintrack.domain.usecase.budget

import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.repository.BudgetRepository
import com.hbacakk.fintrack.domain.usecase.FlowUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveBudgetsUseCase(
    private val budgetRepository: BudgetRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FlowUseCase<Unit, List<Budget>>(dispatcher) {
    override fun execute(params: Unit): Flow<Result<List<Budget>>> =
        budgetRepository
            .observeBudgets()
            .map { Result.Success(it) }
}
