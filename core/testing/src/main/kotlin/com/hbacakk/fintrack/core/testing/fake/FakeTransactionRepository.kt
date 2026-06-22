package com.hbacakk.fintrack.core.testing.fake

import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.repository.MonthlySummary
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * TransactionRepository'nin test amaçlı sahte implementasyonu.
 *
 * Gerçek bir Room veritabanı yerine, bellekte (in-memory) bir liste
 * tutar. Flow ile gerçekten yayın yapar — yani bir test, repository'ye
 * veri eklediğinde, UI tarafı (ViewModel testi) bunu GERÇEKTEN alır.
 *
 * Bu, her testte aynı mockk() kurulumunu tekrar yazmaktan
 * çok daha az kod ve daha gerçekçi davranış sağlar.
 */
class FakeTransactionRepository : TransactionRepository {

    private val transactionsFlow = MutableStateFlow<List<Transaction>>(emptyList())

    // Test'lerin hata senaryosu simüle edebilmesi için
    var shouldReturnError: Boolean = false

    override fun observeTransactions(
        accountId: String?,
        type: TransactionType?,
        category: Category?,
    ): Flow<List<Transaction>> = transactionsFlow.map { transactions ->
        transactions.filter { tx ->
            (accountId == null || tx.accountId == accountId) &&
                    (type == null || tx.type == type) &&
                    (category == null || tx.category == category)
        }
    }

    override suspend fun getTransactionById(id: String): Result<Transaction> {
        val transaction = transactionsFlow.value.find { it.id == id }
        return if (transaction != null) {
            Result.Success(transaction)
        } else {
            Result.Error(DomainException.NotFoundException())
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Transaction> {
        if (shouldReturnError) {
            return Result.Error(DomainException.UnknownException())
        }
        transactionsFlow.value = transactionsFlow.value + transaction
        return Result.Success(transaction)
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Transaction> {
        transactionsFlow.value = transactionsFlow.value.map {
            if (it.id == transaction.id) transaction else it
        }
        return Result.Success(transaction)
    }

    override suspend fun deleteTransaction(id: String): Result<Unit> {
        transactionsFlow.value = transactionsFlow.value.filterNot { it.id == id }
        return Result.Success(Unit)
    }

    override fun observeMonthlySummary(year: Int, month: Int): Flow<MonthlySummary> =
        transactionsFlow.map { transactions ->
            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense,
                transactionCount = transactions.size,
                topExpenseCategory = null,
            )
        }

    override suspend fun syncTransactions(): Result<Unit> = Result.Success(Unit)

    /** Test setup için yardımcı fonksiyon — direkt veri enjekte etmek için */
    fun setTransactions(transactions: List<Transaction>) {
        transactionsFlow.value = transactions
    }
}