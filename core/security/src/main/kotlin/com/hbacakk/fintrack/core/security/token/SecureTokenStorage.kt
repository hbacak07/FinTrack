package com.hbacakk.fintrack.core.security.token

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.hbacakk.fintrack.core.network.interceptor.TokenProvider

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

    fun saveToken(accessToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }

    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .apply()
    }

    fun hasValidSession(): Boolean = getAccessToken() != null

    companion object {
        private const val PREFS_FILE_NAME = "fintrack_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
    }
}