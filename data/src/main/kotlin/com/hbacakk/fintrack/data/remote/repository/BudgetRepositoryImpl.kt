package com.hbacakk.fintrack.data.remote.repository

import com.hbacakk.fintrack.data.local.dao.BudgetDao
import com.hbacakk.fintrack.data.mapper.toDomain
import com.hbacakk.fintrack.data.mapper.toEntity
import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.repository.BudgetRepository
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao,
) : BudgetRepository {

    override fun observeBudgets(): Flow<List<Budget>> =
        budgetDao.observeAll()
            .map { it.toDomain() }
            .catch { emit(emptyList()) }

    override fun observeExceededBudgets(): Flow<List<Budget>> =
        budgetDao.observeExceeded()
            .map { it.toDomain() }
            .catch { emit(emptyList()) }

    override suspend fun getBudgetById(id: String): Result<Budget> = try {
        val entity = budgetDao.getById(id)
        if (entity != null) {
            Result.Success(entity.toDomain())
        } else {
            Result.Error(DomainException.NotFoundException())
        }
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override suspend fun createBudget(budget: Budget): Result<Budget> = try {
        val entity = budget
            .copy(id = budget.id.ifBlank { UUID.randomUUID().toString() })
            .toEntity(isSynced = false)
        budgetDao.insert(entity)
        Result.Success(entity.toDomain())
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override suspend fun updateBudget(budget: Budget): Result<Budget> = try {
        budgetDao.update(budget.toEntity(isSynced = false))
        Result.Success(budget)
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override suspend fun deleteBudget(id: String): Result<Unit> = try {
        budgetDao.deleteById(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }
}