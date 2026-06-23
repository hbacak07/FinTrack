package com.hbacakk.fintrack.core.network.model

import kotlinx.serialization.Serializable

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