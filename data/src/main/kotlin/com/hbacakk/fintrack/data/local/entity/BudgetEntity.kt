package com.hbacakk.fintrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
    val category: String,
    val period: String,
    val startDate: Long,
    val endDate: Long,
    val isSynced: Boolean = false,
)
