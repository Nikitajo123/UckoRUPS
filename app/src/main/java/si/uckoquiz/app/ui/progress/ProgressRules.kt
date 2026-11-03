package si.uckoquiz.app.ui.progress

import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.ui.home.STARS_TO_UNLOCK_CHAPTER
import si.uckoquiz.app.ui.lesson.LESSON_HEARTS
import si.uckoquiz.app.ui.lesson.LessonOutcome
import java.time.LocalDate

/**
 * Pravila za niz, XP in srčke v profilu (UQ-38), po načrtu aplikacije:
 * - XP: vsaka lekcija prišteje svoj XP k skupnemu.
 * - Niz: šteje zaporedne dni z vsaj eno uspešno lekcijo; dan brez učenja ga prekine.
 * - Srčki: 5 jih je; napaka v novi lekciji vzame srček (tudi v profilu).
 *   Obnovijo se vsak nov dan ali z vajo starih lekcij (vaja z vsaj 2 zvezdicama vrne 1 srček).
 */
object ProgressRules {

    /** Ob odprtju aplikacije: nov dan napolni srčke, niz se prekine, če včeraj ni bilo učenja. */
    fun refreshForToday(profile: UserProfile, today: LocalDate): UserProfile {
        val last = parseDate(profile.lastActiveDate)
        val newDay = last == null || last.isBefore(today)
        val streakBroken = last == null || last.isBefore(today.minusDays(1))
        return profile.copy(
            hearts = if (newDay) LESSON_HEARTS else profile.hearts,
            streak = if (streakBroken) 0 else profile.streak,
        )
    }

    /**
     * Ali lahko učenec začne lekcijo. Brez srčkov lahko vadi samo že opravljene lekcije.
     */
    fun canStart(profile: UserProfile, lessonAlreadyDone: Boolean): Boolean =
        profile.hearts > 0 || lessonAlreadyDone

    /** S koliko srčki se lekcija začne: nova lekcija s srčki iz profila, vaja vedno s polnimi. */
    fun startHearts(profile: UserProfile, practice: Boolean): Int =
        if (practice) LESSON_HEARTS else profile.hearts.coerceIn(1, LESSON_HEARTS)

    /**
     * Profil po končani lekciji.
     * [practice] = lekcija je bila že opravljena (vaja): srčki iz profila se ne porabijo.
     */
    fun afterLesson(profile: UserProfile, outcome: LessonOutcome, today: LocalDate, practice: Boolean): UserProfile {
        val hearts = when {
            practice && outcome.stars >= STARS_TO_UNLOCK_CHAPTER -> (profile.hearts + 1).coerceAtMost(LESSON_HEARTS)
            practice || outcome.boss -> profile.hearts // boss ima svoja 3 življenja
            else -> outcome.heartsLeft.coerceIn(0, LESSON_HEARTS)
        }
        val last = parseDate(profile.lastActiveDate)
        val streak = when {
            !outcome.passed -> profile.streak
            last == today -> profile.streak.coerceAtLeast(1)
            last == today.minusDays(1) -> profile.streak + 1
            else -> 1
        }
        return profile.copy(
            xp = profile.xp + outcome.xp,
            hearts = hearts,
            streak = streak,
            lastActiveDate = if (outcome.passed) today.toString() else profile.lastActiveDate,
        )
    }

    /** lastActiveDate je zapisan kot yyyy-MM-dd (tako je v podatkovnem modelu). */
    private fun parseDate(text: String): LocalDate? = runCatching { LocalDate.parse(text) }.getOrNull()
}
