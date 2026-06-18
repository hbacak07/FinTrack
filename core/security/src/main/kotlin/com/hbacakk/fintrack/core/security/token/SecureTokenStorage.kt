package com.hbacakk.fintrack.core.security.token

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hbacakk.fintrack.core.network.interceptor.TokenProvider

/**
 * Access/refresh token'ları şifreli olarak saklar.
 *
 * MasterKey: Android Keystore'da tutulan şifreleme anahtarı.
 * Bu anahtar asla uygulama koduna veya diske düz metin olarak çıkmaz.
 * Cihaz destekliyorsa StrongBox (ayrı güvenlik çipi) kullanılır.
 *
 * TokenProvider interface'ini network modülünde tanımlamıştık.
 * Burada gerçek implementasyonu yapıyoruz — Dependency Inversion'ın
 * pratikte nasıl çalıştığının net bir örneği.
 */
class SecureTokenStorage(
    context: Context,
) : TokenProvider {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    override fun getAccessToken(): String? =
        encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)

    override fun getRefreshToken(): String? =
        encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)

    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    fun hasValidSession(): Boolean = getAccessToken() != null

    companion object {
        private const val PREFS_FILE_NAME = "fintrack_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}