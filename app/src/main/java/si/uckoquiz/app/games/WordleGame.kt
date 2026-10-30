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
 * Wordle EN (game id "wordle_en", UQ-50).
 * Guess the 5-letter word in 6 tries. Green = right letter, right place;
 * yellow = in the word, wrong place; grey = not in the word.
 *
 * Content (in the lesson JSON):
 *   "answers": the words, one per round, e.g. ["RULER", "PAPER", "CHAIR"] (5 letters each)
 *   "config.hints": optional hints, separated by |, same order, e.g. "ravnilo|papir|stol"
 *   "config.maxTries": optional, default 6
 * Any 5 letters are accepted as a guess (no dictionary), so the game stays simple for kids.
 */
@Composable
fun WordleGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val words = remember(task.id) {
        task.answers.map { it.trim().uppercase() }.filter { it.length == WORD_LENGTH }
            .ifEmpty { listOf("RULER") }
    }
    val hints = remember(task.id) { task.config["hints"]?.split("|")?.map { it.trim() } ?: emptyList() }
    val maxTries = remember(task.id) { task.config["maxTries"]?.toIntOrNull()?.coerceIn(3, 8) ?: 6 }
    val session = rememberGameSession(total = words.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var solved by remember(task.id) { mutableStateOf<Boolean?>(null) } // null = still guessing

    if (session.isOver && solved == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val target = words[round.coerceAtMost(words.lastIndex)]
    val guesses = remember(task.id, round) { mutableStateListOf<String>() }
    var current by remember(task.id, round) { mutableStateOf("") }
    var message by remember(task.id, round) { mutableStateOf<String?>(null) }

    val keyStates = remember(guesses.size, round) {
        val map = mutableMapOf<Char, LetterState>()
        guesses.forEach { g ->
            scoreGuess(g, target).forEachIndexed { i, s ->
                val c = g[i]
                if (s.rank > (map[c]?.rank ?: -1)) map[c] = s
            }
        }
        map.toMap()
    }

    fun submit() {
        if (solved != null) return
        if (current.length < WORD_LENGTH) {
            message = "Beseda mora imeti 5 črk."
            return
        }
        guesses.add(current)
        val guess = current
        current = ""
        message = null
        if (guess == target) {
            session.onCorrect(points = 10 * (maxTries - guesses.size + 1))
            solved = true
        } else if (guesses.size >= maxTries) {
            session.onWrong()
            solved = false
        }
    }

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "Wordle EN", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Guess the 5-letter word." }, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Beseda ${round + 1} / ${words.size}", color = color, fontWeight = FontWeight.Bold)
            hints.getOrNull(round)?.takeIf { it.isNotEmpty() }?.let {
                Text("Namig: $it", color = Color.Gray)
            }
            Spacer(Modifier.height(12.dp))
            for (row in 0 until maxTries) {
                val text = when {
                    row < guesses.size -> guesses[row]
                    row == guesses.size && solved == null -> current
                    else -> ""
                }
                val states = if (row < guesses.size) scoreGuess(guesses[row], target) else null
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(3.dp)) {
                    for (col in 0 until WORD_LENGTH) {
                        LetterTile(text.getOrNull(col), states?.get(col))
                    }
                }
            }
            message?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = GameColors.Wrong, fontWeight = FontWeight.Bold)
            }
        }

        val isSolved = solved
        if (isSolved == null) {
            Keyboard(
                keyStates = keyStates,
                onLetter = { if (current.length < WORD_LENGTH) current += it },
                onDelete = { current = current.dropLast(1) },
                onEnter = { submit() },
            )
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (isSolved) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                val dark = if (isSolved) Color(0xFF3B8A00) else Color(0xFFC62828)
                Text(
                    if (isSolved) "Bravo! ${guesses.size} / $maxTries" else "Beseda je bila: $target",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = dark,
                )
                hints.getOrNull(round)?.takeIf { it.isNotEmpty() }?.let { Text("$target = $it", color = dark) }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        solved = null
                        if (!session.isOver) round++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSolved) GameColors.Correct else GameColors.Wrong,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text("NAPREJ", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

private const val WORD_LENGTH = 5

/** rank is used to keep the best known state for each key on the keyboard. */
internal enum class LetterState(val rank: Int) { Absent(0), Present(1), Correct(2) }

/**
 * Scores a guess like real Wordle, including double letters:
 * first mark exact matches, then mark other letters as present only while unused copies are left.
 */
internal fun scoreGuess(guess: String, target: String): List<LetterState> {
    val result = MutableList(guess.length) { LetterState.Absent }
    val left = mutableMapOf<Char, Int>()
    for (i in guess.indices) {
        if (i < target.length && guess[i] == target[i]) {
            result[i] = LetterState.Correct
        } else if (i < target.length) {
            left[target[i]] = (left[target[i]] ?: 0) + 1
        }
    }
    for (i in guess.indices) {
        if (result[i] == LetterState.Correct) continue
        val n = left[guess[i]] ?: 0
        if (n > 0) {
            result[i] = LetterState.Present
            left[guess[i]] = n - 1
        }
    }
    return result
}

private fun LetterState?.color(): Color = when (this) {
    LetterState.Correct -> GameColors.Correct
    LetterState.Present -> GameColors.Gold
    LetterState.Absent -> Color(0xFF9E9E9E)
    null -> Color.White
}

@Composable
private fun LetterTile(letter: Char?, state: LetterState?) {
    Box(
        Modifier
            .size(52.dp)
            .background(state.color(), RoundedCornerShape(6.dp))
            .border(2.dp, if (state == null) Color(0xFFD0D0D0) else Color.Transparent, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            letter?.toString() ?: "",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (state == null) Color.Black else Color.White,
        )
    }
}

@Composable
private fun Keyboard(
    keyStates: Map<Char, LetterState>,
    onLetter: (Char) -> Unit,
    onDelete: () -> Unit,
    onEnter: () -> Unit,
) {
    val rows = listOf("QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM")
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        rows.forEachIndexed { i, keys ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (i == 2) Key("ENTER", Color(0xFFC4C4C4), Color.Black, wide = true, onClick = onEnter)
                keys.forEach { c ->
                    val s = keyStates[c]
                    Key(
                        label = c.toString(),
                        background = if (s == null) Color(0xFFDADCE0) else s.color(),
                        textColor = if (s == null) Color.Black else Color.White,
                        onClick = { onLetter(c) },
                    )
                }
                if (i == 2) Key("⌫", Color(0xFFC4C4C4), Color.Black, wide = true, onClick = onDelete)
            }
        }
    }
}

@Composable
private fun Key(label: String, background: Color, textColor: Color, wide: Boolean = false, onClick: () -> Unit) {
    Box(
        Modifier
            .size(width = if (wide) 52.dp else 32.dp, height = 48.dp)
            .background(background, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = if (wide) 11.sp else 16.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun WordlePreview() {
    MaterialTheme {
        Surface {
            WordleGame(
                task = Task(
                    id = "ang_school_04_t8",
                    type = "game",
                    game = "wordle_en",
                    prompt = "Guess the 5-letter word.",
                    answers = listOf("RULER", "PAPER", "CHAIR"),
                    config = mapOf("hints" to "ravnilo|papir|stol"),
                ),
                onFinish = {},
            )
        }
    }
}
