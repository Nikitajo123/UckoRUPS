package si.uckoquiz.app.ui.profile

import org.junit.Assert.assertEquals
import org.junit.Test
import si.uckoquiz.app.domain.LessonProgress

class ProfileLogicTest {
    @Test
    fun levelGrowsEvery250Xp() {
        assertEquals(1, levelFor(0))
        assertEquals(1, levelFor(249))
        assertEquals(2, levelFor(250))
        assertEquals(3, levelFor(560))
        assertEquals(1, levelFor(-10))
    }

    @Test
    fun statsCountDoneLessonsAndStars() {
        val progress = mapOf(
            "a" to LessonProgress(stars = 3),
            "b" to LessonProgress(stars = 2),
            "c" to LessonProgress(stars = 0, attempts = 1), // neuspešen poskus ne šteje
        )
        assertEquals(ProfileStats(lessonsDone = 2, stars = 5), profileStats(progress))
    }

    @Test
    fun noProgressIsZero() {
        assertEquals(ProfileStats(0, 0), profileStats(emptyMap()))
    }
}
