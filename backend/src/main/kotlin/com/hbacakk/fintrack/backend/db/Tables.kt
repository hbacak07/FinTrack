package com.hbacakk.fintrack.backend.db

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = varchar("id", 36)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val fullName = varchar("full_name", 255)
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object Transactions : Table("transactions") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val amount = double("amount")
    val type = varchar("type", 20)
    val category = varchar("category", 50)
    val description = varchar("description", 500)
    val date = long("date")
    val accountId = varchar("account_id", 36)

    override val primaryKey = PrimaryKey(id)
}

object Budgets : Table("budgets") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(Users.id)
    val name = varchar("name", 255)
    val limit = double("limit_amount")
    val spent = double("spent")
    val category = varchar("category", 50)
    val period = varchar("period", 20)
    val startDate = long("start_date")
    val endDate = long("end_date")

    override val primaryKey = PrimaryKey(id)
}