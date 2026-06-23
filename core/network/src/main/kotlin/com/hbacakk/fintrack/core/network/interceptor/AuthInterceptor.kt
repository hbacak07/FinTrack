package com.hbacakk.fintrack.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Her HTTP isteğine otomatik olarak Authorization header'ı ekler.
 *
 * Koin ile constructor injection: @Inject annotation'ına gerek yok.
 * Koin modülünde "AuthInterceptor(get())" şeklinde tanımlanacak.
 */
class AuthInterceptor(
    private val tokenProvider: TokenProvider,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header(NO_AUTH_HEADER) != null) {
            return chain.proceed(originalRequest)
        }

        val token = tokenProvider.getAccessToken()

        val authenticatedRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(authenticatedRequest)
    }

    companion object {
        const val NO_AUTH_HEADER = "No-Auth"
    }
}

interface TokenProvider {
    fun getAccessToken(): String?
}
