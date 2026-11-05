package si.uckoquiz.app.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task
import kotlin.math.cos
import kotlin.math.sin

/**
 * Lovilec kotov (game id "koti_vrste").
 * An angle is drawn on the screen. The player says what kind of angle it is:
 * ostri, pravi, topi or iztegnjeni. After the answer the game shows how many degrees it has.
 * The right answer is computed from the size, so the content only lists the angles.
 *
 * Content (in the lesson JSON):
 *   "config.angles": "35|90|120|180|..."   whole degrees from 1 to 180
 */
@Composable
fun AngleGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val angles = remember(task.id) { parseAngles(task.config).ifEmpty { DEFAULT_ANGLES } }
    val session = rememberGameSession(total = angles.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var picked by remember(task.id) { mutableStateOf<AngleKind?>(null) }

    if (session.isOver && picked == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val degrees = angles[round.coerceAtMost(angles.lastIndex)]
    val kind = angleKind(degrees)
    val answered = picked != null

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Lovilec kotov", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Kakšen je ta kot?" }, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Kot ${round + 1} / ${angles.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            AngleDrawing(degrees = degrees, color = color, modifier = Modifier.fillMaxWidth().height(220.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                if (answered) "$degrees°" else "? °",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (answered) color else Color.Gray,
            )
        }

        val p = picked
        if (p == null) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AngleKind.entries.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { k ->
                            Button(
                                onClick = {
                                    if (k == kind) session.onCorrect() else session.onWrong()
                                    picked = k
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = color),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                            ) { Text(k.label, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                        }
                    }
                }
            }
        } else {
            val right = p == kind
            val dark = if (right) Color(0xFF3B8A00) else Color(0xFFC62828)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (right) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                Text(if (right) "Odlično!" else "Skoraj!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = dark)
                Text("$degrees° je ${kind.label} kot. ${kind.rule}", color = dark)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        picked = null
                        if (!session.isOver) round++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (right) GameColors.Correct else GameColors.Wrong),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text("NAPREJ", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** Draws the angle: a horizontal arm to the right, a second arm turned by [degrees], and an arc. */
@Composable
private fun AngleDrawing(degrees: Int, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color(0xFFFFF6EC), RoundedCornerShape(18.dp))) {
        val arm = size.minDimension * 0.42f
        // The vertex sits low and centred, so even 180° fits on the screen.
        val vertex = Offset(size.width / 2f, size.height * 0.78f)
        val rad = Math.toRadians(degrees.toDouble())
        val end1 = Offset(vertex.x + arm, vertex.y)
        val end2 = Offset(vertex.x + arm * cos(rad).toFloat(), vertex.y - arm * sin(rad).toFloat())
        val r = arm * 0.3f
        if (degrees == 90) {
            // A right angle is marked with a small square, as in the textbook.
            val s = r * 0.7f
            drawLine(color, Offset(vertex.x + s, vertex.y), Offset(vertex.x + s, vertex.y - s), strokeWidth = 5f)
            drawLine(color, Offset(vertex.x, vertex.y - s), Offset(vertex.x + s, vertex.y - s), strokeWidth = 5f)
        } else {
            drawArc(
                color = color,
                startAngle = -degrees.toFloat(),
                sweepAngle = degrees.toFloat(),
                useCenter = false,
                topLeft = Offset(vertex.x - r, vertex.y - r),
                size = Size(r * 2, r * 2),
                style = Stroke(width = 5f),
            )
        }
        drawLine(Color(0xFF1F2933), vertex, end1, strokeWidth = 9f, cap = StrokeCap.Round)
        drawLine(Color(0xFF1F2933), vertex, end2, strokeWidth = 9f, cap = StrokeCap.Round)
        drawCircle(Color(0xFF1F2933), radius = 9f, center = vertex)
    }
}

internal enum class AngleKind(val label: String, val rule: String) {
    OSTRI("ostri", "Ostri kot je manjši od 90°."),
    PRAVI("pravi", "Pravi kot meri natanko 90°."),
    TOPI("topi", "Topi kot je med 90° in 180°."),
    IZTEGNJENI("iztegnjeni", "Iztegnjeni kot meri 180°."),
}

internal fun angleKind(degrees: Int): AngleKind = when {
    degrees < 90 -> AngleKind.OSTRI
    degrees == 90 -> AngleKind.PRAVI
    degrees < 180 -> AngleKind.TOPI
    else -> AngleKind.IZTEGNJENI
}

internal fun parseAngles(config: Map<String, String>): List<Int> =
    config["angles"]?.split("|")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it in 1..180 }.orEmpty()

private val DEFAULT_ANGLES = listOf(40, 90, 135, 180, 75, 100)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun AnglePreview() {
    MaterialTheme {
        Surface {
            AngleGame(task = Task(id = "mat_koti_igra_t1", type = "game", game = "koti_vrste"), onFinish = {})
        }
    }
}
