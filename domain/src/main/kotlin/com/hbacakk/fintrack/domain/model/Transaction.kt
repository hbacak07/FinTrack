package com.hbacakk.fintrack.domain.model

data class Transaction(
    val id: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val description: String,
    val date: Long,
    val accountId: String,
    val isRecurring: Boolean = false,
)

enum class TransactionType { INCOME, EXPENSE, TRANSFER }

enum class Category(val displayName: String, val isExpense: Boolean) {
    FOOD("Yemek", true),
    TRANSPORT("Ulaşım", true),
    SHOPPING("Alışveriş", true),
    BILLS("Faturalar", true),
    HEALTH("Sağlık", true),
    ENTERTAINMENT("Eğlence", true),
    EDUCATION("Eğitim", true),
    OTHER_EXPENSE("Diğer", true),

    SALARY("Maaş", false),
    FREELANCE("Serbest Çalışma", false),
    INVESTMENT("Yatırım", false),
    OTHER_INCOME("Diğer Gelir", false),
}