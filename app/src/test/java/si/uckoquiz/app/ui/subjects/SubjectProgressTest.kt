package si.uckoquiz.app.ui.subjects

import org.junit.Assert.assertEquals
import org.junit.Test
import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.LessonProgress
import si.uckoquiz.app.domain.Subject
import si.uckoquiz.app.ui.home.buildPath

class SubjectProgressTest {
    private val subject = Subject(id = "mat", name = "Matematika")
    private val chapters = listOf(Chapter(id = "c1", order = 1), Chapter(id = "c2", order = 2))
    private val lessons = mapOf(
        "c1" to listOf(Lesson(id = "a", order = 1), Lesson(id = "b", order = 2)),
        "c2" to listOf(Lesson(id = "c", order = 1), Lesson(id = "d", order = 2)),
    )

    private fun summary(stars: Map<String, Int>) =
        summarize(subject, buildPath(chapters, lessons, stars.mapValues { LessonProgress(stars = it.value) }))

    @Test
    fun noProgressIsZeroPercentInFirstChapter() {
        val s = summary(emptyMap())
        assertEquals(0, s.percent)
        assertEquals(1, s.currentChapter)
        assertEquals(2, s.chapterCount)
    }

    @Test
    fun percentCountsDoneLessons() {
        assertEquals(25, summary(mapOf("a" to 3)).percent)
        assertEquals(50, summary(mapOf("a" to 3, "b" to 2)).percent)
    }

    @Test
    fun currentChapterMovesWhenNextChapterUnlocks() {
        assertEquals(2, summary(mapOf("a" to 3, "b" to 2)).currentChapter)
        // samo 1 zvezdica na koncu poglavja: naslednje poglavje je še zaklenjeno
        assertEquals(1, summary(mapOf("a" to 3, "b" to 1)).currentChapter)
    }

    @Test
    fun everythingDoneIsHundredPercent() {
        val s = summary(mapOf("a" to 3, "b" to 3, "c" to 3, "d" to 3))
        assertEquals(100, s.percent)
        assertEquals(2, s.currentChapter)
    }
}
