package com.hbacakk.fintrack.data.remote.repository

import com.hbacakk.fintrack.core.network.api.AuthApi
import com.hbacakk.fintrack.core.network.model.LoginRequest
import com.hbacakk.fintrack.core.network.model.RegisterRequest
import com.hbacakk.fintrack.core.security.token.SecureTokenStorage
import com.hbacakk.fintrack.domain.model.Currency
import com.hbacakk.fintrack.domain.model.User
import com.hbacakk.fintrack.domain.repository.AuthRepository
import com.hbacakk.fintrack.domain.repository.AuthState
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * AuthRepository implementasyonu.
 *
 * Backend (Ktor) artık gerçek ve çalışıyor (Adım 12).
 * Backend sade tutulduğu için (currency, createdAt, refreshToken
 * desteklemiyor), bu alanlar burada varsayılan değerlerle dolduruluyor.
 */
class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val tokenStorage: SecureTokenStorage,
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(
        if (tokenStorage.hasValidSession()) AuthState.Authenticated
        else AuthState.Unauthenticated
    )

    override suspend fun login(email: String, password: String): Result<User> = try {
        val response = authApi.login(LoginRequest(email, password))
        tokenStorage.saveToken(response.token)

        val user = User(
            id = response.user.id,
            email = response.user.email,
            fullName = response.user.fullName,
            currency = Currency.TRY,
            createdAt = System.currentTimeMillis(),
        )

        _authState.value = AuthState.Authenticated
        Result.Success(user)
    } catch (e: Exception) {
        Result.Error(DomainException.NetworkException(cause = e))
    }

    override suspend fun register(
        email: String,
        password: String,
        fullName: String,
    ): Result<User> = try {
        val response = authApi.register(RegisterRequest(email, password, fullName))
        tokenStorage.saveToken(response.token)

        val user = User(
            id = response.user.id,
            email = response.user.email,
            fullName = response.user.fullName,
            currency = Currency.TRY,
            createdAt = System.currentTimeMillis(),
        )

        _authState.value = AuthState.Authenticated
        Result.Success(user)
    } catch (e: Exception) {
        Result.Error(DomainException.NetworkException(cause = e))
    }

    override suspend fun logout(): Result<Unit> {
        tokenStorage.clearTokens()
        _authState.value = AuthState.Unauthenticated
        return Result.Success(Unit)
    }

    override suspend fun refreshToken(): Result<Unit> {
        // Backend refresh token desteklemiyor (bilinçli sadeleştirme, Adım 12)
        return Result.Success(Unit)
    }

    override fun observeAuthState(): StateFlow<AuthState> = _authState.asStateFlow()

    override suspend fun getCurrentUser(): Result<User> {
        return Result.Error(DomainException.UnknownException(message = "Henüz implemente edilmedi"))
    }
}