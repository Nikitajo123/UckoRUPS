package si.uckoquiz.app.ui.lesson

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import si.uckoquiz.app.domain.Explanation
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.Task

class LessonEngineTest {
    private fun choice(id: String) = Task(id = id, type = "choice", answers = listOf("a", "b"), correct = listOf(0), explanation = "razlaga")
    private val right = TaskAnswer.Choice(setOf(0))
    private val wrong = TaskAnswer.Choice(setOf(1))

    private val lesson = Lesson(
        id = "l1",
        explanation = Explanation(title = "Naslov", steps = listOf("korak 1")),
        tasks = listOf(choice("t1"), choice("t2")),
    )

    /** Odgovori na trenutno nalogo, pritisne PREVERI in NAPREJ. */
    private fun LessonState.solve(answer: TaskAnswer): LessonState =
        LessonEngine.next(LessonEngine.check(LessonEngine.answer(this, answer)), now = 60_000)

    private fun started() = LessonEngine.next(LessonEngine.start(lesson, now = 0), now = 0) // preskoči razlago

    @Test
    fun explanationComesFirstThenTasksInOrder() {
        val s = LessonEngine.start(lesson, now = 0)
        assertTrue(s.page is LessonPage.Explanation)
        assertEquals("t1", (started().page as LessonPage.TaskPage).task.id)
    }

    @Test
    fun checkIsDisabledWithoutAnswer() {
        val s = started()
        assertFalse(s.canCheck)
        assertNull(LessonEngine.check(s).feedback)
    }

    @Test
    fun allCorrectGivesThreeStarsAndXp() {
        val end = started().solve(right).solve(right)
        val outcome = end.outcome
        assertNotNull(outcome)
        assertEquals(3, outcome!!.stars)
        assertEquals(10 + 2 * 2, outcome.xp)
        assertEquals(60, outcome.seconds)
        assertTrue(outcome.unlocksNextChapter)
    }

    @Test
    fun wrongAnswerCostsHeartAndRepeatsTaskAtTheEnd() {
        val afterWrong = LessonEngine.check(LessonEngine.answer(started(), wrong))
        assertEquals(LESSON_HEARTS - 1, afterWrong.hearts)
        assertEquals(listOf("t1"), afterWrong.mistakes)
        val last = afterWrong.pages.last() as LessonPage.TaskPage
        assertEquals("t1", last.task.id)
        assertTrue(last.retry)
    }

    @Test
    fun retryDoesNotChangeTheScore() {
        // t1 napačno, t2 pravilno, ponovitev t1 pravilno: prvi poskusi 1 od 2 = 50 % -> 1 zvezdica
        val end = started().solve(wrong).solve(right).solve(right)
        val outcome = end.outcome!!
        assertEquals(1, outcome.correct)
        assertEquals(2, outcome.total)
        assertEquals(1, outcome.stars)
        assertFalse(outcome.unlocksNextChapter)
    }

    @Test
    fun noHeartsEndsTheLessonWithoutStars() {
        val long = lesson.copy(tasks = (1..6).map { choice("t$it") })
        var s = LessonEngine.next(LessonEngine.start(long, now = 0), now = 0)
        repeat(LESSON_HEARTS) { s = LessonEngine.check(LessonEngine.answer(s, wrong)).let { LessonEngine.next(it, now = 0) } }
        assertNotNull(s.outcome)
        assertEquals(0, s.outcome!!.heartsLeft)
        assertEquals(0, s.outcome!!.stars)
        assertEquals(0, s.outcome!!.xp)
    }

    @Test
    fun bossHasThreeHeartsNoExplanationAndNoHints() {
        val boss = LessonEngine.start(lesson.copy(boss = true), now = 0)
        assertEquals(BOSS_HEARTS, boss.hearts)
        assertTrue(boss.page is LessonPage.TaskPage)
        val afterWrong = LessonEngine.check(LessonEngine.answer(boss, wrong))
        assertEquals("", afterWrong.feedback!!.text)
        assertEquals(2, afterWrong.pages.size) // boss: brez ponovitev
    }

    @Test
    fun starRules() {
        assertEquals(3, LessonEngine.lessonStars(9, 10, passed = true))
        assertEquals(2, LessonEngine.lessonStars(6, 10, passed = true))
        assertEquals(1, LessonEngine.lessonStars(5, 10, passed = true))
        assertEquals(0, LessonEngine.lessonStars(0, 10, passed = true))
        assertEquals(3, LessonEngine.lessonStars(0, 0, passed = true))
        assertEquals(0, LessonEngine.lessonStars(10, 10, passed = false))
    }
}
