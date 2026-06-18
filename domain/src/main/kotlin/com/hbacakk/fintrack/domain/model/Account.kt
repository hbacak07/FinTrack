package com.hbacakk.fintrack.domain.model

data class Account(
    val id: String,
    val name: String,
    val balance: Double,
    val type: AccountType,
    val currency: Currency,
    val color: String,
    val isDefault: Boolean = false,
)

enum class AccountType(val displayName: String) {
    CHECKING("Vadesiz Hesap"),
    SAVINGS("Tasarruf Hesabı"),
    CREDIT_CARD("Kredi Kartı"),
    CASH("Nakit"),
    INVESTMENT("Yatırım Hesabı"),
}