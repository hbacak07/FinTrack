package com.hbacakk.fintrack.domain.usecase.transaction

import com.hbacakk.fintrack.domain.repository.MonthlySummary
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.FlowUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveMonthlySummaryUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FlowUseCase<ObserveMonthlySummaryUseCase.Params, MonthlySummary>(dispatcher) {

    data class Params(val year: Int, val month: Int)

    override fun execute(params: Params): Flow<Result<MonthlySummary>> =
        transactionRepository.observeMonthlySummary(params.year, params.month)
            .map { Result.Success(it) }
}