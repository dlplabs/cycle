package br.com.dlpsystems.cycle.core.review

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import com.google.android.play.core.review.ReviewManagerFactory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayReviewHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataSource,
) {
    suspend fun shouldPromptReview(): Boolean {
        val completed = preferences.reviewCompleted.first()
        if (completed) return false

        val cooldownUntil = preferences.reviewCooldownUntil.first()
        if (System.currentTimeMillis() < cooldownUntil) return false

        val positiveCount = preferences.positiveActionCount.first()
        return positiveCount >= 2
    }

    suspend fun recordPositiveAction(): Boolean {
        preferences.incrementPositiveActionCount()
        return shouldPromptReview()
    }

    suspend fun markReviewCompleted() {
        preferences.setReviewCompleted(true)
    }

    suspend fun postponeReview(days: Int = 14) {
        preferences.setReviewCooldownDays(days)
    }

    fun launchInAppReview(activity: Activity, onComplete: () -> Unit = {}) {
        try {
            val manager = ReviewManagerFactory.create(activity)
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener {
                        onComplete()
                    }
                } else {
                    openPlayStore(activity)
                    onComplete()
                }
            }
        } catch (_: Throwable) {
            openPlayStore(activity)
            onComplete()
        }
    }

    fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(marketIntent)
        } catch (_: Throwable) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
        }
    }

    fun shareApp(context: Context) {
        val shareMessage = "Conheça o Cycle, o aplicativo ideal para acompanhar o seu ciclo e sua saúde: https://cycle.dlpsystems.com.br"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
