package si.uckoquiz.app.ui.lesson

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import si.uckoquiz.app.domain.DragItem
import si.uckoquiz.app.domain.Explanation
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.MatchPair
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

    // povezovanje in vlečenje (UQ-32)

    private val match = Task(
        id = "m1", type = "match",
        pairs = listOf(MatchPair("1/2", "polovica"), MatchPair("1/3", "tretjina"), MatchPair("1/4", "četrtina")),
    )
    private val drag = Task(
        id = "d1", type = "drag", categories = listOf("Manj", "Več"),
        items = listOf(DragItem("1/4", "Manj"), DragItem("3/4", "Več")),
    )

    @Test
    fun matchNeedsAllPairsAndRightPartners() {
        assertFalse(isAnswerComplete(match, TaskAnswer.Match(mapOf(0 to 0, 1 to 1))))
        assertTrue(isAnswerCorrect(match, TaskAnswer.Match(mapOf(0 to 0, 1 to 1, 2 to 2))))
        assertFalse(isAnswerCorrect(match, TaskAnswer.Match(mapOf(0 to 1, 1 to 0, 2 to 2))))
    }

    @Test
    fun rightColumnIsShuffledButStable() {
        val order = matchRightOrder(match)
        assertEquals(listOf(0, 1, 2), order.sorted())
        assertTrue(order != listOf(0, 1, 2))
        assertEquals(order, matchRightOrder(match))
    }

    @Test
    fun dragNeedsEveryItemInItsBox() {
        assertFalse(isAnswerComplete(drag, TaskAnswer.Drag(mapOf(0 to "Manj"))))
        assertTrue(isAnswerCorrect(drag, TaskAnswer.Drag(mapOf(0 to "Manj", 1 to "Več"))))
        assertFalse(isAnswerCorrect(drag, TaskAnswer.Drag(mapOf(0 to "Več", 1 to "Več"))))
        assertEquals("Manj: 1/4; Več: 3/4", correctAnswerText(drag))
    }

    @Test
    fun noAnswerIsNeverComplete() {
        assertFalse(isAnswerComplete(task, TaskAnswer.None))
        assertFalse(isAnswerCorrect(task, TaskAnswer.None))
    }
}
