package si.uckoquiz.app.games

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import si.uckoquiz.app.domain.DragItem
import si.uckoquiz.app.domain.Task

/**
 * Hitro razvrsti (game id "hitro_razvrsti").
 * One card at a time; the player taps the box it belongs to before the time runs out.
 * A wrong box costs a life. After each answer the game moves on by itself.
 *
 * Content (in the lesson JSON):
 *   "categories": ["Se raztopi", "Se ne raztopi"]   (exactly 2)
 *   "items": [{ "text": "🧂 sol", "category": "Se raztopi" }, ...]
 *   "config.seconds": optional time limit, default 60
 */
@Composable
fun SortRushGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val categories = remember(task.id) { sortRushCategories(task) }
    val cards = remember(task.id) {
        task.items.filter { it.category in categories }.ifEmpty { DEFAULT_SORT_ITEMS }.shuffled()
    }
    val session = rememberGameSession(total = cards.size)
    val seconds = remember(task.id) { task.config["seconds"]?.toIntOrNull()?.coerceIn(10, 300) ?: 60 }
    val secondsLeft = rememberCountdown(seconds = seconds, running = !session.isOver) { session.endNow() }
    var round by remember(task.id) { mutableIntStateOf(0) }
    var lastRight by remember(task.id) { mutableStateOf<Boolean?>(null) }

    // Short flash of the result, then the next card.
    LaunchedEffect(round, lastRight) {
        if (lastRight != null) {
            delay(650)
            lastRight = null
            if (!session.isOver) round++
        }
    }

    if (session.isOver && lastRight == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val card = cards[round.coerceAtMost(cards.lastIndex)]

    fun pick(category: String) {
        if (lastRight != null || session.isOver) return
        val ok = category == card.category
        if (ok) session.onCorrect() else session.onWrong()
        lastRight = ok
    }

    Column(Modifier.fillMaxSize()) {
        GameHud(
            title = "Hitro razvrsti",
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
            Text(task.prompt.ifBlank { "Razvrsti čim hitreje!" }, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Kartica ${round + 1} / ${cards.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            val right = lastRight
            val cardBg = when (right) {
                null -> Color.White
                true -> Color(0xFFD7FFB8)
                false -> Color(0xFFFFDFE0)
            }
            val cardBorder = when (right) {
                null -> color
                true -> GameColors.Correct
                false -> GameColors.Wrong
            }
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(cardBg, RoundedCornerShape(24.dp))
                    .border(3.dp, cardBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(card.text, fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                when (right) {
                    null -> " "
                    true -> "Pravilno!"
                    false -> "Napačno: ${card.category}"
                },
                color = if (right == false) GameColors.Wrong else GameColors.Correct,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            categories.forEachIndexed { i, category ->
                Button(
                    onClick = { pick(category) },
                    enabled = lastRight == null,
                    colors = ButtonDefaults.buttonColors(containerColor = if (i == 0) GameColors.Nar else GameColors.Mat),
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                ) { Text(category, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
            }
        }
    }
}

/** The two boxes: from task.categories, or from the items, or the default ones. */
internal fun sortRushCategories(task: Task): List<String> {
    val fromTask = task.categories.filter { it.isNotBlank() }.distinct()
    if (fromTask.size >= 2) return fromTask.take(2)
    val fromItems = task.items.map { it.category }.distinct()
    return if (fromItems.size >= 2) fromItems.take(2) else DEFAULT_SORT_CATEGORIES
}

private val DEFAULT_SORT_CATEGORIES = listOf("Se raztopi", "Se ne raztopi")

/** Used when the task has no items, so the game always works. */
private val DEFAULT_SORT_ITEMS = listOf(
    DragItem("🧂 sol", "Se raztopi"),
    DragItem("🍬 sladkor", "Se raztopi"),
    DragItem("💊 šumeča tableta", "Se raztopi"),
    DragItem("🏖️ pesek", "Se ne raztopi"),
    DragItem("🫒 olje", "Se ne raztopi"),
    DragItem("🍚 riž", "Se ne raztopi"),
)

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun SortRushPreview() {
    MaterialTheme {
        Surface {
            SortRushGame(task = Task(id = "nar_raziskujemo_igra_t1", type = "game", game = "hitro_razvrsti"), onFinish = {})
        }
    }
}
