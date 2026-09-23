package br.com.dlpsystems.cycle.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferences by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val dataStore = context.userPreferences

    val disclaimerAccepted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.DISCLAIMER] ?: false
    }

    val remindersEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.REMINDERS] ?: true
    }

    val premiumCached: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.PREMIUM] ?: false
    }

    val widgetDay: Flow<Int> = dataStore.data.map { prefs -> prefs[Keys.WIDGET_DAY] ?: 0 }
    val widgetPhase: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.WIDGET_PHASE] ?: "" }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DISCLAIMER] = accepted }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.REMINDERS] = enabled }
    }

    suspend fun setPremiumCached(premium: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.PREMIUM] = premium }
    }

    suspend fun setWidgetSnapshot(day: Int, phase: String) {
        dataStore.edit { prefs ->
            prefs[Keys.WIDGET_DAY] = day
            prefs[Keys.WIDGET_PHASE] = phase
        }
    }

    private object Keys {
        val DISCLAIMER = booleanPreferencesKey("disclaimer_accepted")
        val REMINDERS = booleanPreferencesKey("reminders_enabled")
        val PREMIUM = booleanPreferencesKey("premium_cached")
        val WIDGET_DAY = intPreferencesKey("widget_day")
        val WIDGET_PHASE = stringPreferencesKey("widget_phase")
    }
}
