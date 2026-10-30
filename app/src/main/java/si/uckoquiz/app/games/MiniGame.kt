package si.uckoquiz.app.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task

/** What every game hands back to the lesson engine. */
data class GameResult(val correct: Int, val total: Int, val seconds: Int)

/**
 * The only entry point for games. Nikita F calls this from the lesson screen for tasks with type "game".
 * It picks the game by task.game and makes sure onFinish is called exactly once.
 *
 * To add a game: write it in its own file (e.g. SentenceBuilderGame.kt) with the signature
 *   @Composable fun SentenceBuilderGame(task: Task, onFinish: (GameResult) -> Unit)
 * and add one line to the `when` below.
 */
@Composable
fun MiniGame(task: Task, onFinish: (GameResult) -> Unit) {
    var finished by remember(task.id) { mutableStateOf(false) }
    val finishOnce: (GameResult) -> Unit = { result ->
        if (!finished) {
            finished = true
            onFinish(result)
        }
    }

    when (task.game) {
        "sentence_builder" -> SentenceBuilderGame(task, finishOnce)   // UQ-49
        // "wordle_en" -> WordleGame(task, finishOnce)                   // UQ-50
        // "plava_potone" -> FloatOrSinkGame(task, finishOnce)           // UQ-51
        // "laboratorij" -> LabGame(task, finishOnce)                    // UQ-52
        // "decimalke_2048" -> Decimal2048Game(task, finishOnce)         // UQ-53
        // "ulomki_kocke" -> FractionCubesGame(task, finishOnce)         // UQ-55
        else -> DemoGame(task, finishOnce)
    }
}

/**
 * Stand-in for games that are not built yet. It uses the whole framework
 * (session, HUD, timer, end screen), so it is also the test that the framework works.
 */
@Composable
fun DemoGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val session = rememberGameSession(total = 5)
    val secondsLeft = rememberCountdown(seconds = 60, running = !session.isOver) { session.endNow() }

    if (session.isOver) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    Column(Modifier.fillMaxSize()) {
        GameHud(
            title = task.game.ifBlank { "Igra" },
            session = session,
            color = color,
            secondsLeft = secondsLeft,
            onClose = { onFinish(session.result()) },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(task.prompt.ifBlank { "Ta igra še nastaja." }, fontSize = 20.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Vprašanje ${session.answered + 1} / ${session.total}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = { session.onCorrect() },
                    colors = ButtonDefaults.buttonColors(containerColor = GameColors.Correct),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) { Text("Pravilno") }
                Button(
                    onClick = { session.onWrong() },
                    colors = ButtonDefaults.buttonColors(containerColor = GameColors.Wrong),
                    modifier = Modifier.weight(1f).height(52.dp),
                ) { Text("Napačno") }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun MiniGamePreview() {
    MaterialTheme {
        Surface(Modifier.fillMaxWidth()) {
            MiniGame(
                task = Task(
                    id = "mat_ulomki_01_t4",
                    type = "game",
                    game = "ulomki_kocke",
                    prompt = "Sestavi ulomek 3/8.",
                ),
                onFinish = {},
            )
        }
    }
}
