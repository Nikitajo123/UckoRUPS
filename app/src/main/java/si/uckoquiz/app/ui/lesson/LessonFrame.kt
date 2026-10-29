package si.uckoquiz.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.ui.components.CorrectGreen
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.components.UckoMascot
import si.uckoquiz.app.ui.components.UckoMood
import si.uckoquiz.app.ui.theme.TextDark
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.UckoRed

// Boi za povratna informacija (Figma 12a in 12b)
private val CorrectBackground = Color(0xFFD7F5C0)
private val CorrectText = Color(0xFF3A8A00)
private val WrongBackground = Color(0xFFFFDFDF)
private val TrackGrey = Color(0xFFE6E6E6)
private val CardGrey = Color(0xFFF2F2F2)

/** Zgornja vrstica lekcije: X za izhod, vrstica napredka v barvi predmeta, srčki. */
@Composable
fun LessonTopBar(progress: Float, hearts: Int, color: Color, onClose: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "✕",
            fontSize = 22.sp,
            color = Color.Gray,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClose)
                .padding(6.dp)
                .semantics { contentDescription = "Zapri lekcijo" },
        )
        Spacer(Modifier.width(10.dp))
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(10.dp).clip(RoundedCornerShape(50)),
            color = color,
            trackColor = TrackGrey,
        )
        Spacer(Modifier.width(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "Srčki: $hearts" },
        ) {
            UckoIcon(UckoIconType.HEART, Modifier.size(22.dp), color = UckoRed)
            Spacer(Modifier.width(4.dp))
            Text("$hearts", color = UckoRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

// nadpis nad nalogite (npr. IZBERI PRAVILNI ODGOVOR) i prashanjeto. */
@Composable
fun TaskHeader(label: String, prompt: String, color: Color) {
    Column(Modifier.fillMaxWidth()) {
        Text(label.uppercase(), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(6.dp))
        Text(prompt, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun LessonBubble(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = TextDark)
    }
}

/** Učkova razlaga (Figma 07): Učko so naslov, kartica so cekorai na razlagata i poenti za cekorite. */
@Composable
fun ExplanationPage(title: String, step: String, stepIndex: Int, stepCount: Int, color: Color) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            UckoMascot(Modifier.size(84.dp))
            Spacer(Modifier.width(8.dp))
            LessonBubble(title, Modifier.padding(top = 8.dp).weight(1f))
        }
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(CardGrey)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(step, style = MaterialTheme.typography.titleLarge, color = TextDark, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(stepCount) { i ->
                Box(Modifier.size(8.dp).clip(CircleShape).background(if (i == stepIndex) color else Color(0xFFCCCCCC)))
            }
        }
    }
}

/**
 * Dolen tab koga ke se odgovori prasanje (Figma 12a pravilno, 12b napačno):
 * naslov, razlaga, vesel ali žalosten Učko in gumb NAPREJ v zeleni ali rdeči barvi
 */
@Composable
fun FeedbackPanel(correct: Boolean, title: String, text: String, onContinue: () -> Unit) {
    val textColor = if (correct) CorrectText else UckoRed
    Column(
        Modifier
            .fillMaxWidth()
            .background(if (correct) CorrectBackground else WrongBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(title, color = textColor, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                if (text.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(text, color = textColor, style = MaterialTheme.typography.bodyMedium)
                }
            }
            UckoMascot(Modifier.size(56.dp), mood = if (correct) UckoMood.HAPPY else UckoMood.SAD)
        }
        Spacer(Modifier.height(14.dp))
        UckoButton(text = "Naprej", onClick = onContinue, color = if (correct) CorrectGreen else UckoRed)
    }
}

/** Gumb PREVERI (zelen); onemogočen, dokler učenec ne izbere odgovora. */
@Composable
fun CheckButton(enabled: Boolean, onClick: () -> Unit) {
    UckoButton(text = "Preveri", onClick = onClick, color = CorrectGreen, enabled = enabled)
}

/** Gumb NAPREJ za razlago (oranžen kot v Figmi). */
@Composable
fun ContinueButton(onClick: () -> Unit) {
    UckoButton(text = "Naprej", onClick = onClick, color = UckoOrange)
}

@Preview(showBackground = true)
@Composable
private fun FeedbackPreview() {
    UckoQuizTheme {
        Column {
            LessonTopBar(progress = 0.4f, hearts = 4, color = UckoOrange, onClose = {})
            FeedbackPanel(true, "Odlično!", "Pojedel je 3 od 8 kosov, torej 3/8.", onContinue = {})
            Spacer(Modifier.height(12.dp))
            FeedbackPanel(false, "Pravilni odgovor: 3/8", "Pojedel je 3 od 8 kosov, torej 3/8.", onContinue = {})
        }
    }
}
