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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task

/**
 * Deljivostni stroj (game id "deljivost_stroj").
 * A number comes out of the machine. The player taps every number it is divisible by
 * (2, 3, 5, 9, 10) and checks. If it is divisible by none of them, check with nothing selected.
 * The right answer is computed by the game, so the content only lists the numbers.
 *
 * Content (in the lesson JSON):
 *   "config.numbers": "120|135|..."   natural numbers from 1 to 1 000 000
 */
@Composable
fun DivisibilityGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val numbers = remember(task.id) { parseDivisibilityNumbers(task.config).ifEmpty { DEFAULT_NUMBERS } }
    val session = rememberGameSession(total = numbers.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var checked by remember(task.id) { mutableStateOf<Boolean?>(null) }

    if (session.isOver && checked == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val number = numbers[round.coerceAtMost(numbers.lastIndex)]
    val right = divisorsOf(number)
    val chosen = remember(task.id, round) { mutableStateListOf<Int>() }

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Deljivostni stroj", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                task.prompt.ifBlank { "S katerimi števili je deljivo? Izberi vse." },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Število ${round + 1} / ${numbers.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Text("⚙️", fontSize = 44.sp)
            Box(
                Modifier
                    .background(color, RoundedCornerShape(18.dp))
                    .padding(horizontal = 28.dp, vertical = 14.dp),
            ) {
                Text(formatNumber(number), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
            Text("Deljivo z:", color = Color.Gray, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                DIVISORS.forEach { d ->
                    val isChosen = d in chosen
                    val bg = when {
                        checked == null -> if (isChosen) color else Color.White
                        d in right -> Color(0xFFD7FFB8)
                        isChosen -> Color(0xFFFFDFE0)
                        else -> Color.White
                    }
                    val border = when {
                        checked == null -> color
                        d in right -> GameColors.Correct
                        isChosen -> GameColors.Wrong
                        else -> Color.LightGray
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(64.dp)
                            .background(bg, RoundedCornerShape(14.dp))
                            .border(2.dp, border, RoundedCornerShape(14.dp))
                            .clickable(enabled = checked == null) {
                                if (isChosen) chosen.remove(d) else chosen.add(d)
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "$d",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (checked == null && isChosen) Color.White else Color(0xFF1F2933),
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Če ni deljivo z nobenim, pritisni PREVERI brez izbire.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
            )
        }

        val isRight = checked
        if (isRight == null) {
            Button(
                onClick = {
                    val ok = chosen.toSet() == right
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
                Text(if (isRight) "Odlično!" else "Skoraj!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = dark)
                Text(divisibilityWhy(number), color = dark)
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

internal val DIVISORS = listOf(2, 3, 5, 9, 10)

/** Which of 2, 3, 5, 9, 10 divide [n]. */
internal fun divisorsOf(n: Int): Set<Int> = DIVISORS.filter { n % it == 0 }.toSet()

internal fun digitSum(n: Int): Int = n.toString().sumOf { it.digitToInt() }

/** Short explanation with the rules: last digit for 2, 5, 10 and the digit sum for 3 and 9. */
internal fun divisibilityWhy(n: Int): String {
    val last = n % 10
    val sum = digitSum(n)
    val list = divisorsOf(n).sorted()
    val result = if (list.isEmpty()) "Ni deljivo z nobenim od njih." else "Deljivo z: ${list.joinToString(", ")}."
    return "Zadnja števka je $last, vsota števk je $sum. $result"
}

/** 1 000 000 is written with spaces, as in Slovenian school: 12 345. */
internal fun formatNumber(n: Int): String = n.toString().reversed().chunked(3).joinToString(" ").reversed()

internal fun parseDivisibilityNumbers(config: Map<String, String>): List<Int> =
    config["numbers"]?.split("|")?.mapNotNull { it.trim().toIntOrNull() }?.filter { it in 1..1_000_000 }.orEmpty()

private val DEFAULT_NUMBERS = listOf(120, 135, 74, 99, 250, 1_011, 457, 3_600)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun DivisibilityPreview() {
    MaterialTheme {
        Surface {
            DivisibilityGame(task = Task(id = "mat_deljivost_igra_t1", type = "game", game = "deljivost_stroj"), onFinish = {})
        }
    }
}
