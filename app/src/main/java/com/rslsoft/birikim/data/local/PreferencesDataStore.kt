package com.rslsoft.birikim.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "birikim_preferences")

@Singleton
class PreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val BALANCE = longPreferencesKey("balance")
        val HIDE_BALANCE = booleanPreferencesKey("hide_balance")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val DEFAULT_PERIOD = stringPreferencesKey("default_period")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val balance: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.BALANCE] ?: 0L
    }

    val hideBalance: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HIDE_BALANCE] ?: false
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    val biometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.BIOMETRIC_ENABLED] ?: false
    }

    val defaultPeriod: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.DEFAULT_PERIOD] ?: "MONTHLY"
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true
    }

    val appLanguage: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.APP_LANGUAGE] ?: "system"
    }

    suspend fun setBalance(balance: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BALANCE] = balance
        }
    }

    suspend fun setHideBalance(hide: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HIDE_BALANCE] = hide
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setDefaultPeriod(period: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.DEFAULT_PERIOD] = period
        }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun setAppLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.APP_LANGUAGE] = language
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
