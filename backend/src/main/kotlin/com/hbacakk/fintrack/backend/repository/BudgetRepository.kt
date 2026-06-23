package com.hbacakk.fintrack.backend.repository

import com.hbacakk.fintrack.backend.db.Budgets
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

data class BudgetRecord(
    val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
    val category: String,
    val period: String,
    val startDate: Long,
    val endDate: Long,
)

class BudgetRepository {
    fun findAllByUser(userId: String): List<BudgetRecord> =
        transaction {
            Budgets
                .selectAll()
                .where { Budgets.userId eq userId }
                .map { it.toRecord() }
        }

    fun create(
        userId: String,
        name: String,
        limit: Double,
        category: String,
        period: String,
        startDate: Long,
        endDate: Long,
    ): BudgetRecord {
        val id = UUID.randomUUID().toString()

        transaction {
            Budgets.insert {
                it[Budgets.id] = id
                it[Budgets.userId] = userId
                it[Budgets.name] = name
                it[Budgets.limit] = limit
                it[Budgets.spent] = 0.0
                it[Budgets.category] = category
                it[Budgets.period] = period
                it[Budgets.startDate] = startDate
                it[Budgets.endDate] = endDate
            }
        }

        return BudgetRecord(id, name, limit, 0.0, category, period, startDate, endDate)
    }

    private fun ResultRow.toRecord() =
        BudgetRecord(
            id = this[Budgets.id],
            name = this[Budgets.name],
            limit = this[Budgets.limit],
            spent = this[Budgets.spent],
            category = this[Budgets.category],
            period = this[Budgets.period],
            startDate = this[Budgets.startDate],
            endDate = this[Budgets.endDate],
        )
}
