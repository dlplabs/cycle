package br.com.dlpsystems.cycle.core.safety

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed class SosDispatchResult {
    data class SmsSent(val contactCount: Int, val location: Location?) : SosDispatchResult()
    data class FallbackIntent(val intent: Intent, val location: Location?) : SosDispatchResult()
    data class Error(val message: String) : SosDispatchResult()
}

@Singleton
class EmergencySosManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var mediaRecorder: MediaRecorder? = null
    private var isRecordingAudio = false

    suspend fun getFastLocation(): Location? = withContext(Dispatchers.IO) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) return@withContext null

        try {
            val tokenSource = CancellationTokenSource()
            val currentLocation = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                tokenSource.token,
            ).await()

            currentLocation ?: fusedLocationClient.lastLocation.await()
        } catch (_: Exception) {
            runCatching { fusedLocationClient.lastLocation.await() }.getOrNull()
        }
    }

    suspend fun dispatchEmergencySos(
        trustedPhones: List<String>,
        customMessage: String? = null,
    ): SosDispatchResult = withContext(Dispatchers.IO) {
        if (trustedPhones.isEmpty()) {
            return@withContext SosDispatchResult.Error("Nenhum contato de emergência cadastrado.")
        }

        val location = getFastLocation()
        val mapsLink = if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "Localização indisponível no momento"
        }

        val baseMsg = customMessage?.ifBlank { null }
            ?: "SOS Cycle: Preciso de ajuda urgente. Minha localização:"
        val fullSms = "$baseMsg $mapsLink"

        vibrateConfirmation()

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS,
        ) == PackageManager.PERMISSION_GRANTED

        if (hasSmsPermission) {
            var sentCount = 0
            val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            trustedPhones.forEach { phone ->
                try {
                    val parts = smsManager.divideMessage(fullSms)
                    smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
                    sentCount++
                } catch (_: Exception) {
                    smsManager.sendTextMessage(phone, null, fullSms, null, null)
                    sentCount++
                }
            }
            SosDispatchResult.SmsSent(sentCount, location)
        } else {
            // Fallback para Intent de SMS
            val uri = Uri.parse("smsto:${trustedPhones.joinToString(";")}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", fullSms)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            SosDispatchResult.FallbackIntent(intent, location)
        }
    }

    fun startForensicAudioRecording(): File? {
        if (isRecordingAudio) return null
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasMic) return null

        return try {
            val secureDir = File(context.filesDir, "secure_audio_evidence")
            if (!secureDir.exists()) secureDir.mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val audioFile = File(secureDir, "evidence_$timestamp.m4a")

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            mediaRecorder = recorder
            isRecordingAudio = true
            audioFile
        } catch (_: Exception) {
            stopForensicAudioRecording()
            null
        }
    }

    fun stopForensicAudioRecording() {
        if (!isRecordingAudio) return
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (_: Exception) {
        } finally {
            mediaRecorder = null
            isRecordingAudio = false
        }
    }

    private fun vibrateConfirmation() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val effect = CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1),
                )
                vibratorManager.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 80, 50, 120), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            }
        } catch (_: Exception) {
        }
    }
}
