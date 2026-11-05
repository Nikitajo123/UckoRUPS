package si.uckoquiz.app.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import kotlinx.coroutines.delay
import si.uckoquiz.app.domain.Task

/**
 * Boss fight (game id "boss_fight"): the game behind the castle at the end of a subject.
 *
 * The boss has a health bar, the player has 3 lives. Every question has a time limit.
 * A right answer hits the boss; a wrong answer or running out of time costs a life.
 * The boss is beaten once 60 % of the questions are right (same rule as passing a lesson);
 * the player keeps going through all questions for more stars (and gold, if all are right).
 *
 * Content (in the lesson JSON, all in "config", one entry per question, separated by |):
 *   "questions": "Question 1|Question 2"
 *   "options":   "a;b;c|a;b;c;d"     (options of one question separated by ;)
 *   "correct":   "0|2"               (index of the right option, 0 = first)
 *   "why":       optional short explanations, same order
 *   "seconds":   optional time per question, default 20
 *   "bossName":  optional, e.g. "Profesor Zmešnjava"
 *   "bossIcon":  optional emoji, e.g. "🧪"
 */
@Composable
fun BossFightGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val rounds = remember(task.id) { parseBossRounds(task.config).ifEmpty { DEFAULT_BOSS_ROUNDS } }
    // Options are shuffled once, so the right answer is not always in the same place.
    val shuffled = remember(task.id) { rounds.map { it.shuffled() } }
    val bossName = task.config["bossName"]?.ifBlank { null } ?: "Boss"
    val bossIcon = task.config["bossIcon"]?.ifBlank { null } ?: "👾"
    val secondsPerQuestion = remember(task.id) { task.config["seconds"]?.toIntOrNull()?.coerceIn(5, 120) ?: 20 }

    val session = rememberGameSession(total = rounds.size, maxLives = 3)
    val hitsToWin = bossHitsToWin(rounds.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var picked by remember(task.id) { mutableStateOf<Int?>(null) }      // chosen option, -1 = time ran out
    var secondsLeft by remember(task.id) { mutableIntStateOf(secondsPerQuestion) }

    val current = shuffled[round.coerceAtMost(shuffled.lastIndex)]
    val answered = picked != null

    fun answer(index: Int) {
        if (answered || session.isOver) return
        if (index == current.correct) session.onCorrect() else session.onWrong()
        picked = index
    }

    // Time limit for each question; it stops while the feedback is shown.
    LaunchedEffect(round, answered) {
        if (answered) return@LaunchedEffect
        secondsLeft = secondsPerQuestion
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        answer(-1)
    }

    if (session.isOver && !answered) {
        val result = remember { session.result() }
        BossEnd(
            won = session.lives > 0 && result.correct >= hitsToWin,
            bossName = bossName,
            bossIcon = bossIcon,
            result = result,
            onContinue = { onFinish(result) },
        )
        return
    }

    val bossHp = (hitsToWin - session.correct).coerceAtLeast(0)
    val bossDown = bossHp == 0

    Column(Modifier.fillMaxSize()) {
        GameHud(
            title = "Boss level",
            session = session,
            color = color,
            secondsLeft = if (answered) null else secondsLeft,
            onClose = { onFinish(session.result()) },
        )

        // The boss and its health bar.
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B2A4A))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val mood = when {
                bossDown -> "😵"
                picked == current.correct -> "💥"
                answered -> "😈"
                else -> bossIcon
            }
            Text(mood, fontSize = 56.sp)
            Text(bossName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(Color(0xFF3A4A6B), RoundedCornerShape(8.dp)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(bossHp / hitsToWin.toFloat())
                        .background(GameColors.Wrong, RoundedCornerShape(8.dp)),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (bossDown) "Boss je premagan! Dokončaj za vse zvezdice." else "Še $bossHp zadetkov do zmage",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
            )
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Vprašanje ${round + 1} / ${rounds.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(current.question, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            current.options.forEachIndexed { i, option ->
                val bg = when {
                    !answered -> Color.White
                    i == current.correct -> Color(0xFFD7FFB8)
                    i == picked -> Color(0xFFFFDFE0)
                    else -> Color.White
                }
                val border = when {
                    !answered -> Color(0xFFE0E0E0)
                    i == current.correct -> GameColors.Correct
                    i == picked -> GameColors.Wrong
                    else -> Color(0xFFE0E0E0)
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .background(bg, RoundedCornerShape(14.dp))
                        .border(2.dp, border, RoundedCornerShape(14.dp))
                        .clickable(enabled = !answered) { answer(i) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Text(option, color = Color(0xFF1F2933), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        val p = picked
        if (p != null) {
            val right = p == current.correct
            val dark = if (right) Color(0xFF3B8A00) else Color(0xFFC62828)
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (right) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                Text(
                    when {
                        right -> "Zadetek!"
                        p == -1 -> "Zmanjkalo je časa!"
                        else -> "Skoraj! Boss te je zadel."
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = dark,
                )
                if (!right) Text("Pravilno: ${current.options[current.correct]}", color = dark, fontWeight = FontWeight.Bold)
                if (current.why.isNotEmpty()) Text(current.why, color = dark)
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

@Composable
private fun BossEnd(won: Boolean, bossName: String, bossIcon: String, result: GameResult, onContinue: () -> Unit) {
    val stars = starsFor(result)
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(if (won) "🏆" else bossIcon, fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            if (won) "$bossName je premagan!" else "$bossName je tokrat zmagal.",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        if (!won) Text("Nič hudega, poskusi znova. Zmoreš!", textAlign = TextAlign.Center)
        Spacer(Modifier.height(16.dp))
        Text("${result.correct} / ${result.total} pravilnih", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = GameColors.Correct)
        Text("Čas ${formatTime(result.seconds)}", color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text("★".repeat(stars) + "☆".repeat(3 - stars), fontSize = 40.sp, color = GameColors.Gold)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GameColors.Correct),
        ) { Text("NADALJUJ", fontWeight = FontWeight.Bold) }
    }
}

internal data class BossRound(val question: String, val options: List<String>, val correct: Int, val why: String = "") {
    /** Same question with the options in random order; [correct] follows the right option. */
    fun shuffled(random: kotlin.random.Random = kotlin.random.Random.Default): BossRound {
        val order = options.indices.shuffled(random)
        return copy(options = order.map { options[it] }, correct = order.indexOf(correct))
    }
}

/** Right answers needed to beat the boss: 60 %, the same as passing a lesson. */
internal fun bossHitsToWin(total: Int): Int = ((total * 3 + 4) / 5).coerceAtLeast(1)

/** Reads the questions from task.config. Broken questions (no options, bad index) are skipped. */
internal fun parseBossRounds(config: Map<String, String>): List<BossRound> {
    val questions = config["questions"]?.split("|")?.map { it.trim() } ?: return emptyList()
    val options = config["options"]?.split("|") ?: return emptyList()
    val correct = config["correct"]?.split("|")?.map { it.trim().toIntOrNull() } ?: return emptyList()
    val why = config["why"]?.split("|")?.map { it.trim() } ?: emptyList()
    return questions.mapIndexedNotNull { i, q ->
        val opts = options.getOrNull(i)?.split(";")?.map { it.trim() }?.filter { it.isNotEmpty() }.orEmpty()
        val c = correct.getOrNull(i)
        if (q.isEmpty() || opts.size < 2 || c == null || c !in opts.indices) null
        else BossRound(q, opts, c, why.getOrNull(i).orEmpty())
    }
}

/** Used when the task has no questions, so the game always works. */
private val DEFAULT_BOSS_ROUNDS = listOf(
    BossRound("Katera snov je v vodi topna?", listOf("sol", "pesek", "olje"), 0),
    BossRound("Olje vliješ v vodo. Kje bo olje?", listOf("na vrhu", "na dnu", "raztopljeno"), 0),
    BossRound("Led se stali. Kako se imenuje ta sprememba?", listOf("taljenje", "strjevanje", "kondenzacija"), 0),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun BossFightPreview() {
    MaterialTheme {
        Surface {
            BossFightGame(task = Task(id = "nar_raziskujemo_boss_t1", type = "game", game = "boss_fight"), onFinish = {})
        }
    }
}
