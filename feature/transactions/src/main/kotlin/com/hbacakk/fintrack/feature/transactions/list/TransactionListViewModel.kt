package com.hbacakk.fintrack.feature.transactions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.model.TransactionType
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveTransactionsUseCase
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * TransactionListViewModel — filtreleme destekli işlem listesi.
 *
 * flatMapLatest: filtre değiştiğinde (örn. SADECE GİDER seçilince),
 * önceki Flow subscription'ı otomatik iptal edilir, yeni filtreyle
 * yeni bir Flow başlatılır. Eski filtreden gelen "geç kalmış" veri
 * UI'a hiç ulaşmaz.
 */
class TransactionListViewModel(
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow<TransactionType?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val transactionsFlow = _selectedFilter.flatMapLatest { filter ->
        observeTransactionsUseCase(
            ObserveTransactionsUseCase.Params(type = filter)
        )
    }

    val uiState: StateFlow<TransactionListUiState> = combine(
        transactionsFlow,
        _selectedFilter,
        _isLoading,
        _errorMessage,
    ) { transactionsResult, filter, isLoading, error ->
        when (transactionsResult) {
            is Result.Success -> TransactionListUiState(
                isLoading = false,
                transactions = transactionsResult.data,
                selectedFilter = filter,
            )
            is Result.Error -> TransactionListUiState(
                isLoading = false,
                selectedFilter = filter,
                errorMessage = transactionsResult.exception.message,
            )
            is Result.Loading -> TransactionListUiState(
                isLoading = true,
                selectedFilter = filter,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        // WhileSubscribed: ekran görünür olduğu sürece Flow aktif kalır,
        // ekran arka plana gidince 5 saniye sonra durur (kaynak tasarrufu)
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionListUiState(),
    )

    fun onFilterSelected(type: TransactionType?) {
        _selectedFilter.update { type }
    }
}