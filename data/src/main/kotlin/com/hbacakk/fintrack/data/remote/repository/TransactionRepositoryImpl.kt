package com.hbacakk.fintrack.data.remote.repository

import com.hbacakk.fintrack.core.network.api.TransactionApi
import com.hbacakk.fintrack.core.network.model.CreateTransactionRequest
import com.hbacakk.fintrack.data.local.dao.TransactionDao
import com.hbacakk.fintrack.data.local.entity.TransactionEntity
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
 *
 * syncTransactions(): backend'den gelen listeyi Room'a yazar
 * (pull sync). Bu, dummy/seed data gibi backend'de var olan
 * ama Room'da olmayan kayıtların UI'da görünmesini sağlar.
 */
class TransactionRepositoryImpl(
    private val transactionDao: TransactionDao,
    private val transactionApi: TransactionApi,
) : TransactionRepository {

    override fun observeTransactions(
        accountId: String?,
        type: TransactionType?,
        category: Category?,
    ): Flow<List<Transaction>> {
        android.util.Log.d("FinTrackSync", "observeTransactions called: accountId=$accountId, type=$type")
        val flow = when {
            accountId != null -> transactionDao.observeByAccount(accountId)
            type != null -> transactionDao.observeByType(type.name)
            else -> transactionDao.observeAll()
        }

        return flow
            .map { entities ->
                android.util.Log.d("FinTrackSync", "Room emitted ${entities.size} entities")
                entities.toDomain()
            }
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

        // Backend'e de yazmayı dene — başarısız olursa isSynced=false kalır,
        // ileride syncTransactions() ile tekrar denenebilir.
        try {
            transactionApi.createTransaction(
                CreateTransactionRequest(
                    amount = entity.amount,
                    type = entity.type,
                    category = entity.category,
                    description = entity.description,
                    date = entity.date,
                    accountId = entity.accountId,
                ),
            )
            transactionDao.markAsSynced(entity.id)
        } catch (e: Exception) {
            // Network yoksa sessizce devam et — local veri zaten kaydedildi
        }

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
                    totalIncome = income,
                    totalExpense = expense,
                    transactionCount = transactions.size,
                    topExpenseCategory = topCategory,
                )
            }
            .catch { emit(MonthlySummary(0.0, 0.0, 0.0, 0, null)) }
    }

    /**
     * Pull sync: backend'deki tüm transaction'ları çekip Room'a yazar.
     *
     * Gerçek bir production uygulamasında bu fonksiyon "son sync
     * zamanından sonra değişenler" gibi incremental bir strateji
     * kullanır — burada öğrenme/demo amaçlı basit bir "tümünü çek,
     * üzerine yaz" (upsert) stratejisi uyguluyoruz.
     */
    override suspend fun syncTransactions(): Result<Unit> = try {
        val remoteTransactions = transactionApi.getTransactions()

        val entities = remoteTransactions.map { dto ->
            TransactionEntity(
                id = dto.id,
                amount = dto.amount,
                type = dto.type,
                category = dto.category,
                description = dto.description,
                date = dto.date,
                accountId = dto.accountId,
                isRecurring = false,
                isSynced = true,
            )
        }

        transactionDao.insertAll(entities)
        android.util.Log.d("FinTrackSync", "Inserted ${entities.size} entities. DB count after insert: ${transactionDao.getUnsynced().size} unsynced")

        val unsynced = transactionDao.getUnsynced()
        unsynced.forEach { local ->
            try {
                transactionApi.createTransaction(
                    CreateTransactionRequest(
                        amount = local.amount,
                        type = local.type,
                        category = local.category,
                        description = local.description,
                        date = local.date,
                        accountId = local.accountId,
                    ),
                )
                transactionDao.markAsSynced(local.id)
            } catch (e: Exception) {
                // Bu kayıt sync edilemedi, sonraki sync'te tekrar denenecek
            }
        }

        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DomainException.UnknownException(cause = e))
    }
}