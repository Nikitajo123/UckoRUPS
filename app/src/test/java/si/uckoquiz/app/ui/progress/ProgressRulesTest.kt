package si.uckoquiz.app.ui.progress

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.ui.lesson.LessonOutcome
import java.time.LocalDate

class ProgressRulesTest {
    private val today = LocalDate.of(2026, 11, 3)
    private fun outcome(stars: Int, xp: Int = 14, hearts: Int = 4, boss: Boolean = false) =
        LessonOutcome(correct = 2, total = 2, mistakes = emptyList(), seconds = 60, heartsLeft = hearts, stars = stars, xp = xp, boss = boss)

    @Test
    fun lessonAddsXpAndTakesLostHearts() {
        val p = ProgressRules.afterLesson(UserProfile(xp = 100, hearts = 5), outcome(stars = 3, hearts = 3), today, practice = false)
        assertEquals(114, p.xp)
        assertEquals(3, p.hearts)
        assertEquals("2026-11-03", p.lastActiveDate)
    }

    @Test
    fun streakGrowsOnConsecutiveDays() {
        val yesterday = UserProfile(streak = 4, lastActiveDate = "2026-11-02")
        assertEquals(5, ProgressRules.afterLesson(yesterday, outcome(2), today, false).streak)
        val sameDay = UserProfile(streak = 5, lastActiveDate = "2026-11-03")
        assertEquals(5, ProgressRules.afterLesson(sameDay, outcome(2), today, false).streak)
        val longAgo = UserProfile(streak = 9, lastActiveDate = "2026-10-20")
        assertEquals(1, ProgressRules.afterLesson(longAgo, outcome(2), today, false).streak)
    }

    @Test
    fun failedLessonDoesNotCountForStreak() {
        val p = ProgressRules.afterLesson(UserProfile(streak = 4, lastActiveDate = "2026-11-02"), outcome(0, xp = 0, hearts = 0), today, false)
        assertEquals(4, p.streak)
        assertEquals("2026-11-02", p.lastActiveDate)
        assertEquals(0, p.hearts)
    }

    @Test
    fun practiceKeepsHeartsAndGoodPracticeGivesOneBack() {
        val p = UserProfile(hearts = 2)
        assertEquals(3, ProgressRules.afterLesson(p, outcome(stars = 2, hearts = 1), today, practice = true).hearts)
        assertEquals(2, ProgressRules.afterLesson(p, outcome(stars = 1, hearts = 1), today, practice = true).hearts)
        assertEquals(5, ProgressRules.afterLesson(UserProfile(hearts = 5), outcome(3), today, practice = true).hearts)
    }

    @Test
    fun bossDoesNotUseProfileHearts() {
        assertEquals(4, ProgressRules.afterLesson(UserProfile(hearts = 4), outcome(3, hearts = 1, boss = true), today, false).hearts)
    }

    @Test
    fun newDayRefillsHeartsAndBreaksOldStreak() {
        val fromYesterday = ProgressRules.refreshForToday(UserProfile(hearts = 1, streak = 3, lastActiveDate = "2026-11-02"), today)
        assertEquals(5, fromYesterday.hearts)
        assertEquals(3, fromYesterday.streak)
        val old = ProgressRules.refreshForToday(UserProfile(hearts = 0, streak = 7, lastActiveDate = "2026-10-30"), today)
        assertEquals(0, old.streak)
        val sameDay = ProgressRules.refreshForToday(UserProfile(hearts = 2, streak = 3, lastActiveDate = "2026-11-03"), today)
        assertEquals(2, sameDay.hearts)
    }

    @Test
    fun withoutHeartsOnlyPracticeIsAllowed() {
        assertFalse(ProgressRules.canStart(UserProfile(hearts = 0), lessonAlreadyDone = false))
        assertTrue(ProgressRules.canStart(UserProfile(hearts = 0), lessonAlreadyDone = true))
        assertEquals(5, ProgressRules.startHearts(UserProfile(hearts = 2), practice = true))
        assertEquals(2, ProgressRules.startHearts(UserProfile(hearts = 2), practice = false))
    }
}
