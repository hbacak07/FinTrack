package com.hbacakk.fintrack.feature.transactions.add

import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.repository.TransactionRepository
import com.hbacakk.fintrack.domain.usecase.transaction.AddTransactionUseCase
import com.hbacakk.fintrack.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AddTransactionViewModel")
class AddTransactionViewModelTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: AddTransactionViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk()
        viewModel = AddTransactionViewModel(
            AddTransactionUseCase(transactionRepository, testDispatcher)
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("geçersiz tutar girilirse amountError set edilir")
    fun `invalid amount sets amountError`() = runTest(testDispatcher) {
        viewModel.onAmountChange("0")
        viewModel.onDescriptionChange("Market")

        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Geçerli bir tutar girin", viewModel.uiState.value.amountError)
    }

    @Test
    @DisplayName("boş açıklama descriptionError set edilir")
    fun `blank description sets descriptionError`() = runTest(testDispatcher) {
        viewModel.onAmountChange("150")
        viewModel.onDescriptionChange("")

        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Açıklama boş olamaz", viewModel.uiState.value.descriptionError)
    }

    @Test
    @DisplayName("geçerli veriyle kaydetme isSaved'ı true yapar")
    fun `valid data saves and sets isSaved`() = runTest(testDispatcher) {
        coEvery { transactionRepository.addTransaction(any()) } answers {
            Result.Success(firstArg())
        }

        viewModel.onAmountChange("150")
        viewModel.onDescriptionChange("Market alışverişi")

        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isSaved)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    @DisplayName("tip değişince uygun varsayılan kategori seçilir")
    fun `changing type selects appropriate default category`() {
        viewModel.onTypeSelected(TransactionType.INCOME)

        val state = viewModel.uiState.value
        assertEquals(TransactionType.INCOME, state.selectedType)
        assertFalse(state.selectedCategory.isExpense)
    }

    @Test
    @DisplayName("availableCategories sadece seçili tipe uygun kategorileri içerir")
    fun `availableCategories filters by selected type`() {
        viewModel.onTypeSelected(TransactionType.INCOME)

        val categories = viewModel.uiState.value.availableCategories
        assertTrue(categories.all { !it.isExpense })
        assertTrue(categories.contains(Category.SALARY))
    }
}