package br.com.dlpsystems.cycle.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import br.com.dlpsystems.cycle.MainActivity
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.data.local.UserPreferencesDataSource
import br.com.dlpsystems.cycle.domain.model.CyclePhase
import br.com.dlpsystems.cycle.domain.model.PhaseStatus
import br.com.dlpsystems.cycle.domain.repository.CycleRepository
import br.com.dlpsystems.cycle.domain.repository.UserRepository
import br.com.dlpsystems.cycle.domain.usecase.CalculateCurrentPhaseUseCase
import br.com.dlpsystems.cycle.domain.usecase.PhaseCalculationInput
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class PhaseReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val preferences: UserPreferencesDataSource,
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val calculateCurrentPhase: CalculateCurrentPhaseUseCase,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!preferences.remindersEnabled.first()) return Result.success()
        if (!userRepository.isBackendAvailable) return Result.success()
        if (userRepository.observeAuth().first() == null) return Result.success()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.success()
        }

        val profile = userRepository.getProfile()
        val cycles = cycleRepository.getCycles()
        val today = LocalDate.now()
        val todayResult = calculateCurrentPhase(
            PhaseCalculationInput(today, profile, cycles),
        )
        val todayPhase = todayResult.phase
        if (todayResult.status != PhaseStatus.IN_PHASE || todayPhase == null) {
            return Result.success()
        }
        val lastPhase = preferences.lastNotifiedPhase.first()
        val storedDate = preferences.lastNotifiedDate.first()?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (lastPhase == null || storedDate == null) {
            preferences.setLastNotifiedPhase(today.toString(), todayPhase.name)
            return Result.success()
        }
        var cursor: LocalDate = storedDate
        var previous: String = lastPhase
        val entered = mutableListOf<Pair<CyclePhase, Int>>()
        while (cursor.isBefore(today)) {
            cursor = cursor.plusDays(1)
            val day = calculateCurrentPhase(PhaseCalculationInput(cursor, profile, cycles))
            val phase = day.phase ?: continue
            if (day.status == PhaseStatus.IN_PHASE && phase.name != previous) {
                entered.add(phase to day.cycleDay)
                previous = phase.name
            }
        }
        entered.takeLast(4).forEach { (phase, cycleDay) ->
            val phaseName = applicationContext.getString(phase.labelRes())
            showNotification(
                phase.ordinal,
                applicationContext.getString(R.string.reminder_title),
                applicationContext.getString(R.string.reminder_body, phaseName, cycleDay),
            )
        }
        preferences.setLastNotifiedPhase(today.toString(), todayPhase.name)
        return Result.success()
    }

    private fun showNotification(idOffset: Int, title: String, body: String) {
        createChannel()
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_phase)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        val manager = NotificationManagerCompat.from(applicationContext)
        val allowed = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!allowed || !manager.areNotificationsEnabled()) return
        try {
            manager.notify(NOTIFICATION_ID + idOffset, notification)
        } catch (_: SecurityException) {
            return
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            applicationContext.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        manager.createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "phase_reminders"
        private const val NOTIFICATION_ID = 41
    }
}

fun CyclePhase.labelRes(): Int = when (this) {
    CyclePhase.MENSTRUAL -> R.string.phase_menstrual
    CyclePhase.FOLLICULAR -> R.string.phase_follicular
    CyclePhase.OVULATORY -> R.string.phase_ovulatory
    CyclePhase.LUTEAL -> R.string.phase_luteal
}
