package com.hbacakk.fintrack.data.repository

import app.cash.turbine.test
import com.hbacakk.fintrack.data.local.dao.TransactionDao
import com.hbacakk.fintrack.data.local.entity.TransactionEntity
import com.hbacakk.fintrack.data.remote.repository.TransactionRepositoryImpl
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("TransactionRepositoryImpl")
class TransactionRepositoryImplTest {

    private val transactionDao: TransactionDao = mockk()
    private lateinit var repository: TransactionRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = TransactionRepositoryImpl(transactionDao)
    }

    @Nested
    @DisplayName("observeTransactions")
    inner class ObserveTransactions {

        @Test
        @DisplayName("DAO'dan gelen entity listesini domain modeline dönüştürür")
        fun `maps entities to domain models`() = runTest {
            val entities = listOf(fakeTransactionEntity())
            every { transactionDao.observeAll() } returns flowOf(entities)

            repository.observeTransactions().test {
                val result = awaitItem()
                assertEquals(1, result.size)
                assertEquals("tx-123", result.first().id)
                assertEquals(TransactionType.EXPENSE, result.first().type)
                awaitComplete()
            }
        }

        @Test
        @DisplayName("accountId verilince doğru DAO fonksiyonunu çağırır")
        fun `calls observeByAccount when accountId provided`() = runTest {
            val accountId = "acc-456"
            every {
                transactionDao.observeByAccount(accountId)
            } returns flowOf(emptyList())

            repository.observeTransactions(accountId = accountId).test {
                awaitItem()
                awaitComplete()
            }

            coVerify { transactionDao.observeByAccount(accountId) }
        }
    }

    @Nested
    @DisplayName("addTransaction")
    inner class AddTransaction {

        @Test
        @DisplayName("transaction'ı DAO'ya kaydeder ve Success döner")
        fun `inserts transaction and returns success`() = runTest {
            val transaction = fakeTransaction()
            coEvery { transactionDao.insert(any()) } returns Unit

            val result = repository.addTransaction(transaction)

            assertTrue(result is Result.Success)
            coVerify { transactionDao.insert(any()) }
        }

        @Test
        @DisplayName("DAO hata fırlatırsa Error döner")
        fun `returns error when dao throws`() = runTest {
            val transaction = fakeTransaction()
            coEvery { transactionDao.insert(any()) } throws RuntimeException("DB error")

            val result = repository.addTransaction(transaction)

            assertTrue(result is Result.Error)
        }
    }

    @Nested
    @DisplayName("deleteTransaction")
    inner class DeleteTransaction {

        @Test
        @DisplayName("DAO'dan silme çağrısı yapar ve Success döner")
        fun `deletes transaction and returns success`() = runTest {
            coEvery { transactionDao.deleteById("tx-123") } returns Unit

            val result = repository.deleteTransaction("tx-123")

            assertTrue(result is Result.Success)
            coVerify { transactionDao.deleteById("tx-123") }
        }
    }

    // --- Test verileri ---

    private fun fakeTransactionEntity() = TransactionEntity(
        id          = "tx-123",
        amount      = 150.0,
        type        = TransactionType.EXPENSE.name,
        category    = Category.FOOD.name,
        description = "Yemek",
        date        = 1718000000000L,
        accountId   = "acc-456",
        isRecurring = false,
        isSynced    = false,
    )

    private fun fakeTransaction() = Transaction(
        id          = "tx-123",
        amount      = 150.0,
        type        = TransactionType.EXPENSE,
        category    = Category.FOOD,
        description = "Yemek",
        date        = 1718000000000L,
        accountId   = "acc-456",
        isRecurring = false,
    )
}
