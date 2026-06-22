package com.hbacakk.fintrack.core.testing.fixture

import com.hbacakk.fintrack.domain.model.Currency
import com.hbacakk.fintrack.domain.model.User

fun userFixture(
    id: String = "user-test",
    email: String = "test@example.com",
    fullName: String = "Test Kullanıcı",
    currency: Currency = Currency.TRY,
    createdAt: Long = 0L,
): User = User(
    id = id,
    email = email,
    fullName = fullName,
    currency = currency,
    createdAt = createdAt,
)