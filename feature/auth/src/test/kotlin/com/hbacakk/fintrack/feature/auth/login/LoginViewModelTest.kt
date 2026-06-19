package com.hbacakk.fintrack.feature.auth.login

import app.cash.turbine.test
import com.hbacakk.fintrack.domain.model.Currency
import com.hbacakk.fintrack.domain.model.User
import com.hbacakk.fintrack.domain.repository.AuthRepository
import com.hbacakk.fintrack.domain.usecase.auth.LoginUseCase
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LoginViewModel")
class LoginViewModelTest {

    private val authRepository: AuthRepository = mockk()
    private lateinit var loginUseCase: LoginUseCase
    private lateinit var viewModel: LoginViewModel

    /**
     * viewModelScope, Dispatchers.Main kullanır. JVM testlerinde
     * gerçek Main dispatcher yok — bu yüzden test dispatcher'ı
     * Main olarak set ediyoruz.
     */
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        loginUseCase = LoginUseCase(authRepository)
        viewModel = LoginViewModel(loginUseCase)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    @DisplayName("başlangıç state'i boştur")
    fun `initial state is empty`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.email)
            assertEquals("", state.password)
            assertFalse(state.isLoading)
        }
    }

    @Test
    @DisplayName("email değişikliği state'i günceller")
    fun `onEmailChange updates state`() = runTest {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onEmailChange("test@example.com")

            val updated = awaitItem()
            assertEquals("test@example.com", updated.email)
        }
    }

    @Test
    @DisplayName("şifre görünürlüğü toggle edilebilir")
    fun `onPasswordVisibilityToggle flips visibility`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onPasswordVisibilityToggle()

            val updated = awaitItem()
            assertTrue(updated.isPasswordVisible)
        }
    }

    @Test
    @DisplayName("başarılı login isLoginSuccessful'ı true yapar")
    fun `successful login sets isLoginSuccessful`() = runTest {
        val fakeUser = User(
            id = "1", email = "test@example.com", fullName = "Test",
            currency = Currency.TRY, createdAt = 0L,
        )
        coEvery {
            authRepository.login("test@example.com", "password123")
        } returns Result.Success(fakeUser)

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("password123")

        viewModel.uiState.test {
            awaitItem() // password change sonrası state

            viewModel.onLoginClick()

            awaitItem() // isLoading = true
            val successState = awaitItem() // isLoading = false, isLoginSuccessful = true

            assertTrue(successState.isLoginSuccessful)
            assertFalse(successState.isLoading)
        }
    }

    @Test
    @DisplayName("yanlış şifre generalError'a mesaj koyar")
    fun `wrong credentials sets generalError`() = runTest {
        coEvery {
            authRepository.login(any(), any())
        } returns Result.Error(DomainException.UnauthorizedException())

        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("wrongpassword")

        viewModel.uiState.test {
            awaitItem()

            viewModel.onLoginClick()

            awaitItem() // isLoading = true
            val errorState = awaitItem() // isLoading = false, generalError set

            assertEquals("E-posta veya şifre hatalı", errorState.generalError)
            assertFalse(errorState.isLoading)
        }
    }
}