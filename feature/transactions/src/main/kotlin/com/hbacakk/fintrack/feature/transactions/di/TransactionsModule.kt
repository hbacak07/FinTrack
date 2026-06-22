package com.hbacakk.fintrack.feature.transactions.di

import com.hbacakk.fintrack.domain.usecase.transaction.AddTransactionUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveTransactionsUseCase
import com.hbacakk.fintrack.feature.transactions.add.AddTransactionViewModel
import com.hbacakk.fintrack.feature.transactions.list.TransactionListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val transactionsModule = module {
    factory { ObserveTransactionsUseCase(get()) }
    factory { AddTransactionUseCase(get()) }

    viewModel { TransactionListViewModel(get()) }
    viewModel { AddTransactionViewModel(get()) }
}
