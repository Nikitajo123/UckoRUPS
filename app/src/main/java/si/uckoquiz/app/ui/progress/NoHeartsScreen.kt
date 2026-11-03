package si.uckoquiz.app.ui.progress

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import si.uckoquiz.app.R
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.UckoRed

/** Učenec nima več srčkov: nove lekcije počakajo do jutri, stare lahko vadi (in si tako vrne srček). */
@Composable
fun NoHeartsScreen(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(painterResource(R.drawable.ucko_sad), contentDescription = "Učko je žalosten", modifier = Modifier.size(160.dp))
            Spacer(Modifier.height(12.dp))
            UckoIcon(UckoIconType.HEART, Modifier.size(36.dp), color = UckoRed)
            Spacer(Modifier.height(8.dp))
            Text("Zmanjkalo ti je srčkov", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(
                "Jutri se srčki spet napolnijo. Do takrat lahko ponoviš že opravljeno lekcijo – če dobiš vsaj 2 zvezdici, dobiš srček nazaj.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(28.dp))
            UckoButton(text = "Nazaj na pot", onClick = onBack)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun NoHeartsPreview() {
    UckoQuizTheme { NoHeartsScreen(onBack = {}) }
}
