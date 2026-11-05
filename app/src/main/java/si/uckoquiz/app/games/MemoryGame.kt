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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import si.uckoquiz.app.domain.MatchPair
import si.uckoquiz.app.domain.Task

/**
 * Spomin (game id "spomin").
 * All cards lie face down. The player turns two at a time and looks for an English word
 * and its Slovenian translation. A found pair stays open. The clock is the opponent:
 * every pair found before the time runs out counts as correct.
 *
 * Content (in the lesson JSON):
 *   "pairs": [{ "left": "apple", "right": "jabolko" }, ...]   (2 to 8 pairs, 6 fits best)
 *   "config.seconds": optional time limit, default 90
 */
@Composable
fun MemoryGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val cards = remember(task.id) { memoryCards(task.pairs.ifEmpty { DEFAULT_MEMORY_PAIRS }.take(8)) }
    val pairCount = cards.size / 2
    val session = rememberGameSession(total = pairCount)
    val seconds = remember(task.id) { task.config["seconds"]?.toIntOrNull()?.coerceIn(20, 300) ?: 90 }
    val secondsLeft = rememberCountdown(seconds = seconds, running = !session.isOver) { session.endNow() }
    val found = remember(task.id) { mutableStateListOf<Int>() }   // pair ids
    val open = remember(task.id) { mutableStateListOf<Int>() }    // card indexes, at most 2
    var misses by remember(task.id) { mutableIntStateOf(0) }

    // Two open cards that are not a pair turn back after a moment.
    LaunchedEffect(open.toList()) {
        if (open.size == 2) {
            delay(900)
            open.clear()
        }
    }

    if (session.isOver) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    fun tap(index: Int) {
        val card = cards[index]
        if (open.size >= 2 || index in open || card.pairId in found) return
        open.add(index)
        if (open.size == 2) {
            val other = cards[open[0]]
            if (other.pairId == card.pairId) {
                found.add(card.pairId)
                open.clear()
                session.onCorrect()
            } else {
                misses++
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        GameHud(
            title = "Spomin",
            session = session,
            color = color,
            secondsLeft = secondsLeft,
            onClose = { onFinish(session.result()) },
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                task.prompt.ifBlank { "Najdi angleško besedo in njen prevod." },
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Pari: ${found.size} / $pairCount", color = color, fontWeight = FontWeight.Bold)
            Text("Zgrešeno: $misses", color = Color.Gray)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            cards.indices.chunked(MEMORY_COLUMNS).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    row.forEach { index ->
                        val card = cards[index]
                        val isFound = card.pairId in found
                        val faceUp = isFound || index in open
                        val wrongPair = open.size == 2 && index in open
                        val bg = when {
                            isFound -> Color(0xFFD7FFB8)
                            wrongPair -> Color(0xFFFFDFE0)
                            faceUp -> Color.White
                            else -> color
                        }
                        val border = when {
                            isFound -> GameColors.Correct
                            wrongPair -> GameColors.Wrong
                            else -> color
                        }
                        Box(
                            Modifier
                                .weight(1f)
                                .height(84.dp)
                                .background(bg, RoundedCornerShape(14.dp))
                                .border(2.dp, border, RoundedCornerShape(14.dp))
                                .clickable(enabled = !faceUp) { tap(index) }
                                .padding(6.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (faceUp) {
                                Text(
                                    card.text,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    fontSize = 15.sp,
                                    color = if (card.english) color else Color(0xFF1F2933),
                                )
                            } else {
                                Text("?", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    // Keep the last row aligned when it is not full.
                    repeat(MEMORY_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

internal data class MemoryCard(val pairId: Int, val text: String, val english: Boolean)

/** Two cards per pair (English and Slovenian), shuffled. */
internal fun memoryCards(pairs: List<MatchPair>, random: kotlin.random.Random = kotlin.random.Random.Default): List<MemoryCard> =
    pairs.filter { it.left.isNotBlank() && it.right.isNotBlank() }
        .flatMapIndexed { i, p -> listOf(MemoryCard(i, p.left, true), MemoryCard(i, p.right, false)) }
        .shuffled(random)

private const val MEMORY_COLUMNS = 3

/** Used when the task has no pairs, so the game always works. */
private val DEFAULT_MEMORY_PAIRS = listOf(
    MatchPair("apple", "jabolko"),
    MatchPair("bread", "kruh"),
    MatchPair("milk", "mleko"),
    MatchPair("cheese", "sir"),
    MatchPair("water", "voda"),
    MatchPair("egg", "jajce"),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun MemoryPreview() {
    MaterialTheme {
        Surface {
            MemoryGame(task = Task(id = "ang_home_igra_t1", type = "game", game = "spomin"), onFinish = {})
        }
    }
}
