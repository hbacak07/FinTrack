package com.hbacakk.fintrack.domain.usecase.transaction

import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.UseCase
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AddTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UseCase<Transaction, Transaction>(dispatcher) {
    override suspend fun execute(params: Transaction): Result<Transaction> {
        if (params.amount <= 0) {
            return Result.Error(
                DomainException.ValidationException("amount", "Tutar sıfırdan büyük olmalı"),
            )
        }
        if (params.description.isBlank()) {
            return Result.Error(
                DomainException.ValidationException("description", "Açıklama boş olamaz"),
            )
        }

        return transactionRepository.addTransaction(params)
    }
}
