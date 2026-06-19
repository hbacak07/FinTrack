package com.hbacakk.fintrack.feature.auth.login

/**
 * Login ekranının tüm durumunu temsil eden tek bir state sınıfı.
 *
 * Neden tek bir data class?
 * - Compose'da her state değişikliği yeniden çizime sebep olur
 * - Birden fazla ayrı state (var email, var password, var isLoading...)
 *   yerine TEK state objesi, recomposition'ı daha öngörülebilir yapar
 * - copy() ile immutable güncellemeler yapılır
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val generalError: String? = null,
    val isLoginSuccessful: Boolean = false,
)