package com.hbacakk.fintrack.domain.usecase.transaction

import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.UseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class SyncTransactionsUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UseCase<Unit, Unit>(dispatcher) {

    override suspend fun execute(params: Unit): Result<Unit> =
        transactionRepository.syncTransactions()
}