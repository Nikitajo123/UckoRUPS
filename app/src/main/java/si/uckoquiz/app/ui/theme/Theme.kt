package si.uckoquiz.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = UckoOrange,
    onPrimary = Color.White,
    secondary = UckoBlue,
    onSecondary = Color.White,
    tertiary = UckoGreen,
    onTertiary = Color.White,
    error = UckoRed,
    background = BackgroundLight,
    onBackground = TextDark,
    surface = SurfaceLight,
    onSurface = TextDark,
)

private val DarkColors = darkColorScheme(
    primary = UckoOrange,
    onPrimary = Color.White,
    secondary = UckoBlue,
    onSecondary = Color.White,
    tertiary = UckoGreen,
    onTertiary = Color.White,
    error = UckoRed,
    background = BackgroundDark,
    onBackground = TextLight,
    surface = SurfaceDark,
    onSurface = TextLight,
)

// Zaobljeni gumbi in kartice
private val UckoShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun UckoQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        shapes = UckoShapes,
        content = content
    )
}