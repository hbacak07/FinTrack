package com.hbacakk.fintrack.domain.usecase.auth

import com.hbacakk.fintrack.domain.model.Currency
import com.hbacakk.fintrack.domain.model.User
import com.hbacakk.fintrack.domain.repository.AuthRepository
import com.hbacakk.fintrack.domain.util.DomainException
import com.hbacakk.fintrack.domain.util.Result
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("LoginUseCase")
class LoginUseCaseTest {
    // mockk: Kotlin-native mock kütüphanesi
    private val authRepository: AuthRepository = mockk()
    private lateinit var loginUseCase: LoginUseCase

    @BeforeEach
    fun setUp() {
        loginUseCase = LoginUseCase(authRepository)
    }

    @Nested
    @DisplayName("Geçerli input ile")
    inner class ValidInput {
        @Test
        @DisplayName("başarılı login")
        fun `valid credentials return success`() =
            runTest {
                // Given
                val email = "test@example.com"
                val password = "password123"
                val expectedUser = fakeUser()

                // coEvery: suspend fonksiyon mock'u
                coEvery {
                    authRepository.login(email, password)
                } returns Result.Success(expectedUser)

                // When
                val result = loginUseCase(LoginUseCase.Params(email, password))

                // Then
                assertTrue(result is Result.Success)
                assertEquals(expectedUser, (result as Result.Success).data)

                // Repository'nin gerçekten çağrıldığını doğrula
                coVerify(exactly = 1) { authRepository.login(email, password) }
            }
    }

    @Nested
    @DisplayName("Geçersiz input ile")
    inner class InvalidInput {
        @Test
        @DisplayName("boş email")
        fun `blank email returns validation error`() =
            runTest {
                val result = loginUseCase(LoginUseCase.Params("", "password123"))

                assertTrue(result is Result.Error)
                val exception = (result as Result.Error).exception
                assertTrue(exception is DomainException.ValidationException)
                assertEquals("email", (exception as DomainException.ValidationException).field)
            }

        @Test
        @DisplayName("geçersiz email formatı")
        fun `invalid email format returns error`() =
            runTest {
                val result = loginUseCase(LoginUseCase.Params("notanemail", "password123"))

                assertTrue(result is Result.Error)
                assertTrue((result as Result.Error).exception is DomainException.ValidationException)
            }

        @Test
        @DisplayName("kısa şifre")
        fun `short password returns validation error`() =
            runTest {
                val result = loginUseCase(LoginUseCase.Params("test@example.com", "short"))

                assertTrue(result is Result.Error)
                val exception = (result as Result.Error).exception
                assertTrue(exception is DomainException.ValidationException)
                assertEquals("password", (exception as DomainException.ValidationException).field)
            }

        @Test
        @DisplayName("network hatası")
        fun `network error propagates from repository`() =
            runTest {
                coEvery {
                    authRepository.login(any(), any())
                } returns Result.Error(DomainException.NetworkException())

                val result = loginUseCase(LoginUseCase.Params("test@example.com", "password123"))

                assertTrue(result is Result.Error)
                assertTrue((result as Result.Error).exception is DomainException.NetworkException)
            }
    }

    private fun fakeUser() =
        User(
            id = "user-123",
            email = "test@example.com",
            fullName = "Test Kullanıcı",
            currency = Currency.TRY,
            createdAt = System.currentTimeMillis(),
        )
}
