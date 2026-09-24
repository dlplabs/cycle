package br.com.dlpsystems.cycle.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.max

object AvatarCompressor {
    private const val MAX_EDGE = 512
    private const val QUALITY = 80

    fun jpeg(source: ByteArray): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(source, 0, source.size, bounds)
        val decoded = BitmapFactory.decodeByteArray(
            source,
            0,
            source.size,
            BitmapFactory.Options().apply { inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight) },
        ) ?: error("avatar")
        return encode(decoded)
    }

    fun jpeg(context: Context, uri: Uri): ByteArray {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        val sample = sampleSize(bounds.outWidth, bounds.outHeight)
        val decoded = BitmapFactory.Options().apply { inSampleSize = sample }.let { options ->
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }
        } ?: error("avatar")
        return encode(decoded)
    }

    private fun encode(decoded: Bitmap): ByteArray {
        val longest = max(decoded.width, decoded.height).coerceAtLeast(1)
        val bitmap = if (longest > MAX_EDGE) {
            val scale = MAX_EDGE.toFloat() / longest
            Bitmap.createScaledBitmap(
                decoded,
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
                true,
            )
        } else {
            decoded
        }
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, output)
            if (bitmap !== decoded) bitmap.recycle()
            decoded.recycle()
            output.toByteArray()
        }
    }

    private fun sampleSize(width: Int, height: Int): Int {
        var sample = 1
        val longest = max(width, height)
        while (longest / sample > MAX_EDGE * 2) sample *= 2
        return sample
    }
}
