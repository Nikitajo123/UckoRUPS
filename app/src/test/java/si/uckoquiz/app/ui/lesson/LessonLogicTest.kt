package si.uckoquiz.app.ui.lesson

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import si.uckoquiz.app.domain.Explanation
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.Task

class LessonLogicTest {
    private val task = Task(id = "t1", type = "choice", answers = listOf("3/8", "8/3", "5/8"), correct = listOf(0))

    @Test
    fun rightAnswerIsCorrect() {
        assertTrue(isChoiceCorrect(task, setOf(0)))
    }

    @Test
    fun wrongOrEmptyAnswerIsNotCorrect() {
        assertFalse(isChoiceCorrect(task, setOf(1)))
        assertFalse(isChoiceCorrect(task, emptySet()))
        assertFalse(isChoiceCorrect(task, setOf(0, 1)))
    }

    @Test
    fun correctAnswerTextForWrongPanel() {
        assertEquals("3/8", correctAnswerText(task))
    }

    @Test
    fun explanationStepsComeBeforeTasks() {
        val lesson = Lesson(
            explanation = Explanation(title = "Ulomek", steps = listOf("a", "b")),
            tasks = listOf(task, task.copy(id = "t2")),
        )
        val pages = lessonPages(lesson)
        assertEquals(4, pages.size)
        assertTrue(pages[0] is LessonPage.Explanation)
        assertTrue(pages[1] is LessonPage.Explanation)
        assertEquals("t1", (pages[2] as LessonPage.TaskPage).task.id)
    }

    @Test
    fun lessonWithoutHeartsIsNotPassed() {
        assertFalse(LessonOutcome(correct = 1, total = 5, mistakes = emptyList(), seconds = 30, heartsLeft = 0).passed)
        assertTrue(LessonOutcome(correct = 4, total = 5, mistakes = emptyList(), seconds = 30, heartsLeft = 4).passed)
    }
}
