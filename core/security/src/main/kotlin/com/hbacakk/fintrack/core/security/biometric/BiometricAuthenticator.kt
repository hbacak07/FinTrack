package com.hbacakk.fintrack.core.security.biometric

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Biometric (parmak izi/yüz tanıma) authentication yönetimi.
 *
 * Finans uygulamalarında bu kritik: uygulama her açıldığında
 * veya bir işlem (örn. para transferi) onaylanmadan önce
 * biometric doğrulama istenebilir.
 */
class BiometricAuthenticator(
    private val activity: FragmentActivity,
) {
    private val executor: Executor = Executors.newSingleThreadExecutor()

    /**
     * Cihazda biometric donanım var mı ve kullanıcı kayıtlı mı kontrol eder.
     * Bunu UI'da "biometric girişi göster" kararını vermeden önce çağırmalısın.
     */
    fun isBiometricAvailable(): BiometricAvailability {
        val biometricManager = BiometricManager.from(activity)
        return when (
            biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        ) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                BiometricAvailability.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                BiometricAvailability.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                BiometricAvailability.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                BiometricAvailability.NOT_ENROLLED
            else -> BiometricAvailability.UNKNOWN_ERROR
        }
    }

    /**
     * Biometric prompt'u gösterir, sonucu Flow olarak döner.
     *
     * callbackFlow kullanıyoruz çünkü BiometricPrompt callback-based bir API,
     * bunu modern coroutine/Flow dünyasına adapte ediyoruz.
     */
    fun authenticate(
        title: String = "Kimlik Doğrulama",
        subtitle: String = "Devam etmek için kimliğinizi doğrulayın",
    ) = callbackFlow<BiometricResult> {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("İptal")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult,
                ) {
                    trySend(BiometricResult.Success)
                    close()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    trySend(BiometricResult.Error(errorCode, errString.toString()))
                    close()
                }

                override fun onAuthenticationFailed() {
                    // Tek seferlik yanlış deneme — prompt açık kalır, kullanıcı tekrar deneyebilir
                    trySend(BiometricResult.Failed)
                }
            },
        )

        biometricPrompt.authenticate(promptInfo)

        awaitClose { biometricPrompt.cancelAuthentication() }
    }
}

enum class BiometricAvailability {
    AVAILABLE,
    NO_HARDWARE,
    HARDWARE_UNAVAILABLE,
    NOT_ENROLLED,
    UNKNOWN_ERROR,
}

sealed interface BiometricResult {
    data object Success : BiometricResult
    data object Failed : BiometricResult
    data class Error(val code: Int, val message: String) : BiometricResult
}