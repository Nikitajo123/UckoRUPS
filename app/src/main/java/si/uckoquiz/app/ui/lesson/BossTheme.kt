package si.uckoquiz.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.theme.UckoRed

/** Temno modro ozadje boss levela (Figma 15_Boss_level in 15b_Boss_zmaga). */
val BossNavy = Color(0xFF2B2540)

/**
 * Za boss level zamenja ozadje v temno modro in besedilo v belo; kartice z odgovori ostanejo bele.
 * Za navadne lekcije samo pokaže vsebino.
 */
@Composable
fun BossTheme(enabled: Boolean, content: @Composable () -> Unit) {
    if (!enabled) {
        content()
        return
    }
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(background = BossNavy, onBackground = Color.White),
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        Surface(Modifier.fillMaxSize(), color = BossNavy, contentColor = Color.White) {
            content()
        }
    }
}

/** Rdeča oznaka BOSS z gradom pod zgornjo vrstico (Figma 15). */
@Composable
fun BossBadge(modifier: Modifier = Modifier) {
    Row(
        modifier
            .background(UckoRed, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .semantics(mergeDescendants = true) { contentDescription = "Boss level" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UckoIcon(UckoIconType.CASTLE, Modifier.size(16.dp), color = Color.White, cutout = UckoRed)
        Spacer(Modifier.width(6.dp))
        Text("BOSS", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}
