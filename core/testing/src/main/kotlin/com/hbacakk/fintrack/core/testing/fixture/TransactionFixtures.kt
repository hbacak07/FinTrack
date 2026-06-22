package com.hbacakk.fintrack.core.testing.fixture

import com.hbacakk.fintrack.domain.model.Category
import com.hbacakk.fintrack.domain.model.Transaction
import com.hbacakk.fintrack.domain.model.TransactionType

/**
 * Test verisi üreten fonksiyonlar.
 *
 * Neden bu kadar önemli?
 * Her testte "Transaction(id=..., amount=..., ...)" yazmak yerine,
 * tek bir fonksiyon çağrısıyla geçerli, tutarlı test verisi üretiriz.
 * Default parametrelerle, sadece test için önemli olan alanı override edersin:
 *
 * val transaction = transactionFixture(amount = 500.0)
 */
fun transactionFixture(
    id: String = "tx-${System.nanoTime()}",
    amount: Double = 100.0,
    type: TransactionType = TransactionType.EXPENSE,
    category: Category = Category.FOOD,
    description: String = "Test işlemi",
    date: Long = 1718000000000L,
    accountId: String = "acc-test",
): Transaction = Transaction(
    id = id,
    amount = amount,
    type = type,
    category = category,
    description = description,
    date = date,
    accountId = accountId,
)
