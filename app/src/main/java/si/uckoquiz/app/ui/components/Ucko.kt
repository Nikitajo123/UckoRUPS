package si.uckoquiz.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kotlin.math.min

enum class UckoMood { HAPPY, SAD }

private val Fur = Color(0xFFD29B63)
private val EarInside = Color(0xFFB97A43)
private val Muzzle = Color(0xFFF3D9B5)
private val Dark = Color(0xFF1F1F1F)
private val Nose = Color(0xFF5A3A22)

// simona koga ke gi dodade slikite(UQ-48) molze da zamenime so Image(painterResource(R.drawable.ucko_happy)).

@Composable
fun UckoMascot(modifier: Modifier = Modifier, mood: UckoMood = UckoMood.HAPPY) {
    Canvas(
        modifier
            .aspectRatio(1f)
            .semantics { contentDescription = "Učko" }
    ) {
        val s = min(size.width, size.height) / 100f
        withTransform({ scale(s, s, pivot = Offset.Zero) }) {
            // čopki na ušesih
            drawLine(Dark, Offset(27f, 16f), Offset(25f, 4f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            drawLine(Dark, Offset(73f, 16f), Offset(75f, 4f), strokeWidth = 2.2f, cap = StrokeCap.Round)
            // tačke ob strani
            drawPath(Path().apply { moveTo(16f, 60f); lineTo(4f, 72f); lineTo(20f, 70f); close() }, Fur)
            drawPath(Path().apply { moveTo(84f, 60f); lineTo(96f, 72f); lineTo(80f, 70f); close() }, Fur)
            // ušesa
            drawPath(Path().apply { moveTo(18f, 38f); lineTo(27f, 14f); lineTo(42f, 28f); close() }, EarInside)
            drawPath(Path().apply { moveTo(82f, 38f); lineTo(73f, 14f); lineTo(58f, 28f); close() }, EarInside)
            // glava in gobček
            drawCircle(Fur, radius = 34f, center = Offset(50f, 56f))
            drawOval(Muzzle, topLeft = Offset(33f, 54f), size = Size(34f, 28f))
            // oči in nos
            drawCircle(Dark, radius = 3.6f, center = Offset(38f, 54f))
            drawCircle(Dark, radius = 3.6f, center = Offset(62f, 54f))
            drawOval(Nose, topLeft = Offset(46.4f, 59.4f), size = Size(7.2f, 5.2f))
            // usta
            val mouth = Path().apply {
                if (mood == UckoMood.HAPPY) {
                    moveTo(43f, 68f); cubicTo(47.7f, 72.7f, 52.3f, 72.7f, 57f, 68f)
                } else {
                    moveTo(43f, 72f); cubicTo(47.7f, 67.3f, 52.3f, 67.3f, 57f, 72f)
                }
            }
            drawPath(mouth, Nose, style = Stroke(width = 2f, cap = StrokeCap.Round))
        }
    }
}
