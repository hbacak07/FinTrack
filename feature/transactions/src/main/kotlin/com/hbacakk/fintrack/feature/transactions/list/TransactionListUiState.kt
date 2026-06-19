package com.hbacakk.fintrack.feature.transactions.list

import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType

data class TransactionListUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val selectedFilter: TransactionType? = null,
    val errorMessage: String? = null,
)