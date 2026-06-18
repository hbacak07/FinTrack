package com.hbacakk.fintrack.domain.model

data class Budget(
    val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
    val category: Category,
    val period: BudgetPeriod,
    val startDate: Long,
    val endDate: Long,
) {
    val spentPercentage: Double
        get() = if (limit > 0) (spent / limit) * 100 else 0.0

    val remaining: Double
        get() = limit - spent

    val isExceeded: Boolean
        get() = spent > limit

    val isWarning: Boolean
        get() = spentPercentage >= 80 && !isExceeded
}

enum class BudgetPeriod { WEEKLY, MONTHLY, YEARLY }