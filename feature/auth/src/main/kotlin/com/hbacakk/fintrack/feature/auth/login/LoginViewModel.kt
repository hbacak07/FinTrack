package com.hbacakk.fintrack.feature.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hbacakk.fintrack.domain.usecase.auth.LoginUseCase
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Login ekranının ViewModel'i.
 *
 * MutableStateFlow + StateFlow pattern:
 * - _uiState: ViewModel içinde mutable, sadece burada değiştirilir
 * - uiState: dışarıya immutable StateFlow olarak açılır
 *   (Compose tarafı state'i değiştiremez, sadece okur)
 *
 * Bu, "encapsulation" prensibinin Compose/MVVM'deki karşılığıdır.
 */
class LoginViewModel(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update {
            it.copy(email = email, emailError = null, generalError = null)
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update {
            it.copy(password = password, passwordError = null, generalError = null)
        }
    }

    fun onPasswordVisibilityToggle() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun onLoginClick() {
        val currentState = _uiState.value

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, generalError = null) }

            val result = loginUseCase(
                LoginUseCase.Params(
                    email = currentState.email,
                    password = currentState.password,
                )
            )

            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, isLoginSuccessful = true)
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                    handleError(result.exception)
                }
                is Result.Loading -> Unit
            }
        }
    }

    /**
     * Hata tipine göre, hangi alana hangi mesajın gösterileceğine karar verir.
     * ValidationException'lar ilgili field'a, diğerleri genel hataya bağlanır.
     */
    private fun handleError(exception: DomainException) {
        when (exception) {
            is DomainException.ValidationException -> {
                when (exception.field) {
                    "email" -> _uiState.update { it.copy(emailError = exception.message) }
                    "password" -> _uiState.update { it.copy(passwordError = exception.message) }
                    else -> _uiState.update { it.copy(generalError = exception.message) }
                }
            }
            is DomainException.UnauthorizedException -> {
                _uiState.update {
                    it.copy(generalError = "E-posta veya şifre hatalı")
                }
            }
            is DomainException.NetworkException -> {
                _uiState.update {
                    it.copy(generalError = "İnternet bağlantınızı kontrol edin")
                }
            }
            else -> {
                _uiState.update {
                    it.copy(generalError = exception.message)
                }
            }
        }
    }
}
