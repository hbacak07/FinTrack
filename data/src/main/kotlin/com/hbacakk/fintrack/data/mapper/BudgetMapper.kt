package com.hbacakk.fintrack.data.mapper

import com.hbacakk.fintrack.data.local.entity.BudgetEntity
import com.hbacakk.fintrack.domain.model.Budget
import com.hbacakk.fintrack.domain.model.BudgetPeriod
import com.hbacakk.fintrack.domain.model.Category

fun BudgetEntity.toDomain(): Budget = Budget(
    id        = id,
    name      = name,
    limit     = limit,
    spent     = spent,
    category  = Category.valueOf(category),
    period    = BudgetPeriod.valueOf(period),
    startDate = startDate,
    endDate   = endDate,
)

fun Budget.toEntity(isSynced: Boolean = false): BudgetEntity = BudgetEntity(
    id        = id,
    name      = name,
    limit     = limit,
    spent     = spent,
    category  = category.name,
    period    = period.name,
    startDate = startDate,
    endDate   = endDate,
    isSynced  = isSynced,
)

fun List<BudgetEntity>.toDomain(): List<Budget> = map { it.toDomain() }