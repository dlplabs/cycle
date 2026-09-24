package br.com.dlpsystems.cycle.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DriveAvatarStore @Inject constructor() {
    suspend fun save(accessToken: String, fileId: String?, bytes: ByteArray): String = withContext(Dispatchers.IO) {
        if (fileId.isNullOrBlank()) {
            create(accessToken, bytes)
        } else {
            val updated = update(accessToken, fileId, bytes)
            if (updated) fileId else create(accessToken, bytes)
        }
    }

    suspend fun download(accessToken: String, fileId: String): ByteArray = withContext(Dispatchers.IO) {
        val connection = connection(
            "https://www.googleapis.com/drive/v3/files/$fileId?alt=media",
            "GET",
            accessToken,
        )
        try {
            val code = connection.responseCode
            if (code !in 200..299) error("drive")
            connection.inputStream.use { it.readBytes() }
        } finally {
            connection.disconnect()
        }
    }

    private fun create(accessToken: String, bytes: ByteArray): String {
        val boundary = "cycleAvatar"
        val metadata = """{"name":"avatar.bin","parents":["appDataFolder"]}"""
        val body = ByteArrayOutputStream()
        body.write(
            (
                "--$boundary\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n\r\n" +
                    "$metadata\r\n" +
                    "--$boundary\r\n" +
                    "Content-Type: application/octet-stream\r\n\r\n"
                ).toByteArray(Charsets.UTF_8),
        )
        body.write(bytes)
        body.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8))
        val connection = connection(
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart",
            "POST",
            accessToken,
        )
        connection.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")
        connection.doOutput = true
        return try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()
                ?.use { it.readText() }
                .orEmpty()
            if (code !in 200..299) error("drive")
            JSONObject(text).getString("id")
        } finally {
            connection.disconnect()
        }
    }

    private fun update(accessToken: String, fileId: String, bytes: ByteArray): Boolean {
        val connection = connection(
            "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media",
            "PATCH",
            accessToken,
        )
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.doOutput = true
        return try {
            connection.outputStream.use { it.write(bytes) }
            connection.responseCode in 200..299
        } finally {
            connection.disconnect()
        }
    }

    private fun connection(url: String, method: String, accessToken: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $accessToken")
            connectTimeout = 15_000
            readTimeout = 20_000
        }
}
