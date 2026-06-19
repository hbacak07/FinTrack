package com.hbacakk.fintrack.domain.usecase.transaction

import com.hbacakk.fintrack.domain.repository.MonthlySummary
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.FlowUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveMonthlySummaryUseCase(
    private val transactionRepository: TransactionRepository,
) : FlowUseCase<ObserveMonthlySummaryUseCase.Params, MonthlySummary>(Dispatchers.IO) {

    data class Params(val year: Int, val month: Int)

    override fun execute(params: Params): Flow<Result<MonthlySummary>> =
        transactionRepository.observeMonthlySummary(params.year, params.month)
            .map { Result.Success(it) }
}