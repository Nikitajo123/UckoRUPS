package si.uckoquiz.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import si.uckoquiz.app.ui.theme.LockedGrey
import si.uckoquiz.app.ui.theme.TextDark
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoRed

// za gumb PREVERI in pravilni odgovor
val CorrectGreen = Color(0xFF58CC02)

/** Velik zaobljen gumb (NAPREJ, PREVERI, ZAČNI). */
@Composable
fun UckoButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = UckoOrange,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White,
            disabledContainerColor = LockedGrey,
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp, pressedElevation = 0.dp),
    ) {
        Text(text.uppercase(), style = MaterialTheme.typography.labelLarge)
    }
}

enum class OptionState { DEFAULT, SELECTED, CORRECT, WRONG }

/** Karticki za izbor na: odgovor v naloga, avatar, dnevni cilj  */
@Composable
fun OptionCard(
    state: OptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selectedColor: Color = UckoOrange,
    content: @Composable () -> Unit,
) {
    val background: Color
    val border: Color
    val textColor: Color
    when (state) {
        OptionState.DEFAULT -> {
            background = MaterialTheme.colorScheme.surface
            border = Color(0xFFE0E0E0)
            textColor = MaterialTheme.colorScheme.onSurface
        }
        OptionState.SELECTED -> {
            // Figma: oranžna pri nalogah, modra pri izbiri dnevnega cilja
            background = lerp(Color.White, selectedColor, 0.12f)
            border = selectedColor
            textColor = if (selectedColor == UckoOrange) TextDark else selectedColor
        }
        OptionState.CORRECT -> {
            background = Color(0xFFDDF7C8)
            border = CorrectGreen
            textColor = Color(0xFF3A8A00)
        }
        OptionState.WRONG -> {
            background = Color(0xFFFFE0E0)
            border = UckoRed
            textColor = UckoRed
        }
    }
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(background)
            .border(2.dp, border, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides textColor) {
            content()
        }
    }
}