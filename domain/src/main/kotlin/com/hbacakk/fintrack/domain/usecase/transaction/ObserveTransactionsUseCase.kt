package com.hbacakk.fintrack.domain.usecase.transaction

import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.FlowUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ObserveTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : FlowUseCase<ObserveTransactionsUseCase.Params, List<Transaction>>(dispatcher) {

    data class Params(
        val accountId: String? = null,
        val type: TransactionType? = null,
        val category: Category? = null,
    )

    override fun execute(params: Params): Flow<Result<List<Transaction>>> =
        transactionRepository.observeTransactions(
            accountId = params.accountId,
            type      = params.type,
            category  = params.category,
        ).map { Result.Success(it) }
}