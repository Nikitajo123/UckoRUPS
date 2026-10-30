package si.uckoquiz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class UckoIconType { CHECK, STAR, LOCK, GAMEPAD, CASTLE, DROP, HEART, PERSON }


@Composable
fun UckoIcon(
    type: UckoIconType,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    cutout: Color = Color.Transparent,
) {
    Canvas(modifier) {
        val s = min(size.width, size.height) / 24f
        val left = (size.width - 24f * s) / 2f
        val top = (size.height - 24f * s) / 2f
        withTransform({
            translate(left, top)
            scale(s, s, pivot = Offset.Zero)
        }) {
            when (type) {
                UckoIconType.CHECK -> drawPath(
                    Path().apply { moveTo(6f, 12.5f); lineTo(10f, 16.5f); lineTo(18f, 8f) },
                    color,
                    style = Stroke(width = 2.8f, cap = StrokeCap.Round, join = StrokeJoin.Round),
                )
                UckoIconType.STAR -> drawPath(starPath(12f, 12.6f, 9f, 4f), color)
                UckoIconType.LOCK -> {
                    drawArc(color, 180f, 180f, false, topLeft = Offset(8f, 4f), size = Size(8f, 8f), style = Stroke(2.4f))
                    drawLine(color, Offset(8f, 8f), Offset(8f, 11f), strokeWidth = 2.4f)
                    drawLine(color, Offset(16f, 8f), Offset(16f, 11f), strokeWidth = 2.4f)
                    drawRoundRect(color, topLeft = Offset(5.5f, 10.5f), size = Size(13f, 9.5f), cornerRadius = CornerRadius(2f))
                }
                UckoIconType.GAMEPAD -> {
                    drawRoundRect(color, topLeft = Offset(3f, 7f), size = Size(18f, 10.5f), cornerRadius = CornerRadius(5f))
                    drawRect(cutout, topLeft = Offset(6f, 11.4f), size = Size(5f, 1.7f))
                    drawRect(cutout, topLeft = Offset(7.65f, 9.75f), size = Size(1.7f, 5f))
                    drawCircle(cutout, radius = 1.1f, center = Offset(15.5f, 11f))
                    drawCircle(cutout, radius = 1.1f, center = Offset(17.5f, 13.4f))
                }
                UckoIconType.CASTLE -> {
                    drawRect(color, topLeft = Offset(5f, 9.5f), size = Size(14f, 10.5f))
                    drawRect(color, topLeft = Offset(5f, 6f), size = Size(3f, 4f))
                    drawRect(color, topLeft = Offset(10.5f, 6f), size = Size(3f, 4f))
                    drawRect(color, topLeft = Offset(16f, 6f), size = Size(3f, 4f))
                    drawRect(cutout, topLeft = Offset(10.5f, 16.5f), size = Size(3f, 3.5f))
                    drawCircle(cutout, radius = 1.5f, center = Offset(12f, 16.5f))
                }
                UckoIconType.DROP -> drawPath(
                    Path().apply {
                        moveTo(12f, 3f)
                        cubicTo(12f, 3f, 5f, 11f, 5f, 15f)
                        cubicTo(5f, 18.9f, 8.1f, 22f, 12f, 22f)
                        cubicTo(15.9f, 22f, 19f, 18.9f, 19f, 15f)
                        cubicTo(19f, 11f, 12f, 3f, 12f, 3f)
                        close()
                    },
                    color,
                )
                UckoIconType.HEART -> drawPath(
                    Path().apply {
                        moveTo(12f, 20f)
                        cubicTo(5f, 14f, 3f, 11f, 3f, 8.5f)
                        cubicTo(3f, 6f, 5f, 4f, 7.5f, 4f)
                        cubicTo(9.3f, 4f, 11f, 5f, 12f, 6.5f)
                        cubicTo(13f, 5f, 14.7f, 4f, 16.5f, 4f)
                        cubicTo(19f, 4f, 21f, 6f, 21f, 8.5f)
                        cubicTo(21f, 11f, 19f, 14f, 12f, 20f)
                        close()
                    },
                    color,
                )
                UckoIconType.PERSON -> {
                    drawCircle(color, radius = 4f, center = Offset(12f, 9f))
                    drawPath(
                        Path().apply {
                            moveTo(4.5f, 20f)
                            cubicTo(4.5f, 15.5f, 8f, 13.5f, 12f, 13.5f)
                            cubicTo(16f, 13.5f, 19.5f, 15.5f, 19.5f, 20f)
                            close()
                        },
                        color,
                    )
                }
            }
        }
    }
}

private fun starPath(cx: Float, cy: Float, outer: Float, inner: Float): Path = Path().apply {
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outer else inner
        val angle = -Math.PI / 2 + i * Math.PI / 5
        val x = cx + r * cos(angle).toFloat()
        val y = cy + r * sin(angle).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
    }
    close()
}
