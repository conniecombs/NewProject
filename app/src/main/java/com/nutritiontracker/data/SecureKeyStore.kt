package com.nutritiontracker.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.nutritiontracker.data.model.AIProvider

class SecureKeyStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "nutrition_tracker_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveApiKey(provider: AIProvider, key: String) {
        prefs.edit().putString(provider.prefKey, key).apply()
    }

    fun getApiKey(provider: AIProvider): String {
        return prefs.getString(provider.prefKey, "") ?: ""
    }

    fun clearApiKey(provider: AIProvider) {
        prefs.edit().remove(provider.prefKey).apply()
    }

    fun hasApiKey(provider: AIProvider): Boolean {
        return getApiKey(provider).isNotBlank()
    }

    fun getSelectedProvider(): AIProvider {
        val name = prefs.getString(KEY_SELECTED_PROVIDER, AIProvider.CLAUDE.name) ?: AIProvider.CLAUDE.name
        return try {
            AIProvider.valueOf(name)
        } catch (e: IllegalArgumentException) {
            AIProvider.CLAUDE
        }
    }

    fun saveSelectedProvider(provider: AIProvider) {
        prefs.edit().putString(KEY_SELECTED_PROVIDER, provider.name).apply()
    }

    companion object {
        private const val KEY_SELECTED_PROVIDER = "selected_ai_provider"

        private val AIProvider.prefKey: String
            get() = "api_key_${name.lowercase()}"
    }
}
