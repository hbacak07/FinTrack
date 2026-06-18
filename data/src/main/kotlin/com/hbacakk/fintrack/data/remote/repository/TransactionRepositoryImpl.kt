package com.hbacakk.fintrack.data.remote.repository

import com.hbacakk.fintrack.data.local.dao.TransactionDao
import com.hbacakk.fintrack.data.mapper.toDomain
import com.hbacakk.fintrack.data.mapper.toEntity
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.repository.MonthlySummary
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * TransactionRepository implementasyonu.
 *
 * Offline-first pattern:
 * 1. Kullanıcı işlem ekler → önce Room'a yaz
 * 2. UI anında Room'dan güncellenir (Flow)
 * 3. Arka planda API'ye sync edilir
 * 4. Internet yoksa isSynced=false kalır, sonra sync edilir
 *
 * UI her zaman Room'dan okur, asla direkt API'den değil.
 * Bu "Single Source of Truth" pattern'idir.
 */
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override fun observeTransactions(
        accountId: String?,
        type: TransactionType?,
        category: Category?,
    ): Flow<List<Transaction>> {
        val flow = when {
            accountId != null -> transactionDao.observeByAccount(accountId)
            type != null -> transactionDao.observeByType(type.name)
            else -> transactionDao.observeAll()
        }

        return flow
            .map { entities -> entities.toDomain() }
            .catch { emit(emptyList()) }
    }

    override suspend fun getTransactionById(id: String): Result<Transaction> = try {
        val entity = transactionDao.getById(id)
        if (entity != null) {
            Result.Success(entity.toDomain())
        } else {
            Result.Error(DomainException.NotFoundException())
        }
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> = try {
        val entity = transaction
            .copy(id = transaction.id.ifBlank { UUID.randomUUID().toString() })
            .toEntity(isSynced = false)
        transactionDao.insert(entity)
        Result.Success(entity.toDomain())
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> = try {
        transactionDao.update(transaction.toEntity(isSynced = false))
        Result.Success(transaction)
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> = try {
        transactionDao.deleteById(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }

    override fun observeMonthlySummary(year: Int, month: Int): Flow<MonthlySummary> {
        val yearStr = year.toString()
        val monthStr = month.toString().padStart(2, '0')

        return transactionDao.observeByMonth(yearStr, monthStr)
            .map { entities ->
                val transactions = entities.toDomain()
                val income = transactions
                    .filter { it.type == TransactionType.INCOME }
                    .sumOf { it.amount }
                val expense = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount }
                val topCategory = transactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .groupBy { it.category }
                    .maxByOrNull { it.value.sumOf { t -> t.amount } }
                    ?.key

                MonthlySummary(
                    totalIncome      = income,
                    totalExpense     = expense,
                    transactionCount = transactions.size,
                    topExpenseCategory = topCategory,
                )
            }
            .catch { emit(MonthlySummary(0.0, 0.0, 0.0, 0,null)) }
    }

    override suspend fun syncTransactions(): Result<Unit> = try {
        val unsynced = transactionDao.getUnsynced()
        // API sync — Adım 8'de Ktor backend hazır olunca implemente edilecek
        // Şimdilik başarılı kabul ediyoruz
        unsynced.forEach { transactionDao.markAsSynced(it.id) }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }
}