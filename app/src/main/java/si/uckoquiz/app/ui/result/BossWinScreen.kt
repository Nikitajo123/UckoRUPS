package si.uckoquiz.app.ui.result

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.R
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.lesson.BossTheme
import si.uckoquiz.app.ui.lesson.LessonOutcome
import si.uckoquiz.app.ui.theme.UckoGold
import si.uckoquiz.app.ui.theme.UckoQuizTheme

/** Zmaga nad bossom (Figma 15b_Boss_zmaga): temno ozadje, pokal, nagrada XP, plešoči Učko, NADALJUJ. */
@Composable
fun BossWinScreen(outcome: LessonOutcome, onContinue: () -> Unit) {
    BossTheme(enabled = true) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))
            Text("🏆", fontSize = 72.sp)
            Spacer(Modifier.height(16.dp))
            Text("Svet opravljen!", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text("Boss je premagan.", style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.75f))
            Spacer(Modifier.height(20.dp))
            Row(
                Modifier
                    .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UckoIcon(UckoIconType.STAR, Modifier.size(22.dp), color = UckoGold)
                Spacer(Modifier.width(8.dp))
                Text("+${outcome.xp} XP", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(28.dp))
            Image(painterResource(R.drawable.ucko_dance), contentDescription = "Učko pleše", modifier = Modifier.size(170.dp))
            Spacer(Modifier.weight(1f))
            UckoButton(text = "Nadaljuj", onClick = onContinue, color = UckoGold)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BossWinPreview() {
    UckoQuizTheme {
        BossWinScreen(
            LessonOutcome(correct = 6, total = 7, mistakes = emptyList(), seconds = 180, heartsLeft = 2, stars = 2, xp = 62, boss = true),
            onContinue = {},
        )
    }
}
