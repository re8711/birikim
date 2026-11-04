package com.rslsoft.birikim.ui.screens.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslsoft.birikim.data.local.PreferencesDataStore
import com.rslsoft.birikim.data.repository.CalculationHistoryRepository
import com.rslsoft.birikim.data.repository.GoalRepository
import com.rslsoft.birikim.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val hideBalanceDefault: Boolean = false,
    val biometricEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val appLanguage: String = "system",
    val exportMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesDataStore: PreferencesDataStore,
    private val transactionRepository: TransactionRepository,
    private val goalRepository: GoalRepository,
    private val calculationHistoryRepository: CalculationHistoryRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                preferencesDataStore.themeMode,
                preferencesDataStore.hideBalance,
                preferencesDataStore.biometricEnabled,
                preferencesDataStore.notificationsEnabled,
                preferencesDataStore.appLanguage
            ) { theme, hide, bio, notif, lang ->
                SettingsUiState(
                    themeMode = theme,
                    hideBalanceDefault = hide,
                    biometricEnabled = bio,
                    notificationsEnabled = notif,
                    appLanguage = lang
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setAppLanguage(language: String) {
        viewModelScope.launch {
            preferencesDataStore.setAppLanguage(language)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesDataStore.setThemeMode(mode)
        }
    }

    fun setHideBalanceDefault(hide: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setHideBalance(hide)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setBiometricEnabled(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesDataStore.setNotificationsEnabled(enabled)
        }
    }

    fun exportDataToCsv() {
        viewModelScope.launch {
            try {
                val transactions = transactionRepository.getAll().first()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("tr", "TR"))

                val sb = StringBuilder()
                sb.append("ID;Başlık;Tutar (TL);Tür;Tarih\n")

                transactions.forEach { t ->
                    val lira = t.amount / 100.0
                    val date = dateFormat.format(Date(t.createdAt))
                    sb.append("${t.id};\"${t.title}\";$lira;${t.type.name};$date\n")
                }

                val fileName = "birikim_islemler_${System.currentTimeMillis()}.csv"
                val file = File(context.getExternalFilesDir(null), fileName)
                file.writeText(sb.toString())

                _uiState.update { it.copy(exportMessage = "Veriler indirilenler klasörüne aktarıldı: $fileName") }
            } catch (e: Exception) {
                _uiState.update { it.copy(exportMessage = "Dışa aktarma hatası: ${e.message}") }
            }
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            transactionRepository.deleteAll()
            goalRepository.getAll().first().forEach { goalRepository.delete(it) }
            calculationHistoryRepository.deleteAll()
            preferencesDataStore.clearAll()
        }
    }
}