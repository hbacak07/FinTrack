package com.hbacakk.fintrack.backend.repository

import com.hbacakk.fintrack.backend.db.Transactions
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

data class TransactionRecord(
    val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String,
    val date: Long,
    val accountId: String,
)

class TransactionRepository {
    fun findAllByUser(userId: String): List<TransactionRecord> =
        transaction {
            Transactions
                .selectAll()
                .where { Transactions.userId eq userId }
                .map { it.toRecord() }
        }

    fun create(
        userId: String,
        amount: Double,
        type: String,
        category: String,
        description: String,
        date: Long,
        accountId: String,
    ): TransactionRecord {
        val id = UUID.randomUUID().toString()

        transaction {
            Transactions.insert {
                it[Transactions.id] = id
                it[Transactions.userId] = userId
                it[Transactions.amount] = amount
                it[Transactions.type] = type
                it[Transactions.category] = category
                it[Transactions.description] = description
                it[Transactions.date] = date
                it[Transactions.accountId] = accountId
            }
        }

        return TransactionRecord(id, amount, type, category, description, date, accountId)
    }

    fun deleteById(
        id: String,
        userId: String,
    ): Boolean =
        transaction {
            Transactions.deleteWhere {
                (Transactions.id eq id) and (Transactions.userId eq userId)
            } > 0
        }

    private fun ResultRow.toRecord() =
        TransactionRecord(
            id = this[Transactions.id],
            amount = this[Transactions.amount],
            type = this[Transactions.type],
            category = this[Transactions.category],
            description = this[Transactions.description],
            date = this[Transactions.date],
            accountId = this[Transactions.accountId],
        )
}
