package si.uckoquiz.app.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import si.uckoquiz.app.R

enum class UckoMood(@DrawableRes val drawable: Int, val description: String) {
    HAPPY(R.drawable.ucko_happy, "Učko je vesel"),
    SAD(R.drawable.ucko_sad, "Učko je žalosten"),
    CHEER(R.drawable.ucko_cheer, "Učko navija"),
    DANCE(R.drawable.ucko_dance, "Učko pleše"),
}

@Composable
fun UckoMascot(modifier: Modifier = Modifier, mood: UckoMood = UckoMood.HAPPY) {
    Image(
        painter = painterResource(mood.drawable),
        contentDescription = mood.description,
        contentScale = ContentScale.Fit,
        modifier = modifier.aspectRatio(1f),
    )
}
