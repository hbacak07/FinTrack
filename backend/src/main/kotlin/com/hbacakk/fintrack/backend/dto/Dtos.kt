package com.hbacakk.fintrack.backend.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String,
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto,
)

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val fullName: String,
)

@Serializable
data class TransactionDto(
    val id: String,
    val amount: Double,
    val type: String,
    val category: String,
    val description: String,
    val date: Long,
    val accountId: String,
)

@Serializable
data class CreateTransactionRequest(
    val amount: Double,
    val type: String,
    val category: String,
    val description: String,
    val date: Long,
    val accountId: String,
)

@Serializable
data class BudgetDto(
    val id: String,
    val name: String,
    val limit: Double,
    val spent: Double,
    val category: String,
    val period: String,
    val startDate: Long,
    val endDate: Long,
)

@Serializable
data class CreateBudgetRequest(
    val name: String,
    val limit: Double,
    val category: String,
    val period: String,
    val startDate: Long,
    val endDate: Long,
)

@Serializable
data class ErrorResponse(
    val message: String,
)
