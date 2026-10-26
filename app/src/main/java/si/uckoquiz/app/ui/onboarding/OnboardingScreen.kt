package si.uckoquiz.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.ui.components.OptionCard
import si.uckoquiz.app.ui.components.OptionState
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme

private val AVATARS = listOf("🦊", "🐻", "🐼", "🐸", "🐯", "🐨", "🦁", "🐰")
private val DAILY_GOALS = listOf(5 to "Lahkotno", 10 to "Redno", 15 to "Zagnano")
private const val LAST_STEP = 3

// ob prvem zagonu: pozdrav, ime, avatar, dnevni cilj (v minutah)
@Composable
fun OnboardingScreen(onFinish: (UserProfile) -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableStateOf(AVATARS.first()) }
    var dailyGoal by rememberSaveable { mutableIntStateOf(10) }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            LinearProgressIndicator(
                progress = { (step + 1) / (LAST_STEP + 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(50)),
                color = UckoOrange,
                trackColor = Color(0xFFE6E6E6),
            )
            Spacer(Modifier.height(32.dp))

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (step) {
                    0 -> WelcomeStep()
                    1 -> NameStep(name) { name = it }
                    2 -> AvatarStep(avatar) { avatar = it }
                    else -> GoalStep(dailyGoal) { dailyGoal = it }
                }
            }

            UckoButton(
                text = if (step < LAST_STEP) "Naprej" else "Začni",
                enabled = step != 1 || name.isNotBlank(),
                onClick = {
                    if (step < LAST_STEP) {
                        step++
                    } else {
                        onFinish(UserProfile(displayName = name.trim(), avatar = avatar, dailyGoal = dailyGoal))
                    }
                },
            )
        }
    }
}

@Composable
private fun StepTitle(text: String) {
    Text(text, style = MaterialTheme.typography.headlineMedium)
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun WelcomeStep() {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        // TODO: ko Simona doda sliko: Image(painterResource(R.drawable.ucko_happy), contentDescription = "Učko")
        Text("🐾", fontSize = 96.sp)
        Spacer(Modifier.height(24.dp))
        Text("Živijo! Jaz sem Učko.", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text(
            "Skupaj se bova učila matematiko, angleščino in naravoslovje. Najprej se spoznajva!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun NameStep(name: String, onChange: (String) -> Unit) {
    Column {
        StepTitle("Kako ti je ime?")
        OutlinedTextField(
            value = name,
            onValueChange = { if (it.length <= 20) onChange(it) },
            label = { Text("Ime ali vzdevek") },
            supportingText = { Text("Priimka ne vpisuj.") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AvatarStep(selected: String, onSelect: (String) -> Unit) {
    Column {
        StepTitle("Izberi svoj avatar")
        AVATARS.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { emoji ->
                    OptionCard(
                        state = if (emoji == selected) OptionState.SELECTED else OptionState.DEFAULT,
                        onClick = { onSelect(emoji) },
                        modifier = Modifier.weight(1f).aspectRatio(1f),
                    ) {
                        Text(emoji, fontSize = 36.sp)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun GoalStep(selected: Int, onSelect: (Int) -> Unit) {
    Column {
        StepTitle("Koliko časa na dan?")
        DAILY_GOALS.forEach { (minutes, label) ->
            OptionCard(
                state = if (minutes == selected) OptionState.SELECTED else OptionState.DEFAULT,
                onClick = { onSelect(minutes) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("$minutes minut", fontWeight = FontWeight.Bold)
                    Text(label)
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun OnboardingPreview() {
    UckoQuizTheme { OnboardingScreen(onFinish = {}) }
}