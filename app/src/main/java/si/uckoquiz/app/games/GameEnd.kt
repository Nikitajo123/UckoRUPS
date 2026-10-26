package si.uckoquiz.app.games

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 1 to 3 stars from the share of correct answers (same rule for every game). */
fun starsFor(result: GameResult): Int {
    if (result.total == 0) return 0
    val share = result.correct.toFloat() / result.total
    return when {
        share >= 0.9f -> 3
        share >= 0.6f -> 2
        share > 0f -> 1
        else -> 0
    }
}

/** End screen shared by every game (frame G0 in Figma). NADALJUJ hands the result back. */
@Composable
fun GameEnd(result: GameResult, onContinue: () -> Unit) {
    val stars = starsFor(result)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // TODO(UQ-48): replace with the Učko image (happy when stars >= 2, sad otherwise)
        Text(if (stars >= 2) "🐱" else "😿", fontSize = 72.sp)
        Spacer(Modifier.height(16.dp))
        Text("Igra končana!", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Text(
            "${result.correct} / ${result.total} pravilnih",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = GameColors.Correct,
        )
        Text("Čas ${formatTime(result.seconds)}", color = Color.Gray)
        Spacer(Modifier.height(16.dp))
        Text(
            "★".repeat(stars) + "☆".repeat(3 - stars),
            fontSize = 40.sp,
            color = GameColors.Gold,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GameColors.Correct),
        ) {
            Text("NADALJUJ", fontWeight = FontWeight.Bold)
        }
    }
}
