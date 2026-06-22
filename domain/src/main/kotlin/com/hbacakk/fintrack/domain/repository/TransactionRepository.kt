package com.hbacakk.fintrack.domain.repository

import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    /**
     * İşlemleri Flow olarak gözlemle.
     * Room veritabanı değiştiğinde UI otomatik güncellenir.
     * Bu pattern: "Single Source of Truth"
     */
    fun observeTransactions(
        accountId: String? = null,
        type: TransactionType? = null,
        category: Category? = null,
    ): Flow<List<Transaction>>

    suspend fun getTransactionById(id: String): Result<Transaction>

    suspend fun addTransaction(transaction: Transaction): Result<Transaction>

    suspend fun updateTransaction(transaction: Transaction): Result<Transaction>

    suspend fun deleteTransaction(id: String): Result<Unit>

    fun observeMonthlySummary(
        year: Int,
        month: Int,
    ): Flow<MonthlySummary>

    suspend fun syncTransactions(): Result<Unit>
}

data class MonthlySummary(
    val totalIncome: Double,
    val totalExpense: Double,
    val netAmount: Double = totalIncome - totalExpense,
    val transactionCount: Int,
    val topExpenseCategory: Category?,
)
