package br.com.dlpsystems.cycle.data.remote

import android.content.Context
import android.os.Bundle
import br.com.dlpsystems.cycle.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

object AnalyticsEvents {
    const val EVENT_CYCLE_LOGGED = "cycle_daily_logged"
    const val EVENT_SOS_OPENED = "sos_relief_triggered"
    const val EVENT_PDF_EXPORTED = "doctor_report_exported"
    const val EVENT_EVIDENCE_CLICKED = "scientific_source_viewed"
    const val EVENT_PAYWALL_VIEWED = "paywall_screen_viewed"
    const val EVENT_SUBSCRIPTION_PURCHASED = "subscription_activated"
}

@Singleton
class AnalyticsService @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val analytics: FirebaseAnalytics? =
        if (BuildConfig.FIREBASE_CONFIGURED) FirebaseAnalytics.getInstance(context) else null

    fun log(event: String, screen: String? = null) {
        val params = Bundle()
        if (screen != null) params.putString(FirebaseAnalytics.Param.SCREEN_NAME, screen)
        analytics?.logEvent(event, params)
    }
}
