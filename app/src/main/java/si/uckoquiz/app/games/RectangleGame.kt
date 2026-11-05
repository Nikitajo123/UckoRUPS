package si.uckoquiz.app.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task

/**
 * Gradbenik (game id "obseg_ploscina").
 * A rectangle is drawn on a grid of 1 cm squares. The game asks for the obseg (odd rounds)
 * or the ploščina (even rounds). The four options are the right answer and typical mistakes
 * (the other quantity, a + b, 2 · a + b ...), all computed by the game.
 *
 * Content (in the lesson JSON):
 *   "config.rects": "5x3|4x4|7x2|..."   sides in cm, each from 1 to 10
 */
@Composable
fun RectangleGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val rects = remember(task.id) { parseRects(task.config).ifEmpty { DEFAULT_RECTS } }
    val questions = remember(task.id) { rects.mapIndexed { i, r -> rectQuestion(r.first, r.second, askArea = i % 2 == 1) } }
    val session = rememberGameSession(total = questions.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var picked by remember(task.id) { mutableStateOf<Int?>(null) }

    if (session.isOver && picked == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val q = questions[round.coerceAtMost(questions.lastIndex)]
    val unit = if (q.askArea) "cm²" else "cm"

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Gradbenik", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                if (q.askArea) "Koliko meri PLOŠČINA?" else "Koliko meri OBSEG?",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = color,
            )
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Lik ${round + 1} / ${questions.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("a = ${q.a} cm, b = ${q.b} cm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text("Vsak kvadratek meri 1 cm².", color = Color.Gray, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            GridRectangle(q.a, q.b, color, highlightBorder = !q.askArea)
        }

        val p = picked
        if (p == null) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                q.options.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { option ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .background(Color.White, RoundedCornerShape(14.dp))
                                    .border(2.dp, color, RoundedCornerShape(14.dp))
                                    .clickable {
                                        if (option == q.answer) session.onCorrect() else session.onWrong()
                                        picked = option
                                    },
                                contentAlignment = Alignment.Center,
                            ) { Text("$option $unit", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1F2933)) }
                        }
                    }
                }
            }
        } else {
            val right = p == q.answer
            val dark = if (right) Color(0xFF3B8A00) else Color(0xFFC62828)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (right) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                Text(if (right) "Odlično!" else "Skoraj!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = dark)
                Text(q.why, color = dark)
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

/** The rectangle on a cm grid. For obseg the border is drawn thicker, for ploščina the squares are filled. */
@Composable
private fun GridRectangle(a: Int, b: Int, color: Color, highlightBorder: Boolean) {
    Canvas(
        Modifier
            .widthIn(max = 320.dp)
            .fillMaxWidth()
            .aspectRatio(a.toFloat() / b.toFloat()),
    ) {
        val cell = size.width / a
        if (!highlightBorder) {
            drawRect(color.copy(alpha = 0.25f), size = Size(cell * a, cell * b))
        }
        for (i in 0..a) drawLine(Color(0xFFBDBDBD), Offset(i * cell, 0f), Offset(i * cell, cell * b), strokeWidth = 2f)
        for (j in 0..b) drawLine(Color(0xFFBDBDBD), Offset(0f, j * cell), Offset(cell * a, j * cell), strokeWidth = 2f)
        drawRect(
            color = color,
            size = Size(cell * a, cell * b),
            style = Stroke(width = if (highlightBorder) 10f else 5f),
        )
    }
}

internal data class RectQuestion(
    val a: Int,
    val b: Int,
    val askArea: Boolean,
    val answer: Int,
    val options: List<Int>,
    val why: String,
)

internal fun rectPerimeter(a: Int, b: Int) = 2 * a + 2 * b
internal fun rectArea(a: Int, b: Int) = a * b

/**
 * One round: the right answer plus three typical mistakes, all different, in random order.
 * Mistakes: the other quantity (obseg instead of ploščina and back), a + b, and 2 · a + b.
 */
internal fun rectQuestion(a: Int, b: Int, askArea: Boolean, random: kotlin.random.Random = kotlin.random.Random.Default): RectQuestion {
    val perimeter = rectPerimeter(a, b)
    val area = rectArea(a, b)
    val answer = if (askArea) area else perimeter
    val mistakes = listOf(if (askArea) perimeter else area, a + b, 2 * a + b, 2 * (a + b) + 2, answer + a, answer + 1)
    val options = (listOf(answer) + mistakes.filter { it != answer && it > 0 }.distinct()).distinct().take(4).shuffled(random)
    val why = if (askArea) {
        "p = a · b = $a · $b = $area cm². Obseg bi bil $perimeter cm."
    } else {
        "o = 2 · a + 2 · b = 2 · $a + 2 · $b = $perimeter cm. Ploščina bi bila $area cm²."
    }
    return RectQuestion(a, b, askArea, answer, options, why)
}

/** Reads "5x3|4x4". Sides must be 1 to 10 cm, so the grid fits on the screen. */
internal fun parseRects(config: Map<String, String>): List<Pair<Int, Int>> =
    config["rects"]?.split("|")?.mapNotNull { part ->
        val sides = part.lowercase().split("x", "×").map { it.trim().toIntOrNull() }
        val a = sides.getOrNull(0)
        val b = sides.getOrNull(1)
        if (sides.size == 2 && a != null && b != null && a in 1..10 && b in 1..10) a to b else null
    }.orEmpty()

private val DEFAULT_RECTS = listOf(5 to 3, 4 to 4, 6 to 2, 7 to 3, 8 to 4, 6 to 5)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun RectanglePreview() {
    MaterialTheme {
        Surface {
            RectangleGame(task = Task(id = "mat_obseg_igra_t1", type = "game", game = "obseg_ploscina"), onFinish = {})
        }
    }
}
