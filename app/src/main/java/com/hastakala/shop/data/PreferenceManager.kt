package com.hastakala.shop.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class PreferenceManager @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val THEME_COLOR = stringPreferencesKey("theme_color")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val AUTO_SAVE_BILLS = booleanPreferencesKey("auto_save_bills")
        val SALE_CONFIRMATION = booleanPreferencesKey("sale_confirmation")
        val DEFAULT_QTY = intPreferencesKey("default_qty")
        val CURRENCY = stringPreferencesKey("currency")
        val LOW_STOCK_ALERTS = booleanPreferencesKey("low_stock_alerts")
        val MIN_STOCK_THRESHOLD = floatPreferencesKey("min_stock_threshold")
        val STOCK_NOTIFICATIONS = booleanPreferencesKey("stock_notifications")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val EXPORT_FORMAT = stringPreferencesKey("export_format")
        val WEEKLY_SUMMARY = booleanPreferencesKey("weekly_summary")
        val BACKUP_REMINDERS = booleanPreferencesKey("backup_reminders")
        val LANGUAGE = stringPreferencesKey("language")
        val APP_LOCK = booleanPreferencesKey("app_lock")
        val UNLOCK_METHOD = stringPreferencesKey("unlock_method")
        val APP_PIN = stringPreferencesKey("app_pin")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
    }

    val darkModeFlow: Flow<Boolean> = context.dataStore.data.map { it[DARK_MODE] ?: false }
    val themeColorFlow: Flow<String> = context.dataStore.data.map { it[THEME_COLOR] ?: "Brown" }
    val fontSizeFlow: Flow<String> = context.dataStore.data.map { it[FONT_SIZE] ?: "Medium" }
    val autoSaveBillsFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_SAVE_BILLS] ?: true }
    val saleConfirmationFlow: Flow<Boolean> = context.dataStore.data.map { it[SALE_CONFIRMATION] ?: true }
    val defaultQtyFlow: Flow<Int> = context.dataStore.data.map { it[DEFAULT_QTY] ?: 1 }
    val currencyFlow: Flow<String> = context.dataStore.data.map { it[CURRENCY] ?: "₹" }
    val lowStockAlertsFlow: Flow<Boolean> = context.dataStore.data.map { it[LOW_STOCK_ALERTS] ?: true }
    val minStockThresholdFlow: Flow<Float> = context.dataStore.data.map { it[MIN_STOCK_THRESHOLD] ?: 5f }
    val stockNotificationsFlow: Flow<Boolean> = context.dataStore.data.map { it[STOCK_NOTIFICATIONS] ?: true }
    val autoBackupFlow: Flow<Boolean> = context.dataStore.data.map { it[AUTO_BACKUP] ?: false }
    val exportFormatFlow: Flow<String> = context.dataStore.data.map { it[EXPORT_FORMAT] ?: "PDF" }
    val weeklySummaryFlow: Flow<Boolean> = context.dataStore.data.map { it[WEEKLY_SUMMARY] ?: true }
    val backupRemindersFlow: Flow<Boolean> = context.dataStore.data.map { it[BACKUP_REMINDERS] ?: true }
    val languageFlow: Flow<String> = context.dataStore.data.map { 
        it[LANGUAGE] ?: "English"
    }
    val appLockFlow: Flow<Boolean> = context.dataStore.data.map { it[APP_LOCK] ?: false }
    val unlockMethodFlow: Flow<String> = context.dataStore.data.map { it[UNLOCK_METHOD] ?: "Biometric" }
    val appPinFlow: Flow<String?> = context.dataStore.data.map { it[APP_PIN] }
    val lastBackupTimeFlow: Flow<Long> = context.dataStore.data.map { it[LAST_BACKUP_TIME] ?: 0L }

    suspend fun <T> updatePreference(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
