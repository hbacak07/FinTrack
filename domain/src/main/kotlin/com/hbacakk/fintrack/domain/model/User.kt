package com.hbacakk.fintrack.domain.model

data class User(
    val id: String,
    val email: String,
    val fullName: String,
    val currency: Currency = Currency.TRY,
    val createdAt: Long,
)

enum class Currency(val symbol: String, val code: String) {
    TRY("₺", "TRY"),
    USD("$", "USD"),
    EUR("€", "EUR"),
    GBP("£", "GBP"),
}