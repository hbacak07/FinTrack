package com.hbacakk.fintrack.data.mapper

import com.hbacakk.fintrack.data.local.entity.TransactionEntity
import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType

/**
 * Entity ↔ Domain dönüşüm fonksiyonları.
 *
 * Neden ayrı mapper fonksiyonları?
 * - Entity ve domain modeli birbirinden bağımsız kalır
 * - Dönüşüm mantığı tek yerde toplanır
 * - Test etmesi kolaydır — saf fonksiyonlar
 *
 * Extension function olarak yazmak, kullanımı doğallaştırır:
 * entity.toDomain() veya transaction.toEntity()
 */
fun TransactionEntity.toDomain(): Transaction = Transaction(
    id          = id,
    amount      = amount,
    type        = TransactionType.valueOf(type),
    category    = Category.valueOf(category),
    description = description,
    date        = date,
    accountId   = accountId,
    isRecurring = isRecurring,
)

fun Transaction.toEntity(isSynced: Boolean = false): TransactionEntity = TransactionEntity(
    id          = id,
    amount      = amount,
    type        = type.name,
    category    = category.name,
    description = description,
    date        = date,
    accountId   = accountId,
    isRecurring = isRecurring,
    isSynced    = isSynced,
)

fun List<TransactionEntity>.toDomain(): List<Transaction> = map { it.toDomain() }
