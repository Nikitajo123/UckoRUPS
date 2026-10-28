package si.uckoquiz.app.ui.lesson

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.theme.LockedGrey

/**
 * Začasni zaslon lekcije, dokler ni narejen pravi (UQ-31).
 * Gumb "Končaj" shrani 3 zvezdice, da se na poti preizkusi odklepanje.
 */
@Composable
fun LessonPlaceholder(lessonId: String, repository: ContentRepository, onClose: () -> Unit) {
    val scope = rememberCoroutineScope()
    BackHandler(onBack = onClose)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Lekcija: $lessonId", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Zaslon lekcije pride z UQ-31.", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(24.dp))
            UckoButton(
                text = "Končaj s 3 zvezdicami",
                onClick = {
                    scope.launch {
                        repository.saveResult(lessonId, stars = 3, xp = 10, mistakes = emptyList())
                        onClose()
                    }
                },
            )
            Spacer(Modifier.height(12.dp))
            UckoButton(text = "Nazaj", onClick = onClose, color = LockedGrey)
        }
    }
}
