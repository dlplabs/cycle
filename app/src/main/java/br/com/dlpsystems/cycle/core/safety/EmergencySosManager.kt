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
import androidx.core.content.FileProvider
import br.com.dlpsystems.cycle.domain.model.ForensicAudioRecord
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
    data class SmsSent(val contactCount: Int, val location: Location?, val messageText: String) : SosDispatchResult()
    data class FallbackIntent(val intent: Intent, val location: Location?, val messageText: String) : SosDispatchResult()
    data class Error(val message: String) : SosDispatchResult()
}

@Singleton
class EmergencySosManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private var mediaRecorder: MediaRecorder? = null
    private var isRecordingAudio = false

    fun hasLocationPermission(): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return hasFine || hasCoarse
    }

    fun hasAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    suspend fun getFastLocation(): Location? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission()) return@withContext null

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

    suspend fun formatLocationMapsLink(): Pair<Location?, String> {
        val location = getFastLocation()
        val link = if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "Localização GPS não disponível no momento (ative a permissão de localização)"
        }
        return location to link
    }

    suspend fun dispatchEmergencySos(
        trustedPhones: List<String>,
        customMessage: String? = null,
    ): SosDispatchResult = withContext(Dispatchers.IO) {
        if (trustedPhones.isEmpty()) {
            return@withContext SosDispatchResult.Error("Adicione pelo menos 1 pessoa ao seu Círculo de Confiança para disparar o SOS.")
        }

        val (location, mapsLink) = formatLocationMapsLink()

        val baseMsg = customMessage?.ifBlank { null }
            ?: "SOS Cycle: Preciso de apoio imediato. Acompanhe minha localização:"
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
                    runCatching {
                        smsManager.sendTextMessage(phone, null, fullSms, null, null)
                        sentCount++
                    }
                }
            }
            if (sentCount > 0) {
                SosDispatchResult.SmsSent(sentCount, location, fullSms)
            } else {
                val uri = Uri.parse("smsto:${trustedPhones.joinToString(";")}")
                val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                    putExtra("sms_body", fullSms)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                SosDispatchResult.FallbackIntent(intent, location, fullSms)
            }
        } else {
            val uri = Uri.parse("smsto:${trustedPhones.joinToString(";")}")
            val intent = Intent(Intent.ACTION_SENDTO, uri).apply {
                putExtra("sms_body", fullSms)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            SosDispatchResult.FallbackIntent(intent, location, fullSms)
        }
    }

    suspend fun buildTransitStartShareIntent(
        destinationLabel: String,
        durationMinutes: Int,
    ): Intent = withContext(Dispatchers.IO) {
        val (_, mapsLink) = formatLocationMapsLink()
        val dest = destinationLabel.trim().ifBlank { "Casa" }
        val text = "Proteção em Trânsito (Cycle): Estou em deslocamento para \"$dest\" " +
            "(previsão de chegada: $durationMinutes min). " +
            "Minha localização de partida: $mapsLink"

        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Acompanhamento de Trajeto — Cycle")
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    suspend fun buildSafeArrivalShareIntent(
        destinationLabel: String,
    ): Intent = withContext(Dispatchers.IO) {
        val dest = destinationLabel.trim().ifBlank { "Casa" }
        val text = "Cheguei em segurança em \"$dest\"! Obrigada por acompanhar meu trajeto pelo Cycle."

        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Cheguei em Segurança — Cycle")
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    suspend fun buildQuickLocationShareIntent(
        customMessage: String? = null,
    ): Intent = withContext(Dispatchers.IO) {
        val (_, mapsLink) = formatLocationMapsLink()
        val prefix = customMessage?.trim()?.ifBlank { null }
            ?: "Círculo de Confiança (Cycle): Compartilhando minha localização atual para sua tranquilidade:"
        val text = "$prefix $mapsLink"

        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Minha Localização Atual — Cycle")
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    fun startForensicAudioRecording(): File? {
        if (isRecordingAudio) return null
        if (!hasAudioPermission()) return null

        return try {
            val secureDir = getSecureAudioDir()
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val audioFile = File(secureDir, "evidencia_$timestamp.m4a")

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
            vibrateConfirmation()
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

    fun listForensicAudioRecords(): List<ForensicAudioRecord> {
        val dir = getSecureAudioDir()
        val files = dir.listFiles { file ->
            file.isFile && (file.name.endsWith(".m4a") || file.name.endsWith(".mp4"))
        } ?: return emptyList()

        return files
            .sortedByDescending { it.lastModified() }
            .map { file ->
                ForensicAudioRecord(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    createdAtMillis = file.lastModified(),
                    sizeBytes = file.length(),
                )
            }
    }

    fun deleteForensicAudioRecord(filePath: String): Boolean {
        return runCatching {
            val file = File(filePath)
            if (file.exists() && file.parentFile?.name == "secure_audio_evidence") {
                file.delete()
            } else {
                false
            }
        }.getOrDefault(false)
    }

    fun buildShareAudioEvidenceIntent(filePath: String): Intent? {
        return runCatching {
            val file = File(filePath)
            if (!file.exists()) return null
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file,
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "audio/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Registro de Áudio — Cofre de Segurança Cycle")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }.getOrNull()
    }

    private fun getSecureAudioDir(): File {
        val secureDir = File(context.filesDir, "secure_audio_evidence")
        if (!secureDir.exists()) secureDir.mkdirs()
        return secureDir
    }

    fun vibrateConfirmation() {
        vibratePattern(longArrayOf(0, 80, 50, 120))
    }

    fun vibrateAlert() {
        vibratePattern(longArrayOf(0, 250, 120, 250, 120, 400))
    }

    private fun vibratePattern(pattern: LongArray) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val effect = CombinedVibration.createParallel(
                    VibrationEffect.createWaveform(pattern, -1),
                )
                vibratorManager.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(250)
                }
            }
        } catch (_: Exception) {
        }
    }
}

