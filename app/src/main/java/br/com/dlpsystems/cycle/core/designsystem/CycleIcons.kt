package br.com.dlpsystems.cycle.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

object CycleIcons {
    val AppMark: ImageVector
        get() = appMark ?: ImageVector.Builder(
            name = "AppMark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            ringArc(stroke = Color(0xFFB34A4A), start = -77f, sweep = 72f)
            ringArc(stroke = Color(0xFF3D7858), start = 13f, sweep = 72f)
            ringArc(stroke = Color(0xFFB87326), start = 103f, sweep = 72f)
            ringArc(stroke = Color(0xFF65538A), start = 193f, sweep = 72f)
        }.build().also { appMark = it }

    val Menstrual: ImageVector
        get() = menstrual ?: strokeIcon("Menstrual") {
            moveTo(12f, 3.2f)
            curveTo(12f, 3.2f, 6.2f, 9.4f, 6.2f, 14.2f)
            arcTo(5.8f, 5.8f, 0f, true, false, 17.8f, 14.2f)
            curveTo(17.8f, 9.4f, 12f, 3.2f, 12f, 3.2f)
            close()
        }.also { menstrual = it }

    val Follicular: ImageVector
        get() = follicular ?: strokeIcon("Follicular") {
            moveTo(12f, 20f)
            curveTo(12f, 20f, 5.5f, 16.5f, 6f, 10.5f)
            curveTo(6.4f, 6.2f, 10f, 4f, 12f, 4f)
            curveTo(14f, 4f, 17.6f, 6.2f, 18f, 10.5f)
            curveTo(18.5f, 16.5f, 12f, 20f, 12f, 20f)
            close()
            moveTo(12f, 20f)
            lineTo(12f, 8f)
        }.also { follicular = it }

    val Ovulatory: ImageVector
        get() = ovulatory ?: strokeIcon("Ovulatory") {
            moveTo(12f, 8.2f)
            arcTo(3.8f, 3.8f, 0f, true, true, 11.99f, 8.2f)
            moveTo(12f, 2.8f)
            lineTo(12f, 5f)
            moveTo(12f, 19f)
            lineTo(12f, 21.2f)
            moveTo(2.8f, 12f)
            lineTo(5f, 12f)
            moveTo(19f, 12f)
            lineTo(21.2f, 12f)
            moveTo(5.4f, 5.4f)
            lineTo(6.9f, 6.9f)
            moveTo(17.1f, 17.1f)
            lineTo(18.6f, 18.6f)
            moveTo(18.6f, 5.4f)
            lineTo(17.1f, 6.9f)
            moveTo(6.9f, 17.1f)
            lineTo(5.4f, 18.6f)
        }.also { ovulatory = it }

    val Luteal: ImageVector
        get() = luteal ?: strokeIcon("Luteal") {
            moveTo(14.5f, 4.5f)
            arcTo(7.2f, 7.2f, 0f, true, false, 14.5f, 19.5f)
            arcTo(5.2f, 5.2f, 0f, true, true, 14.5f, 4.5f)
            close()
        }.also { luteal = it }

    val Nutrition: ImageVector
        get() = nutrition ?: strokeIcon("Nutrition") {
            moveTo(12f, 20.5f)
            curveTo(8f, 16.8f, 5.5f, 13.6f, 5.5f, 10.4f)
            arcTo(3.2f, 3.2f, 0f, false, true, 12f, 9.2f)
            arcTo(3.2f, 3.2f, 0f, false, true, 18.5f, 10.4f)
            curveTo(18.5f, 13.6f, 16f, 16.8f, 12f, 20.5f)
            close()
            moveTo(12f, 9.2f)
            curveTo(12f, 6.5f, 13.6f, 4.2f, 16.2f, 3.2f)
        }.also { nutrition = it }

    val Exercise: ImageVector
        get() = exercise ?: strokeIcon("Exercise") {
            moveTo(3f, 14f)
            curveTo(5.2f, 14f, 5.8f, 9f, 8f, 9f)
            curveTo(10.2f, 9f, 10.8f, 15f, 13f, 15f)
            curveTo(15.2f, 15f, 15.8f, 10f, 18f, 10f)
            curveTo(19.4f, 10f, 20.2f, 12f, 21f, 12f)
        }.also { exercise = it }

    val Skincare: ImageVector
        get() = skincare ?: strokeIcon("Skincare") {
            moveTo(12f, 3f)
            curveTo(12f, 3f, 7f, 8.6f, 7f, 13.2f)
            arcTo(5f, 5f, 0f, true, false, 17f, 13.2f)
            curveTo(17f, 8.6f, 12f, 3f, 12f, 3f)
            close()
            moveTo(9.2f, 14.2f)
            curveTo(9.8f, 16.2f, 11.2f, 17.2f, 12.8f, 17.2f)
        }.also { skincare = it }

    val Mind: ImageVector
        get() = mind ?: strokeIcon("Mind") {
            moveTo(12f, 4.2f)
            arcTo(7.8f, 7.8f, 0f, true, true, 11.99f, 4.2f)
            moveTo(12f, 8.2f)
            arcTo(3.8f, 3.8f, 0f, true, true, 11.99f, 8.2f)
        }.also { mind = it }

    private var appMark: ImageVector? = null
    private var menstrual: ImageVector? = null
    private var follicular: ImageVector? = null
    private var ovulatory: ImageVector? = null
    private var luteal: ImageVector? = null
    private var nutrition: ImageVector? = null
    private var exercise: ImageVector? = null
    private var skincare: ImageVector? = null
    private var mind: ImageVector? = null
}

private fun ImageVector.Builder.ringArc(stroke: Color, start: Float, sweep: Float) {
    val radius = 8f
    val center = 12f
    fun point(angle: Float): Pair<Float, Float> {
        val radians = Math.toRadians(angle.toDouble())
        return center + radius * cos(radians).toFloat() to center + radius * sin(radians).toFloat()
    }
    val (startX, startY) = point(start)
    val (endX, endY) = point(start + sweep)
    path(
        stroke = SolidColor(stroke),
        strokeLineWidth = 1.8f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ) {
        moveTo(startX, startY)
        arcTo(
            horizontalEllipseRadius = radius,
            verticalEllipseRadius = radius,
            theta = 0f,
            isMoreThanHalf = sweep > 180f,
            isPositiveArc = true,
            x1 = endX,
            y1 = endY,
        )
    }
}

private fun strokeIcon(name: String, block: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ) {
            block()
        }
    }.build()
