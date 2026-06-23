package com.hbacakk.fintrack.backend.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date

/**
 * JWT token üretimi ve doğrulaması.
 *
 * Android tarafındaki AuthInterceptor, bu sınıfın ürettiği
 * token'ı "Authorization: Bearer <token>" header'ında gönderir.
 *
 * NOT: secret'ı gerçek bir projede ortam değişkeninden okumalısın,
 * burada öğrenme amaçlı sabit bırakıyoruz.
 */
object JwtConfig {
    private const val SECRET = "fintrack-secret-key-change-in-production"
    private const val ISSUER = "fintrack-backend"
    private const val VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L // 7 gün

    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(userId: String, email: String): String =
        JWT.create()
            .withIssuer(ISSUER)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
            .sign(algorithm)
}