package si.uckoquiz.app.ui.lesson

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import si.uckoquiz.app.ui.sample.SampleContent

/** Boss level matematike iz vzorčne vsebine: 3 srčki, brez razlage, brez namigov, brez ponovitev. */
class BossLevelTest {
    private val boss = SampleContent.lessons.first { it.id == "mat_decimalna_boss" }

    @Test
    fun bossLessonHasTasksAndIsMarked() {
        assertTrue(boss.boss)
        assertTrue(boss.tasks.size >= 5)
    }

    @Test
    fun bossStartsWithThreeLivesEvenWithFullProfileHearts() {
        val s = LessonEngine.start(boss, now = 0, hearts = LESSON_HEARTS)
        assertEquals(BOSS_HEARTS, s.hearts)
        assertTrue(s.page is LessonPage.TaskPage) // brez razlage
    }

    @Test
    fun wrongAnswerInBossGivesNoHintAndNoRetry() {
        val start = LessonEngine.start(boss, now = 0)
        val wrong = LessonEngine.check(LessonEngine.answer(start, TaskAnswer.Choice(setOf(1))))
        assertEquals("", wrong.feedback!!.text)
        assertEquals(start.pages.size, wrong.pages.size)
        assertEquals(BOSS_HEARTS - 1, wrong.hearts)
    }

    @Test
    fun threeMistakesEndTheBoss() {
        var s = LessonEngine.start(boss, now = 0)
        repeat(BOSS_HEARTS) {
            val task = (s.page as LessonPage.TaskPage).task
            val wrongAnswer: TaskAnswer = when (task.type) {
                "match" -> TaskAnswer.Match(task.pairs.indices.associateWith { (it + 1) % task.pairs.size })
                "drag" -> TaskAnswer.Drag(task.items.indices.associateWith { i -> task.categories.first { c -> c != task.items[i].category } })
                else -> TaskAnswer.Choice(setOf(task.answers.indices.first { it !in task.correct }))
            }
            s = LessonEngine.next(LessonEngine.check(LessonEngine.answer(s, wrongAnswer)), now = 0)
        }
        assertEquals(0, s.outcome!!.heartsLeft)
        assertEquals(0, s.outcome!!.stars)
    }
}
