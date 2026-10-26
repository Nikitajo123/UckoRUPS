package si.uckoquiz.app.ui.theme

import androidx.compose.ui.graphics.Color

// Barve predmetov
val UckoOrange = Color(0xFFF7A04F)   // Matematika
val UckoBlue = Color(0xFF4F8EF7)     // Angleščina
val UckoGreen = Color(0xFF4FBF7A)    // Naravoslovje

// Barve za igro in učno pot
val UckoGold = Color(0xFFFFC83D)     // opravljene lekcije, zvezdice
val UckoRed = Color(0xFFE5484D)      // napačen odgovor, srčki
val LockedGrey = Color(0xFFBDBDBD)   // zaklenjene lekcije

// Ozadje in besedilo
val BackgroundLight = Color(0xFFFFFBF5)
val SurfaceLight = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1F2933)

val BackgroundDark = Color(0xFF15171C)
val SurfaceDark = Color(0xFF1F2229)
val TextLight = Color(0xFFF1F3F5)

/** Barva predmeta po njegovem id-ju (mat, ang, nar). */
fun subjectColor(subjectId: String): Color = when (subjectId) {
    "mat" -> UckoOrange
    "ang" -> UckoBlue
    "nar" -> UckoGreen
    else -> Color.Gray
}