package com.bankmanaging.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.bankmanaging.app.model.DefaultSmsPatterns
import com.bankmanaging.app.model.SmsPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "bank_managing_prefs")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

class PreferencesRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private object Keys {
        val BANK_SENDERS = stringPreferencesKey("bank_senders_json")
        val SMS_PATTERNS = stringPreferencesKey("sms_patterns_json")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PERSIAN_DIGITS = booleanPreferencesKey("use_persian_digits")
    }

    val bankSenders: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.BANK_SENDERS]
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            runCatching { json.decodeFromString<List<String>>(raw) }.getOrDefault(emptyList())
        }
    }

    val smsPatterns: Flow<List<SmsPattern>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.SMS_PATTERNS]
        if (raw.isNullOrBlank()) {
            DefaultSmsPatterns.allDefaults
        } else {
            runCatching { json.decodeFromString<List<SmsPattern>>(raw) }
                .getOrDefault(DefaultSmsPatterns.allDefaults)
                .ifEmpty { DefaultSmsPatterns.allDefaults }
        }
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            ThemeMode.LIGHT.name -> ThemeMode.LIGHT
            ThemeMode.DARK.name -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    val usePersianDigits: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.PERSIAN_DIGITS] ?: true
    }

    suspend fun saveBankSenders(senders: List<String>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BANK_SENDERS] = json.encodeToString(senders)
        }
    }

    suspend fun saveSmsPatterns(patterns: List<SmsPattern>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SMS_PATTERNS] = json.encodeToString(patterns)
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setUsePersianDigits(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PERSIAN_DIGITS] = enabled
        }
    }
}
