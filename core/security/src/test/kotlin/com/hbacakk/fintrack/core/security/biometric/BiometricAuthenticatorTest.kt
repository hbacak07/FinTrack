package com.hbacakk.fintrack.core.security.biometric

import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("BiometricAuthenticator")
class BiometricAuthenticatorTest {

    @Test
    @DisplayName("biometric kullanılabilir olduğunda AVAILABLE döner")
    fun `returns AVAILABLE when biometric is ready`() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val biometricManager = mockk<BiometricManager>()

        // BiometricManager.from() statik bir fonksiyon, MockK ile statik mock'luyoruz
        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(activity) } returns biometricManager
        every {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        } returns BiometricManager.BIOMETRIC_SUCCESS

        val authenticator = BiometricAuthenticator(activity)

        val result = authenticator.isBiometricAvailable()

        assertEquals(BiometricAvailability.AVAILABLE, result)
    }

    @Test
    @DisplayName("kullanıcı biometric kayıtlı değilse NOT_ENROLLED döner")
    fun `returns NOT_ENROLLED when user has no enrolled biometrics`() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val biometricManager = mockk<BiometricManager>()

        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(activity) } returns biometricManager
        every {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED

        val authenticator = BiometricAuthenticator(activity)

        val result = authenticator.isBiometricAvailable()

        assertEquals(BiometricAvailability.NOT_ENROLLED, result)
    }

    @Test
    @DisplayName("donanım yoksa NO_HARDWARE döner")
    fun `returns NO_HARDWARE when device has no biometric sensor`() {
        val activity = mockk<FragmentActivity>(relaxed = true)
        val biometricManager = mockk<BiometricManager>()

        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(activity) } returns biometricManager
        every {
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        } returns BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE

        val authenticator = BiometricAuthenticator(activity)

        val result = authenticator.isBiometricAvailable()

        assertEquals(BiometricAvailability.NO_HARDWARE, result)
    }
}