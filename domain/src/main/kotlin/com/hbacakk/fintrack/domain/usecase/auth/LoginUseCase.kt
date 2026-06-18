package com.hbacakk.fintrack.domain.usecase.auth

import com.hbacakk.fintrack.domain.model.User
import com.hbacakk.fintrack.domain.repository.AuthRepository
import com.hbacakk.fintrack.domain.usecase.UseCase
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.Dispatchers

class LoginUseCase(
    private val authRepository: AuthRepository,
) : UseCase<LoginUseCase.Params, User>(Dispatchers.IO) {

    data class Params(
        val email: String,
        val password: String,
    )

    override suspend fun execute(params: Params): Result<User> {
        // Validation — iş kuralları domain katmanında yaşar
        val emailError = validateEmail(params.email)
        if (emailError != null) return Result.Error(emailError)

        val passwordError = validatePassword(params.password)
        if (passwordError != null) return Result.Error(passwordError)

        return authRepository.login(params.email, params.password)
    }

    private fun validateEmail(email: String): DomainException.ValidationException? {
        if (email.isBlank()) {
            return DomainException.ValidationException("email", "E-posta boş olamaz")
        }
        if (!email.contains("@") || !email.contains(".")) {
            return DomainException.ValidationException("email", "Geçersiz e-posta adresi")
        }
        return null
    }

    private fun validatePassword(password: String): DomainException.ValidationException? {
        if (password.isBlank()) {
            return DomainException.ValidationException("password", "Şifre boş olamaz")
        }
        if (password.length < 8) {
            return DomainException.ValidationException("password", "Şifre en az 8 karakter olmalı")
        }
        return null
    }
}