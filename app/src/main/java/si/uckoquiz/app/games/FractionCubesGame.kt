package si.uckoquiz.app.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task

/**
 * Ulomki s kockami (game id "ulomki_kocke", UQ-55).
 * The player colours cubes to build the asked fraction, e.g. 3 of 8 cubes for 3/8.
 *
 * Content (in the lesson JSON, config):
 *   "fractions": "3/8|2/4|5/6"            several rounds, or
 *   "numerator": "3", "denominator": "8"  one round (as in mat_ulomki_01)
 * Denominators from 2 to 12.
 */
@Composable
fun FractionCubesGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val rounds = remember(task.id) { parseFractions(task.config).ifEmpty { DEFAULT_FRACTIONS } }
    val session = rememberGameSession(total = rounds.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var checked by remember(task.id) { mutableStateOf<Boolean?>(null) }

    if (session.isOver && checked == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val (num, den) = rounds[round.coerceAtMost(rounds.lastIndex)]
    val on = remember(task.id, round) { mutableStateListOf<Boolean>().apply { repeat(den) { add(false) } } }
    val count = on.count { it }
    val perRow = if (den <= 4) den else (den + 1) / 2

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Ulomki s kockami", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Pobarvaj kocke." }.let { if (rounds.size > 1) "Pobarvaj kocke." else it }, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Naloga ${round + 1} / ${rounds.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Pobarvaj $num/$den", fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            val fill = when (checked) {
                true -> GameColors.Correct
                false -> GameColors.Wrong
                null -> color
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (start in 0 until den step perRow) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        for (i in start until minOf(start + perRow, den)) {
                            Box(
                                Modifier
                                    .size(if (perRow > 4) 52.dp else 68.dp)
                                    .background(if (on[i]) fill else Color.White, RoundedCornerShape(12.dp))
                                    .border(2.dp, if (on[i]) fill else Color(0xFFD0D0D0), RoundedCornerShape(12.dp))
                                    .clickable(enabled = checked == null) { on[i] = !on[i] },
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("$count od $den = $count/$den", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = fill)
            Text("Tapni kocko, da jo pobarvaš ali odbarvaš.", color = Color.Gray)
        }

        val isRight = checked
        if (isRight == null) {
            Button(
                onClick = {
                    val ok = count == num
                    if (ok) session.onCorrect() else session.onWrong()
                    checked = ok
                },
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Correct),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
            ) { Text("PREVERI", fontWeight = FontWeight.Bold) }
        } else {
            val dark = if (isRight) Color(0xFF3B8A00) else Color(0xFFC62828)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (isRight) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                Text(if (isRight) "Pravilno!" else "Ne čisto.", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = dark)
                Text(
                    "Imenovalec $den pove, da je celota razdeljena na $den delov. Števec $num pove, koliko delov pobarvamo.",
                    color = dark,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        checked = null
                        if (!session.isOver) round++
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRight) GameColors.Correct else GameColors.Wrong),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text("NAPREJ", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** Reads "fractions" ("3/8|2/4") or a single "numerator"/"denominator". Bad entries are skipped. */
internal fun parseFractions(config: Map<String, String>): List<Pair<Int, Int>> {
    val list = config["fractions"]?.split("|")
        ?: listOfNotNull(
            config["numerator"]?.let { n -> config["denominator"]?.let { d -> "$n/$d" } },
        )
    return list.mapNotNull { f ->
        val parts = f.trim().split("/")
        val n = parts.getOrNull(0)?.trim()?.toIntOrNull()
        val d = parts.getOrNull(1)?.trim()?.toIntOrNull()
        if (parts.size == 2 && n != null && d != null && d in 2..12 && n in 1..d) n to d else null
    }
}

private val DEFAULT_FRACTIONS = listOf(1 to 2, 3 to 8, 2 to 4, 5 to 6, 7 to 10)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun FractionCubesPreview() {
    MaterialTheme {
        Surface {
            FractionCubesGame(
                task = Task(
                    id = "mat_ulomki_01_t4",
                    type = "game",
                    game = "ulomki_kocke",
                    prompt = "Sestavi ulomek 3/8.",
                    config = mapOf("numerator" to "3", "denominator" to "8"),
                ),
                onFinish = {},
            )
        }
    }
}
