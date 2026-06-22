package com.hbacakk.fintrack.feature.transactions.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.usecase.transaction.AddTransactionUseCase
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    fun onAmountChange(amount: String) {
        // Sadece sayı ve nokta karakterine izin ver — UI seviyesinde basit doğrulama
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(amount = amount, amountError = null) }
        }
    }

    fun onDescriptionChange(description: String) {
        _uiState.update { it.copy(description = description, descriptionError = null) }
    }

    fun onTypeSelected(type: TransactionType) {
        _uiState.update {
            // Tip değişince, o tipe uygun ilk kategoriyi otomatik seç
            val defaultCategory = Category.entries.first { c -> c.isExpense == (type == TransactionType.EXPENSE) }
            it.copy(selectedType = type, selectedCategory = defaultCategory)
        }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSaveClick() {
        val state = _uiState.value

        val amountValue = state.amount.toDoubleOrNull()
        if (amountValue == null || amountValue <= 0) {
            _uiState.update { it.copy(amountError = "Geçerli bir tutar girin") }
            return
        }
        if (state.description.isBlank()) {
            _uiState.update { it.copy(descriptionError = "Açıklama boş olamaz") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val transaction = Transaction(
                id = UUID.randomUUID().toString(),
                amount = amountValue,
                type = state.selectedType,
                category = state.selectedCategory,
                description = state.description,
                date = System.currentTimeMillis(),
                accountId = "default", // Adım 9'da hesap seçimi eklenecek
            )

            val result = addTransactionUseCase(transaction)

            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            descriptionError = (result.exception as? DomainException.ValidationException)?.message,
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }
}
