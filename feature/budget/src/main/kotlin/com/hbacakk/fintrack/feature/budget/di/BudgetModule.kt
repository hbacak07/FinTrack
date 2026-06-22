package com.hbacakk.fintrack.feature.budget.di

import com.hbacakk.fintrack.domain.usecase.budget.CreateBudgetUseCase
import com.hbacakk.fintrack.domain.usecase.budget.ObserveBudgetsUseCase
import com.hbacakk.fintrack.feature.budget.create.CreateBudgetViewModel
import com.hbacakk.fintrack.feature.budget.list.BudgetListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val budgetModule = module {
    factory { ObserveBudgetsUseCase(get()) }
    factory { CreateBudgetUseCase(get()) }

    viewModel { BudgetListViewModel(get()) }
    viewModel { CreateBudgetViewModel(get()) }
}
