package br.com.dlpsystems.cycle.data.local

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.size.Precision
import coil.size.Scale
import java.io.ByteArrayOutputStream

object AvatarCompressor {
    private const val MAX_EDGE = 512
    private const val QUALITY = 80

    suspend fun jpeg(context: Context, data: Any): ByteArray {
        val request = ImageRequest.Builder(context)
            .data(data)
            .size(MAX_EDGE, MAX_EDGE)
            .scale(Scale.FIT)
            .precision(Precision.INEXACT)
            .allowHardware(false)
            .build()
        val result = context.imageLoader.execute(request)
        val drawable = (result as? SuccessResult)?.drawable ?: error("avatar")
        val targetWidth = if (drawable.intrinsicWidth > 0) {
            drawable.intrinsicWidth.coerceAtMost(MAX_EDGE)
        } else {
            MAX_EDGE
        }
        val targetHeight = if (drawable.intrinsicHeight > 0) {
            drawable.intrinsicHeight.coerceAtMost(MAX_EDGE)
        } else {
            MAX_EDGE
        }
        val bitmap = drawable.toBitmap(
            width = targetWidth,
            height = targetHeight,
            config = Bitmap.Config.ARGB_8888,
        )
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, output)
            output.toByteArray()
        }
    }
}
