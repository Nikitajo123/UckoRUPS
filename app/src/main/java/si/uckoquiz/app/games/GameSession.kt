package si.uckoquiz.app.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * Lives, score, combo and time for one round of a mini game.
 * Every game uses this, so all games count points the same way.
 *
 * Usage inside a game:
 *   val session = rememberGameSession(total = 8)
 *   session.onCorrect()  or  session.onWrong()
 *   if (session.isOver) GameEnd(session.result(), ...)
 */
class GameSession(val total: Int, val maxLives: Int = 3) {
    var lives by mutableIntStateOf(maxLives)
        private set
    var score by mutableIntStateOf(0)
        private set
    var correct by mutableIntStateOf(0)
        private set
    var answered by mutableIntStateOf(0)
        private set
    var combo by mutableIntStateOf(0)
        private set

    private val startedAt = System.currentTimeMillis()

    /** True when the player has no lives left or has answered everything. */
    val isOver: Boolean
        get() = lives <= 0 || answered >= total

    /** Points are multiplied by the combo: x2 from 3 in a row, x3 from 6 in a row. */
    fun onCorrect(points: Int = 10) {
        if (isOver) return
        correct++
        answered++
        combo++
        score += points * comboMultiplier()
    }

    fun onWrong() {
        if (isOver) return
        answered++
        combo = 0
        lives--
    }

    /** Ends the round early, for example when the timer runs out. */
    fun endNow() {
        answered = total
    }

    fun comboMultiplier(): Int = when {
        combo >= 6 -> 3
        combo >= 3 -> 2
        else -> 1
    }

    fun seconds(): Int = ((System.currentTimeMillis() - startedAt) / 1000).toInt()

    fun result(): GameResult = GameResult(correct = correct, total = total, seconds = seconds())
}

@Composable
fun rememberGameSession(total: Int, maxLives: Int = 3): GameSession =
    remember(total, maxLives) { GameSession(total, maxLives) }

/**
 * Counts down from [seconds] once per second while [running] is true.
 * Returns the seconds left and calls [onTimeUp] once when it reaches 0.
 */
@Composable
fun rememberCountdown(seconds: Int, running: Boolean = true, onTimeUp: () -> Unit = {}): Int {
    var left by remember(seconds) { mutableIntStateOf(seconds) }
    val timeUp by rememberUpdatedState(onTimeUp)
    LaunchedEffect(seconds, running) {
        while (running && left > 0) {
            delay(1000)
            left--
        }
        if (running && left == 0) timeUp()
    }
    return left
}
