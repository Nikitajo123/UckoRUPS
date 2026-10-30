package si.uckoquiz.app.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
 * Laboratorij (game id "laboratorij", UQ-52).
 * A mixture is shown. The player picks the separation methods in the right order
 * (e.g. sol + pesek: raztapljanje -> filtriranje -> izhlapevanje) and checks.
 *
 * Content (in the lesson JSON, all in "config", separated by |, one entry per round):
 *   "mixtures": "Pesek + voda|Sol + voda"
 *   "steps":    "filtriranje|izhlapevanje"      (several steps in one round: separated by commas, in order)
 *   "why":      optional explanations, same order
 *   "tools":    optional shelf, separated by commas; default = all methods below
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LabGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val rounds = remember(task.id) { parseLabRounds(task.config).ifEmpty { DEFAULT_ROUNDS } }
    val tools = remember(task.id) {
        task.config["tools"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            ?: (ALL_METHODS + rounds.flatMap { it.steps }).distinct()
    }
    val session = rememberGameSession(total = rounds.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var checked by remember(task.id) { mutableStateOf<Boolean?>(null) }

    if (session.isOver && checked == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val current = rounds[round.coerceAtMost(rounds.lastIndex)]
    val chosen = remember(task.id, round) { mutableStateListOf<String>() }
    val slotCount = current.steps.size

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Laboratorij", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Izberi korake za ločevanje zmesi." }, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Zmes ${round + 1} / ${rounds.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("⚗️", fontSize = 56.sp)
            Text(current.mixture, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Text(
                if (slotCount == 1) "Potreben je 1 korak." else "Potrebni so $slotCount koraki, po vrsti.",
                color = Color.Gray,
            )
            Spacer(Modifier.height(16.dp))

            // Step slots: tap a filled slot to empty it (and every step after it).
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                for (i in 0 until slotCount) {
                    val step = chosen.getOrNull(i)
                    val ok = checked != null && step == current.steps[i]
                    val borderColor = when {
                        checked == null -> color
                        ok -> GameColors.Correct
                        else -> GameColors.Wrong
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(88.dp)
                            .background(
                                if (checked == null) Color(0xFFF2FBF5) else if (ok) Color(0xFFD7FFB8) else Color(0xFFFFDFE0),
                                RoundedCornerShape(16.dp),
                            )
                            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
                            .clickable(enabled = checked == null && step != null) {
                                while (chosen.size > i) chosen.removeAt(chosen.lastIndex)
                            }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("${i + 1}", color = borderColor, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopStart))
                        Text(step?.let { labelFor(it) } ?: "", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Metode:", color = Color.Gray, modifier = Modifier.align(Alignment.Start))
            FlowRow(Modifier.fillMaxWidth()) {
                tools.filter { it !in chosen }.forEach { tool ->
                    Box(
                        Modifier
                            .padding(4.dp)
                            .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp))
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .clickable(enabled = checked == null && chosen.size < slotCount) { chosen.add(tool) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) { Text(labelFor(tool), fontWeight = FontWeight.Bold) }
                }
            }
        }

        val isRight = checked
        if (isRight == null) {
            Button(
                onClick = {
                    val ok = chosen.toList() == current.steps
                    if (ok) session.onCorrect() else session.onWrong()
                    checked = ok
                },
                enabled = chosen.size == slotCount,
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
                Text("Pravilno: " + current.steps.joinToString(" → "), color = dark, fontWeight = FontWeight.Bold)
                if (current.why.isNotEmpty()) Text(current.why, color = dark)
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

internal data class LabRound(val mixture: String, val steps: List<String>, val why: String = "")

/** Reads the rounds from task.config. Rounds without a mixture or steps are skipped. */
internal fun parseLabRounds(config: Map<String, String>): List<LabRound> {
    val mixtures = config["mixtures"]?.split("|")?.map { it.trim() } ?: return emptyList()
    val steps = config["steps"]?.split("|") ?: return emptyList()
    val why = config["why"]?.split("|")?.map { it.trim() } ?: emptyList()
    return mixtures.mapIndexedNotNull { i, m ->
        val s = steps.getOrNull(i)?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
        if (m.isEmpty() || s.isEmpty()) null else LabRound(m, s, why.getOrNull(i).orEmpty())
    }
}

private val ICONS = mapOf(
    "sejanje" to "🕸️",
    "filtriranje" to "☕",
    "izhlapevanje" to "🔥",
    "magnet" to "🧲",
    "raztapljanje v vodi" to "💧",
    "ločilni lij" to "🧪",
)

private fun labelFor(method: String): String = ICONS[method]?.let { "$it $method" } ?: method

private val ALL_METHODS = listOf("sejanje", "filtriranje", "izhlapevanje", "magnet", "raztapljanje v vodi", "ločilni lij")

/** Used when the task has no config, so the game always works. */
private val DEFAULT_ROUNDS = listOf(
    LabRound("Pesek + voda", listOf("filtriranje"), "Pesek ostane na filtrirnem papirju, voda gre skozi."),
    LabRound("Sol + voda", listOf("izhlapevanje"), "Voda izhlapi, sol ostane v posodi."),
    LabRound("Železni opilki + pesek", listOf("magnet"), "Magnet privlači železo, pesek ne."),
    LabRound("Kamenčki + pesek", listOf("sejanje"), "Drobna zrna gredo skozi sito, kamenčki ostanejo."),
    LabRound("Olje + voda", listOf("ločilni lij"), "Olje plava na vodi; vodo spustimo skozi pipico lija."),
    LabRound(
        "Sol + pesek",
        listOf("raztapljanje v vodi", "filtriranje", "izhlapevanje"),
        "Sol se raztopi, pesek odfiltriramo, nato vodo izhlapimo in dobimo sol.",
    ),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun LabPreview() {
    MaterialTheme {
        Surface {
            LabGame(task = Task(id = "nar_zmesi_03_t8", type = "game", game = "laboratorij"), onFinish = {})
        }
    }
}
