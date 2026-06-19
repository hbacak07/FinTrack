package com.hbacakk.fintrack.feature.home.dashboard

import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.model.BudgetPeriod
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.repository.BudgetRepository
import com.hbacakk.fintrack.domain.repository.MonthlySummary
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.budget.ObserveBudgetsUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveMonthlySummaryUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveTransactionsUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("HomeViewModel")
class HomeViewModelTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var budgetRepository: BudgetRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk()
        budgetRepository = mockk()
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("üç kaynak da başarılı olunca doğru state üretir")
    fun `combines three flows into correct state`() = runTest(testDispatcher) {
        every {
            transactionRepository.observeMonthlySummary(any(), any())
        } returns flowOf(
            MonthlySummary(
                totalIncome = 10000.0,
                totalExpense = 4000.0,
                transactionCount = 8,
                topExpenseCategory = Category.FOOD,
            )
        )
        every {
            transactionRepository.observeTransactions(any(), any(), any())
        } returns flowOf(emptyList())
        every { budgetRepository.observeBudgets() } returns flowOf(emptyList())

        val viewModel = buildViewModel()

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(6000.0, state.monthlySummary?.netAmount)
    }

    @Test
    @DisplayName("bütçe aşıldığında hasExceededBudgets true olur")
    fun `sets hasExceededBudgets when a budget is exceeded`() = runTest(testDispatcher) {
        val exceededBudget = Budget(
            id = "b1", name = "Yemek", limit = 1000.0, spent = 1500.0,
            category = Category.FOOD, period = BudgetPeriod.MONTHLY,
            startDate = 0L, endDate = 0L,
        )

        every {
            transactionRepository.observeMonthlySummary(any(), any())
        } returns flowOf(MonthlySummary(0.0, 0.0, 0.0,0, null))
        every {
            transactionRepository.observeTransactions(any(), any(), any())
        } returns flowOf(emptyList())
        every { budgetRepository.observeBudgets() } returns flowOf(listOf(exceededBudget))

        val viewModel = buildViewModel()

        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasExceededBudgets)
    }

    private fun buildViewModel() = HomeViewModel(
        observeMonthlySummaryUseCase = ObserveMonthlySummaryUseCase(transactionRepository),
        observeTransactionsUseCase = ObserveTransactionsUseCase(transactionRepository),
        observeBudgetsUseCase = ObserveBudgetsUseCase(budgetRepository),
    )
}