package com.hbacakk.fintrack.domain.repository

import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgets(): Flow<List<Budget>>

    suspend fun getBudgetById(id: String): Result<Budget>

    suspend fun createBudget(budget: Budget): Result<Budget>

    suspend fun updateBudget(budget: Budget): Result<Budget>

    suspend fun deleteBudget(id: String): Result<Unit>

    fun observeExceededBudgets(): Flow<List<Budget>>
}
