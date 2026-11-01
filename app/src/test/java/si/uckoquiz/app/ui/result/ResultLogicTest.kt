package si.uckoquiz.app.ui.result

import org.junit.Assert.assertEquals
import org.junit.Test
import si.uckoquiz.app.ui.lesson.LessonOutcome

class ResultLogicTest {
    private fun outcome(correct: Int, total: Int, hearts: Int = 3, stars: Int = 0, boss: Boolean = false) =
        LessonOutcome(correct = correct, total = total, mistakes = emptyList(), seconds = 0, heartsLeft = hearts, stars = stars, boss = boss)

    @Test
    fun accuracyIsShareOfCorrectAnswers() {
        assertEquals(80, accuracyPercent(outcome(4, 5)))
        assertEquals(100, accuracyPercent(outcome(0, 0)))
    }

    @Test
    fun durationIsMinutesAndSeconds() {
        assertEquals("3:42", formatDuration(222))
        assertEquals("0:05", formatDuration(5))
        assertEquals("0:00", formatDuration(-3))
    }

    @Test
    fun titleDependsOnResult() {
        assertEquals("Lekcija končana!", resultTitle(outcome(4, 5, stars = 2)))
        assertEquals("Zmanjkalo je srčkov", resultTitle(outcome(1, 5, hearts = 0)))
        assertEquals("Boss premagan!", resultTitle(outcome(5, 5, stars = 3, boss = true)))
    }

    @Test
    fun oneStarExplainsTheUnlockRule() {
        assertEquals(
            "Za odklep naslednjega poglavja potrebuješ vsaj 2 zvezdici. Ponovi lekcijo!",
            resultMessage(outcome(2, 5, stars = 1)),
        )
    }
}
