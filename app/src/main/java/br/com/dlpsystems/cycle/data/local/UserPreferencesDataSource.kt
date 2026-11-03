package br.com.dlpsystems.cycle.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import br.com.dlpsystems.cycle.domain.model.LifeStageMode
import br.com.dlpsystems.cycle.domain.model.TransitProtectionSession
import br.com.dlpsystems.cycle.domain.model.TrustedContact
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    val cycleRemindersEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.CYCLE_REMINDERS] ?: true
    }

    val premiumCached: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.PREMIUM] ?: false
    }

    val widgetDay: Flow<Int> = dataStore.data.map { prefs -> prefs[Keys.WIDGET_DAY] ?: 0 }
    val widgetPhase: Flow<String> = dataStore.data.map { prefs -> prefs[Keys.WIDGET_PHASE] ?: "" }

    val seenCoachMarks: Flow<Set<String>> = dataStore.data.map { prefs ->
        prefs[Keys.COACH_MARKS] ?: emptySet()
    }

    val lastNotifiedPhase: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_PHASE]
    }

    val lastNotifiedDate: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_PHASE_DATE]
    }

    val lastDailyReminderDate: Flow<String?> = dataStore.data.map { prefs ->
        prefs[Keys.LAST_DAILY_REMINDER_DATE]
    }

    val welcomeSeen: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.WELCOME_SEEN] ?: false
    }

    val activeLifeStageMode: Flow<LifeStageMode> = dataStore.data.map { prefs ->
        val raw = prefs[Keys.ACTIVE_LIFE_STAGE_MODE] ?: LifeStageMode.REGULAR_CYCLE.name
        runCatching { LifeStageMode.valueOf(raw) }.getOrDefault(LifeStageMode.REGULAR_CYCLE)
    }

    val reviewCompleted: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.REVIEW_COMPLETED] ?: false
    }

    val reviewCooldownUntil: Flow<Long> = dataStore.data.map { prefs ->
        prefs[Keys.REVIEW_COOLDOWN_UNTIL] ?: 0L
    }

    val positiveActionCount: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.POSITIVE_ACTION_COUNT] ?: 0
    }

    val trustedContacts: Flow<List<TrustedContact>> = dataStore.data.map { prefs ->
        val rawSet = prefs[Keys.TRUSTED_CONTACTS] ?: emptySet()
        rawSet.mapNotNull { TrustedContact.decode(it) }
            .sortedBy { it.name.lowercase() }
    }

    val customSosMessage: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.CUSTOM_SOS_MESSAGE]
            ?: "SOS Cycle: Preciso de apoio imediato. Acompanhe minha localização:"
    }

    val activeTransitSession: Flow<TransitProtectionSession?> = dataStore.data.map { prefs ->
        TransitProtectionSession.decode(prefs[Keys.ACTIVE_TRANSIT_SESSION])
    }

    suspend fun setTrustedContacts(contacts: List<TrustedContact>) {
        dataStore.edit { prefs ->
            prefs[Keys.TRUSTED_CONTACTS] = contacts.map { it.encode() }.toSet()
        }
    }

    suspend fun setCustomSosMessage(message: String) {
        dataStore.edit { prefs ->
            prefs[Keys.CUSTOM_SOS_MESSAGE] = message.trim()
        }
    }

    suspend fun setActiveTransitSession(session: TransitProtectionSession?) {
        dataStore.edit { prefs ->
            if (session == null) {
                prefs.remove(Keys.ACTIVE_TRANSIT_SESSION)
            } else {
                prefs[Keys.ACTIVE_TRANSIT_SESSION] = session.encode()
            }
        }
    }

    suspend fun setActiveLifeStageMode(mode: LifeStageMode) {
        dataStore.edit { prefs -> prefs[Keys.ACTIVE_LIFE_STAGE_MODE] = mode.name }
    }

    suspend fun setWelcomeSeen(seen: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.WELCOME_SEEN] = seen }
    }

    suspend fun setDisclaimerAccepted(accepted: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.DISCLAIMER] = accepted }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.REMINDERS] = enabled }
    }

    suspend fun setCycleRemindersEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.CYCLE_REMINDERS] = enabled }
    }

    suspend fun setLastDailyReminderDate(date: String) {
        dataStore.edit { prefs -> prefs[Keys.LAST_DAILY_REMINDER_DATE] = date }
    }

    suspend fun setPremiumCached(premium: Boolean) {
        dataStore.edit { prefs -> prefs[Keys.PREMIUM] = premium }
    }

    suspend fun markCoachSeen(key: String) {
        dataStore.edit { prefs ->
            prefs[Keys.COACH_MARKS] = (prefs[Keys.COACH_MARKS] ?: emptySet()) + key
        }
    }

    suspend fun setLastNotifiedPhase(date: String, phase: String) {
        dataStore.edit { prefs ->
            prefs[Keys.LAST_PHASE_DATE] = date
            prefs[Keys.LAST_PHASE] = phase
        }
    }

    suspend fun setWidgetSnapshot(day: Int, phase: String) {
        dataStore.edit { prefs ->
            prefs[Keys.WIDGET_DAY] = day
            prefs[Keys.WIDGET_PHASE] = phase
        }
    }

    suspend fun incrementPositiveActionCount(): Int {
        var count = 0
        dataStore.edit { prefs ->
            val current = prefs[Keys.POSITIVE_ACTION_COUNT] ?: 0
            count = current + 1
            prefs[Keys.POSITIVE_ACTION_COUNT] = count
        }
        return count
    }

    suspend fun setReviewCompleted(completed: Boolean = true) {
        dataStore.edit { prefs -> prefs[Keys.REVIEW_COMPLETED] = completed }
    }

    suspend fun setReviewCooldownDays(days: Int = 14) {
        val cooldownMs = System.currentTimeMillis() + days * 24L * 60L * 60L * 1000L
        dataStore.edit { prefs -> prefs[Keys.REVIEW_COOLDOWN_UNTIL] = cooldownMs }
    }

    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    private object Keys {
        val DISCLAIMER = booleanPreferencesKey("disclaimer_accepted")
        val WELCOME_SEEN = booleanPreferencesKey("welcome_seen")
        val REMINDERS = booleanPreferencesKey("reminders_enabled")
        val CYCLE_REMINDERS = booleanPreferencesKey("cycle_reminders_enabled")
        val PREMIUM = booleanPreferencesKey("premium_cached")
        val WIDGET_DAY = intPreferencesKey("widget_day")
        val WIDGET_PHASE = stringPreferencesKey("widget_phase")
        val COACH_MARKS = stringSetPreferencesKey("coach_marks_seen")
        val LAST_PHASE = stringPreferencesKey("last_notified_phase")
        val LAST_PHASE_DATE = stringPreferencesKey("last_notified_phase_date")
        val LAST_DAILY_REMINDER_DATE = stringPreferencesKey("last_daily_reminder_date")
        val ACTIVE_LIFE_STAGE_MODE = stringPreferencesKey("active_life_stage_mode")
        val TRUSTED_CONTACTS = stringSetPreferencesKey("safety_trusted_contacts")
        val CUSTOM_SOS_MESSAGE = stringPreferencesKey("safety_custom_sos_message")
        val ACTIVE_TRANSIT_SESSION = stringPreferencesKey("safety_active_transit_session")
        val REVIEW_COMPLETED = booleanPreferencesKey("review_completed")
        val REVIEW_COOLDOWN_UNTIL = longPreferencesKey("review_cooldown_until")
        val POSITIVE_ACTION_COUNT = intPreferencesKey("review_positive_action_count")
    }
}
