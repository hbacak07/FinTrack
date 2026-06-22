package com.hbacakk.fintrack.domain.usecase.budget

import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.repository.BudgetRepository
import com.hbacakk.fintrack.domain.usecase.UseCase
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Bu use case, dispatcher injection pattern'ini DOĞRU şekilde
 * uyguluyor — Adım 8'de öğrendiğimiz dersi burada baştan doğru yapıyoruz.
 */
class CreateBudgetUseCase(
    private val budgetRepository: BudgetRepository,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : UseCase<Budget, Budget>(dispatcher) {
    override suspend fun execute(params: Budget): Result<Budget> {
        if (params.limit <= 0) {
            return Result.Error(
                DomainException.ValidationException("limit", "Bütçe limiti sıfırdan büyük olmalı"),
            )
        }
        if (params.name.isBlank()) {
            return Result.Error(
                DomainException.ValidationException("name", "Bütçe adı boş olamaz"),
            )
        }

        return budgetRepository.createBudget(params)
    }
}
