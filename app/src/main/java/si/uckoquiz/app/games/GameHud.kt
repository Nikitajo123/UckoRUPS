package si.uckoquiz.app.games

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Colours used by the games (same as the Figma wireframes). */
object GameColors {
    val Mat = Color(0xFFF7A04F)
    val Ang = Color(0xFF4F8EF7)
    val Nar = Color(0xFF4FBF7A)
    val Correct = Color(0xFF58CC02)
    val Wrong = Color(0xFFFF4B4B)
    val Gold = Color(0xFFFFC800)
    val Locked = Color(0xFFC4C4C4)
    val Surface = Color(0xFFF2F2F2)

    /** Picks the subject colour from a task id like "mat_ulomki_01_t4". */
    fun forTaskId(taskId: String): Color = when (taskId.substringBefore('_')) {
        "ang" -> Ang
        "nar" -> Nar
        else -> Mat
    }
}

/**
 * Top bar shared by every game: close button, game name, hearts, score and an optional timer.
 * Pass secondsLeft = null for games without a timer.
 */
@Composable
fun GameHud(
    title: String,
    session: GameSession,
    color: Color,
    secondsLeft: Int? = null,
    onClose: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onClose != null) {
                TextButton(onClick = onClose) {
                    Text("✕", color = Color.White, fontSize = 20.sp)
                }
            } else {
                Spacer(Modifier.width(12.dp))
            }
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "♥".repeat(session.lives) + "♡".repeat((session.maxLives - session.lives).coerceAtLeast(0)),
                color = Color.White,
                fontSize = 18.sp,
            )
            Spacer(Modifier.width(12.dp))
            val combo = if (session.comboMultiplier() > 1) "  x${session.comboMultiplier()}" else ""
            Text("${session.score}$combo", color = Color.White, fontWeight = FontWeight.Bold)
            if (secondsLeft != null) {
                Spacer(Modifier.width(12.dp))
                Text(formatTime(secondsLeft), color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }
    }
}

fun formatTime(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)
