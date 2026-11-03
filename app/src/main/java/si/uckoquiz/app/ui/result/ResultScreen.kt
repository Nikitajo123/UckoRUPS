package si.uckoquiz.app.ui.result

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import si.uckoquiz.app.R
import si.uckoquiz.app.ui.components.CorrectGreen
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.lesson.LessonOutcome
import si.uckoquiz.app.ui.theme.UckoBlue
import si.uckoquiz.app.ui.theme.UckoGold
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.UckoRed

// Barve iz Figme 13_Rezultat
private val TitleGold = Color(0xFFF2B300)
private val StarGrey = Color(0xFFD6D6D6)
private val AccuracyGreen = Color(0xFF58A700)

/* pravila prikaza (brez Androida, testirana) */

/** Natančnost v odstotkih: delež pravilnih prvih odgovorov (lekcija brez nalog = 100 %). */
fun accuracyPercent(outcome: LessonOutcome): Int =
    if (outcome.total == 0) 100 else outcome.correct * 100 / outcome.total

/** Čas kot m:ss (npr. 222 s = "3:42"). */
fun formatDuration(seconds: Int): String {
    val safe = seconds.coerceAtLeast(0)
    return "${safe / 60}:${(safe % 60).toString().padStart(2, '0')}"
}

/** Naslov zaslona. */
fun resultTitle(outcome: LessonOutcome): String = when {
    !outcome.passed -> "Zmanjkalo je srčkov"
    outcome.boss -> "Boss premagan!"
    else -> "Lekcija končana!"
}

/** Kratko sporočilo pod zvezdicami: spodbuda in pravilo za odklep (vsaj 2 zvezdici). */
fun resultMessage(outcome: LessonOutcome): String = when {
    !outcome.passed -> "Nič hudega, poskusi znova. Zmoreš!"
    outcome.stars == 3 -> "Popolno! Vse prav v prvem poskusu."
    outcome.unlocksNextChapter -> "Bravo! Nadaljuj na naslednjo lekcijo."
    else -> "Za odklep naslednjega poglavja potrebuješ vsaj 2 zvezdici. Ponovi lekcijo!"
}

/* zaslon */

/**
 * Zaslon rezultata (Figma 13_Rezultat): plešoči Učko, naslov, zvezdice (pojavijo se ena za drugo),
 * XP, natančnost in čas, niz dni ter gumba NADALJUJ in PONOVI.
 * Če je lekcija neuspela (brez srčkov), je Učko žalosten in glavni gumb je PONOVI.
 */
@Composable
fun ResultScreen(
    outcome: LessonOutcome,
    streak: Int,
    onContinue: () -> Unit,
    onRetry: () -> Unit,
) {
    BackHandler(onBack = onContinue)
    // Zmaga nad bossom ima svoj zaslon (Figma 15b); poraz je enak kot pri navadni lekciji.
    if (outcome.boss && outcome.passed) {
        BossWinScreen(outcome, onContinue)
        return
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(24.dp))
                Image(
                    painter = painterResource(if (outcome.passed) R.drawable.ucko_dance else R.drawable.ucko_sad),
                    contentDescription = if (outcome.passed) "Učko pleše" else "Učko je žalosten",
                    modifier = Modifier.size(170.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    resultTitle(outcome),
                    color = if (outcome.passed) TitleGold else UckoRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                StarsRow(outcome.stars)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatBox("XP", "+${outcome.xp}", UckoGold, Modifier.weight(1f))
                    StatBox("Natančnost", "${accuracyPercent(outcome)} %", AccuracyGreen, Modifier.weight(1f))
                    StatBox("Čas", formatDuration(outcome.seconds), UckoBlue, Modifier.weight(1f))
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "Niz: $streak dni" },
                ) {
                    UckoIcon(UckoIconType.DROP, Modifier.size(20.dp), color = UckoOrange)
                    Spacer(Modifier.width(6.dp))
                    Text("Niz: $streak dni", color = UckoOrange, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    resultMessage(outcome),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(16.dp))
            if (outcome.passed) {
                UckoButton(text = "Nadaljuj", onClick = onContinue, color = CorrectGreen)
                Spacer(Modifier.height(10.dp))
                SecondaryButton(text = "Ponovi", onClick = onRetry)
            } else {
                UckoButton(text = "Poskusi znova", onClick = onRetry, color = UckoOrange)
                Spacer(Modifier.height(10.dp))
                SecondaryButton(text = "Nazaj", onClick = onContinue)
            }
        }
    }
}

/** Tri zvezdice; srednja je večja in višja  Pridobljene se pojavijo ena za drugo. */
@Composable
private fun StarsRow(stars: Int) {
    val inPreview = LocalInspectionMode.current
    var shown by remember(stars) { mutableIntStateOf(if (inPreview) stars else 0) }
    LaunchedEffect(stars) {
        while (shown < stars) {
            delay(350)
            shown++
        }
    }
    Row(
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$stars od 3 zvezdic" },
    ) {
        repeat(3) { i ->
            val filled = i < shown
            val scale by animateFloatAsState(
                targetValue = if (filled) 1f else 0.85f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "star$i",
            )
            val size: Dp = if (i == 1) 64.dp else 52.dp
            UckoIcon(
                UckoIconType.STAR,
                Modifier
                    .offset(y = if (i == 1) (-10).dp else 0.dp)
                    .padding(horizontal = 4.dp)
                    .size(size)
                    .scale(scale),
                color = if (filled) UckoGold else StarGrey,
            )
        }
    }
}

/** Okvir s statistiko (Figma: XP, Natančnost, Čas): barvni napis zgoraj, vrednost spodaj. */
@Composable
private fun StatBox(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .clip(shape)
            .border(2.dp, color, shape)
            .semantics(mergeDescendants = true) { contentDescription = "$label: $value" },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.fillMaxWidth().background(color).padding(vertical = 4.dp), contentAlignment = Alignment.Center) {
            Text(label, color = Color.White, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
        }
        Text(
            value,
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }
}

/** Bel gumb s sivim robom (PONOVI, NAZAJ). */
@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(2.dp, Color(0xFFE0E0E0)),
    ) {
        Text(text.uppercase(), color = Color(0xFF737373), style = MaterialTheme.typography.labelLarge)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultPassedPreview() {
    UckoQuizTheme {
        ResultScreen(
            outcome = LessonOutcome(correct = 4, total = 5, mistakes = listOf("t3"), seconds = 222, heartsLeft = 4, stars = 2, xp = 18),
            streak = 4,
            onContinue = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ResultFailedPreview() {
    UckoQuizTheme {
        ResultScreen(
            outcome = LessonOutcome(correct = 1, total = 5, mistakes = emptyList(), seconds = 95, heartsLeft = 0),
            streak = 4,
            onContinue = {},
            onRetry = {},
        )
    }
}
