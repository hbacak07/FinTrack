package com.hbacakk.fintrack.feature.home.di

import com.hbacakk.fintrack.domain.usecase.budget.ObserveBudgetsUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveMonthlySummaryUseCase
import com.hbacakk.fintrack.domain.usecase.transaction.ObserveTransactionsUseCase
import com.hbacakk.fintrack.feature.home.dashboard.HomeViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val homeModule = module {
    factory { ObserveMonthlySummaryUseCase(get()) }
    factory { ObserveTransactionsUseCase(get()) }
    factory { ObserveBudgetsUseCase(get()) }

    viewModel { HomeViewModel(get(), get(), get()) }
}