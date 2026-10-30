package si.uckoquiz.app.ui.lesson

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import si.uckoquiz.app.domain.Task
import si.uckoquiz.app.ui.theme.UckoBlue
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import java.util.Locale

/** Hitrost govora: običajna in "počasi" (Figma 11). */
private const val NORMAL_RATE = 0.9f
private const val SLOW_RATE = 0.5f

/** Vmesnik za predvajanje zvoka. ListenPlayer pozna samo ta vmesnik, zato predogled ne potrebuje TextToSpeech. */
interface Speaker {
    val ready: Boolean
    val available: Boolean
    fun speak(text: String, slow: Boolean = false)
    fun shutdown()
}

/**
 * Ovoj okoli Androidovega TextToSpeech: angleški glas (britanski ili americki).
 * [available] je false, če naprava nima angleskega glasu ali TTS sploh ne deluje.
 */
class AndroidSpeaker(context: Context) : Speaker, TextToSpeech.OnInitListener {
    private val tts = TextToSpeech(context.applicationContext, this)

    // Stanje je v mutableStateOf, da se zaslon osveži, ko je glas pripravljen.
    private var readyState by mutableStateOf(false)
    private var availableState by mutableStateOf(true)
    override val ready: Boolean get() = readyState
    override val available: Boolean get() = availableState

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            availableState = false
            return
        }
        // setLanguage vrne negativno število, če jezika ni (LANG_MISSING_DATA, LANG_NOT_SUPPORTED)
        val ok = tts.setLanguage(Locale.UK) >= 0 || tts.setLanguage(Locale.US) >= 0
        availableState = ok
        readyState = ok
    }

    override fun speak(text: String, slow: Boolean) {
        if (!ready || text.isBlank()) return
        tts.setSpeechRate(if (slow) SLOW_RATE else NORMAL_RATE)
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ucko_listen")
    }

    override fun shutdown() {
        try {
            tts.stop()
            tts.shutdown()
        } catch (_: Throwable) {}
    }
}

/** Ustvari Speaker za čas, ko je zaslon prikazan, in ga ob odhodu ugasne. V predogledu vrne null. */
@Composable
fun rememberSpeaker(): Speaker? {
    if (LocalInspectionMode.current) return null
    val context = LocalContext.current
    // Če TextToSpeech ne uspe (redko), vrne null in naloga pokaže besedilo namesto zvoka.
    val speaker: Speaker? = remember { runCatching { AndroidSpeaker(context) }.getOrNull() }
    DisposableEffect(speaker) {
        onDispose { speaker?.shutdown() }
    }
    return speaker
}

/** Besedilo, ki ga prebere glas: task.audioText, sicer pravilni odgovor. */
fun listenText(task: Task): String =
    task.audioText.ifBlank { task.correct.mapNotNull { task.answers.getOrNull(it) }.joinToString(", ") }

/**
 * Predvajalnik za nalogo poslušanja (Figma 11): velik gumb z zvočnikom in manjši gumb "počasi".
 * Besedilo se ob prikazu naloge enkrat predvaja samo. Če glas ni na voljo, pokaže besedilo.
 */
@Composable
fun ListenPlayer(task: Task, color: Color) {
    val speaker = rememberSpeaker()
    val text = listenText(task)
    var playedOnce by remember(task.id) { mutableStateOf(false) }

    LaunchedEffect(task.id, speaker?.ready) {
        if (speaker != null && speaker.ready && !playedOnce) {
            speaker.speak(text)
            playedOnce = true
        }
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            // velik gumb (normalna hitrost)
            Box(
                Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { speaker?.speak(text) }
                    .semantics { contentDescription = "Predvajaj" },
                contentAlignment = Alignment.Center,
            ) {
                SpeakerIcon(Modifier.size(44.dp))
            }
            Spacer(Modifier.width(14.dp))
            // manjši gumb "počasi"
            Box(
                Modifier
                    .offset(y = 16.dp)
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEDEDED))
                    .clickable { speaker?.speak(text, slow = true) }
                    .semantics { contentDescription = "Predvajaj počasi" },
                contentAlignment = Alignment.Center,
            ) {
                Text("počasi", color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
        if (speaker != null && !speaker.available) {
            Spacer(Modifier.height(12.dp))
            Text(
                "Zvok na tej napravi ni na voljo. Besedilo: \"$text\"",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Bela ikona zvočnika z dvema valovoma. */
@Composable
private fun SpeakerIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension / 24f
        val body = Path().apply {
            moveTo(3f * s, 9f * s)
            lineTo(7f * s, 9f * s)
            lineTo(12f * s, 4.5f * s)
            lineTo(12f * s, 19.5f * s)
            lineTo(7f * s, 15f * s)
            lineTo(3f * s, 15f * s)
            close()
        }
        drawPath(body, Color.White)
        val wave = Stroke(width = 2f * s, cap = StrokeCap.Round)
        drawArc(Color.White, -50f, 100f, false, topLeft = Offset(9f * s, 7.5f * s), size = Size(9f * s, 9f * s), style = wave)
        drawArc(Color.White, -50f, 100f, false, topLeft = Offset(7f * s, 4f * s), size = Size(15f * s, 16f * s), style = wave)
    }
}

@Preview(showBackground = true)
@Composable
private fun ListenPlayerPreview() {
    UckoQuizTheme {
        ListenPlayer(Task(id = "l", type = "listen", audioText = "my brother"), UckoBlue)
    }
}