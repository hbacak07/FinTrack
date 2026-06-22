package com.hbacakk.fintrack.domain.repository

import com.hbacakk.fintrack.domain.model.User
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(
        email: String,
        password: String,
    ): Result<User>

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
    ): Result<User>

    suspend fun logout(): Result<Unit>

    suspend fun refreshToken(): Result<Unit>

    /**
     * Oturum durumunu gözlemle.
     * Flow kullanıyoruz çünkü oturum durumu değişebilir
     * (token expire, başka cihazdan logout vb.)
     */
    fun observeAuthState(): Flow<AuthState>

    suspend fun getCurrentUser(): Result<User>
}

sealed interface AuthState {
    data object Authenticated : AuthState

    data object Unauthenticated : AuthState

    data object Loading : AuthState
}
