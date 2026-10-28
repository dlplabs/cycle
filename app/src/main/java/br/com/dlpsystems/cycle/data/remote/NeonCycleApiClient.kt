package br.com.dlpsystems.cycle.data.remote

import br.com.dlpsystems.cycle.domain.model.DailyLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NeonCycleApiClient @Inject constructor() {

    companion object {
        private const val BASE_API_URL = "https://appcycle.com.br/api/cycle"
        private const val CONNECT_TIMEOUT_MS = 15_000
        private const val READ_TIMEOUT_MS = 20_000
    }

    data class PresignUploadResult(
        val uploadUrl: String,
        val fileKey: String,
        val method: String,
        val contentType: String,
        val maxSizeBytes: Long,
    )

    data class SyncResult(
        val success: Boolean,
        val savedLogsCount: Int,
        val timestamp: String,
    )

    /**
     * Sincroniza configurações e logs diários com o Neon PostgreSQL
     */
    suspend fun syncWithNeon(
        userId: String,
        activeLifeStage: String,
        lastPeriodDate: String,
        cycleLength: Int,
        periodLength: Int,
        logs: List<DailyLog>,
    ): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_API_URL/sync")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val payload = JSONObject().apply {
                put("userId", userId)
                put("settings", JSONObject().apply {
                    put("activeLifeStageMode", activeLifeStage)
                    put("lastPeriodDate", lastPeriodDate)
                    put("cycleLengthDays", cycleLength)
                    put("periodLengthDays", periodLength)
                })

                val logsArray = JSONArray()
                logs.forEach { log ->
                    logsArray.put(JSONObject().apply {
                        put("date", log.date.toString())
                        put("cycleDay", log.cycleDay)
                        put("phase", log.phase.name.lowercase())
                        put("mood", log.mood?.name?.lowercase())
                        put("flowIntensity", log.flowIntensity?.name?.lowercase())
                        put("skin", log.skin?.name?.lowercase())
                        put("painLevel", log.painLevel)
                        put("notes", log.notes)
                        val symptomsJson = JSONArray()
                        log.symptoms.forEach { symptomsJson.put(it.name.lowercase()) }
                        put("symptoms", symptomsJson)
                    })
                }
                put("logs", logsArray)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                Result.success(
                    SyncResult(
                        success = json.optBoolean("success", true),
                        savedLogsCount = json.optInt("savedLogsCount", 0),
                        timestamp = json.optString("timestamp", ""),
                    )
                )
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP $responseCode Neon Sync: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Solicita URL pré-assinada para upload de laudo/exame médico no Cloudflare R2
     */
    suspend fun requestR2UploadPresignedUrl(
        userId: String,
        fileName: String,
        mimeType: String,
        category: String = "EXAM",
    ): Result<PresignUploadResult> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_API_URL/storage/presign")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val body = JSONObject().apply {
                put("userId", userId)
                put("fileName", fileName)
                put("mimeType", mimeType)
                put("category", category)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val data = json.getJSONObject("data")
                Result.success(
                    PresignUploadResult(
                        uploadUrl = data.getString("uploadUrl"),
                        fileKey = data.getString("fileKey"),
                        method = data.getString("method"),
                        contentType = data.getJSONObject("headers").optString("Content-Type", mimeType),
                        maxSizeBytes = data.optLong("maxSizeBytes", 25 * 1024 * 1024),
                    )
                )
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP $responseCode R2 Presign: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Efetua o upload do arquivo binário diretamente para o Cloudflare R2
     */
    suspend fun uploadFileToR2(
        uploadUrl: String,
        contentType: String,
        bytes: ByteArray,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val url = URL(uploadUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "PUT"
                doOutput = true
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                setRequestProperty("Content-Type", contentType)
                setFixedLengthStreamingMode(bytes.size)
            }

            connection.outputStream.use { os ->
                os.write(bytes)
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                Result.success(Unit)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP $responseCode Upload R2: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
