package com.hbacakk.fintrack.feature.budget.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.model.BudgetPeriod
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.usecase.budget.CreateBudgetUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

class CreateBudgetViewModel(
    private val createBudgetUseCase: CreateBudgetUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateBudgetUiState())
    val uiState: StateFlow<CreateBudgetUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name, nameError = null) }
    }

    fun onLimitChange(limit: String) {
        if (limit.isEmpty() || limit.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.update { it.copy(limit = limit, limitError = null) }
        }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onPeriodSelected(period: BudgetPeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
    }

    fun onSaveClick() {
        val state = _uiState.value
        val limitValue = state.limit.toDoubleOrNull()

        if (limitValue == null || limitValue <= 0) {
            _uiState.update { it.copy(limitError = "Geçerli bir limit girin") }
            return
        }
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Bütçe adı boş olamaz") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val calendar = Calendar.getInstance()
            val startDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            val endDate = calendar.timeInMillis

            val budget = Budget(
                id = UUID.randomUUID().toString(),
                name = state.name,
                limit = limitValue,
                spent = 0.0,
                category = state.selectedCategory,
                period = state.selectedPeriod,
                startDate = startDate,
                endDate = endDate,
            )

            val result = createBudgetUseCase(budget)

            when (result) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, isSaved = true) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, nameError = result.exception.message)
                }
                is Result.Loading -> Unit
            }
        }
    }
}
