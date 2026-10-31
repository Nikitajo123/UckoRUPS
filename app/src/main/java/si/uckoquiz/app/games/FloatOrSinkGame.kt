package si.uckoquiz.app.games

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.DragItem
import si.uckoquiz.app.domain.Task

/**
 * Plava ali potone (game id "plava_potone", UQ-51).
 * An object is shown, the player predicts whether it floats or sinks,
 * then the object drops into the water tank and floats up or sinks to the bottom.
 *
 * Content (in the lesson JSON):
 *   "items": [{ "text": "🔩 žebelj", "category": "potone" }, { "text": "🍎 jabolko", "category": "plava" }, ...]
 *   "config.why": optional reasons, separated by |, same order as the items.
 *                 Without it the game says "manjšo/večjo gostoto kot voda".
 */
@Composable
fun FloatOrSinkGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val objects = remember(task.id) {
        task.items.filter { it.category.trim().lowercase() in setOf(FLOATS, SINKS) }
            .ifEmpty { DEFAULT_OBJECTS }
            .shuffled()
    }
    val reasons = remember(task.id) {
        // Reasons follow the order in the JSON, so look them up by the object text, not by round.
        val list = task.config["why"]?.split("|")?.map { it.trim() } ?: emptyList()
        task.items.mapIndexedNotNull { i, item -> list.getOrNull(i)?.let { item.text to it } }.toMap()
    }
    val session = rememberGameSession(total = objects.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var guessedRight by remember(task.id) { mutableStateOf<Boolean?>(null) } // null = still predicting

    if (session.isOver && guessedRight == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val item = objects[round.coerceAtMost(objects.lastIndex)]
    val floats = item.category.trim().lowercase() == FLOATS
    val dropped = guessedRight != null

    // 0 = water surface. Floating objects bob just below it, sinking ones fall to the bottom.
    val objectY by animateDpAsState(
        targetValue = when {
            !dropped -> (-70).dp
            floats -> 10.dp
            else -> 190.dp
        },
        animationSpec = tween(durationMillis = if (dropped) 1200 else 0),
        label = "objectY",
    )

    fun predict(saysFloats: Boolean) {
        if (dropped) return
        val ok = saysFloats == floats
        if (ok) session.onCorrect() else session.onWrong()
        guessedRight = ok
    }

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Plava ali potone", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Ali predmet plava ali potone?" }, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Predmet ${round + 1} od ${objects.size}",
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.End),
            )
            Spacer(Modifier.height(8.dp))
            Text(item.text, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { predict(saysFloats = true) },
                    enabled = !dropped,
                    colors = ButtonDefaults.buttonColors(containerColor = GameColors.Ang),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) { Text("PLAVA", fontWeight = FontWeight.Bold) }
                Button(
                    onClick = { predict(saysFloats = false) },
                    enabled = !dropped,
                    colors = ButtonDefaults.buttonColors(containerColor = GameColors.Nar),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) { Text("POTONE", fontWeight = FontWeight.Bold) }
            }

            Spacer(Modifier.height(90.dp))

            // Water tank. The object is drawn on top and moves with objectY.
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color(0xFFDDEFFF), RoundedCornerShape(18.dp))
                    .border(3.dp, Color(0xFF9CC9F0), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.TopCenter,
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 30.dp)
                        .height(227.dp)
                        .background(Color(0xFF8EC5F5), RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)),
                )
                Text(
                    item.text.substringBefore(' ').takeIf { it != item.text } ?: "⬤",
                    fontSize = 44.sp,
                    modifier = Modifier.offset(y = objectY + 20.dp),
                )
            }
        }

        val right = guessedRight
        if (right != null) {
            val dark = if (right) Color(0xFF3B8A00) else Color(0xFFC62828)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (right) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                Text(
                    (if (right) "Pravilno! " else "Ne čisto. ") + if (floats) "Plava." else "Potone.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = dark,
                )
                Text(reasons[item.text] ?: defaultReason(floats), color = dark)
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        guessedRight = null
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

internal const val FLOATS = "plava"
internal const val SINKS = "potone"

internal fun defaultReason(floats: Boolean) =
    if (floats) "Ima manjšo gostoto kot voda, zato plava." else "Ima večjo gostoto kot voda, zato potone."

/** Used when the task has no items, so the game always works. */
internal val DEFAULT_OBJECTS = listOf(
    DragItem("🔩 žebelj", SINKS),
    DragItem("🍎 jabolko", FLOATS),
    DragItem("🪙 kovanec", SINKS),
    DragItem("🪵 lesena kocka", FLOATS),
    DragItem("🪨 kamen", SINKS),
    DragItem("🧊 kocka ledu", FLOATS),
    DragItem("🔑 ključ", SINKS),
    DragItem("🕯️ sveča", FLOATS),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun FloatOrSinkPreview() {
    MaterialTheme {
        Surface {
            FloatOrSinkGame(
                task = Task(id = "nar_snovi_03_t8", type = "game", game = "plava_potone"),
                onFinish = {},
            )
        }
    }
}
