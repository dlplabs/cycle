package br.com.dlpsystems.cycle.core.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import br.com.dlpsystems.cycle.MainActivity
import br.com.dlpsystems.cycle.R
import br.com.dlpsystems.cycle.domain.model.ContraceptiveAlarmConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

const val PILL_NOTIFICATION_CHANNEL_ID = "pill_reminder_channel"
const val PILL_ALARM_ACTION = "br.com.dlpsystems.cycle.ACTION_PILL_ALARM"
const val PILL_TAKEN_ACTION = "br.com.dlpsystems.cycle.ACTION_PILL_TAKEN"
const val PILL_SNOOZE_ACTION = "br.com.dlpsystems.cycle.ACTION_PILL_SNOOZE"

@Singleton
class ContraceptiveAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    init {
        createNotificationChannel()
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun scheduleAlarm(config: ContraceptiveAlarmConfig) {
        val intent = Intent(context, ContraceptiveAlarmReceiver::class.java).apply {
            action = PILL_ALARM_ACTION
            putExtra("hideSensitive", config.hideSensitiveInfoOnLockScreen)
            putExtra("snoozeMinutes", config.snoozeMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PILL_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (!config.isEnabled) {
            alarmManager.cancel(pendingIntent)
            return
        }

        // Determina o próximo disparo hoje ou amanhã
        val now = LocalDateTime.now()
        var targetTime = LocalDateTime.of(LocalDate.now(), config.reminderTime)
        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1)
        }

        val triggerEpochMs = targetTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
        }
    }

    fun scheduleSnooze(snoozeMinutes: Int, hideSensitive: Boolean) {
        val intent = Intent(context, ContraceptiveAlarmReceiver::class.java).apply {
            action = PILL_ALARM_ACTION
            putExtra("hideSensitive", hideSensitive)
            putExtra("snoozeMinutes", snoozeMinutes)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            PILL_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val triggerEpochMs = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000L)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
            }
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerEpochMs, pendingIntent)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                PILL_NOTIFICATION_CHANNEL_ID,
                "Lembretes de Anticoncepcional",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notificações pontuais para lembrete do método contraceptivo"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val PILL_ALARM_REQUEST_CODE = 4001
        const val PILL_NOTIFICATION_ID = 4002
    }
}

class ContraceptiveAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> {
                // Reagenda o alarme usando a configuração persistida
                val appContext = context.applicationContext
                val scheduler = ContraceptiveAlarmScheduler(appContext)
                // Reagenda por segurança
                scheduler.scheduleAlarm(ContraceptiveAlarmConfig(isEnabled = true))
            }
            PILL_ALARM_ACTION -> {
                val hideSensitive = intent.getBooleanExtra("hideSensitive", true)
                val snoozeMinutes = intent.getIntExtra("snoozeMinutes", 15)
                showNotification(context, hideSensitive, snoozeMinutes)
            }
            PILL_TAKEN_ACTION -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(ContraceptiveAlarmScheduler.PILL_NOTIFICATION_ID)
            }
            PILL_SNOOZE_ACTION -> {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(ContraceptiveAlarmScheduler.PILL_NOTIFICATION_ID)
                val scheduler = ContraceptiveAlarmScheduler(context)
                val snoozeMinutes = intent.getIntExtra("snoozeMinutes", 15)
                val hideSensitive = intent.getBooleanExtra("hideSensitive", true)
                scheduler.scheduleSnooze(snoozeMinutes, hideSensitive)
            }
        }
    }

    private fun showNotification(context: Context, hideSensitive: Boolean, snoozeMinutes: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val contentIntent = Intent(context, MainActivity::class.java)
        val pendingContentIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val takenIntent = Intent(context, ContraceptiveAlarmReceiver::class.java).apply {
            action = PILL_TAKEN_ACTION
        }
        val takenPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            takenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val snoozeIntent = Intent(context, ContraceptiveAlarmReceiver::class.java).apply {
            action = PILL_SNOOZE_ACTION
            putExtra("snoozeMinutes", snoozeMinutes)
            putExtra("hideSensitive", hideSensitive)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            2,
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val title = if (hideSensitive) "Lembrete de Cuidado" else "Hora do Anticoncepcional"
        val message = if (hideSensitive) "Momento do seu registro diário de saúde." else "Hora de tomar seu anticoncepcional conforme seu esquema habitual."

        val notification = NotificationCompat.Builder(context, PILL_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_lua_lavanda)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(if (hideSensitive) NotificationCompat.VISIBILITY_PRIVATE else NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(pendingContentIntent)
            .addAction(0, "Tomei agora", takenPendingIntent)
            .addAction(0, "Adiar $snoozeMinutes min", snoozePendingIntent)
            .build()

        notificationManager.notify(ContraceptiveAlarmScheduler.PILL_NOTIFICATION_ID, notification)
    }
}
