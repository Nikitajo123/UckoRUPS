package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameSessionTest {

    @Test
    fun correctAnswersAddPointsAndCount() {
        val s = GameSession(total = 5)
        s.onCorrect()
        s.onCorrect()
        assertEquals(2, s.correct)
        assertEquals(2, s.answered)
        assertEquals(20, s.score)
        assertFalse(s.isOver)
    }

    @Test
    fun comboDoublesPointsFromThirdInARow() {
        val s = GameSession(total = 10)
        repeat(3) { s.onCorrect() } // 10 + 10 + 20
        assertEquals(40, s.score)
        assertEquals(2, s.comboMultiplier())
    }

    @Test
    fun wrongAnswerCostsALifeAndResetsCombo() {
        val s = GameSession(total = 10, maxLives = 3)
        repeat(3) { s.onCorrect() }
        s.onWrong()
        assertEquals(2, s.lives)
        assertEquals(0, s.combo)
        assertEquals(1, s.comboMultiplier())
    }

    @Test
    fun gameEndsWhenLivesRunOut() {
        val s = GameSession(total = 10, maxLives = 2)
        s.onWrong()
        s.onWrong()
        assertTrue(s.isOver)
        s.onCorrect() // ignored after the end
        assertEquals(0, s.correct)
    }

    @Test
    fun gameEndsWhenAllAnswered() {
        val s = GameSession(total = 2)
        s.onCorrect()
        s.onWrong()
        assertTrue(s.isOver)
        val r = s.result()
        assertEquals(1, r.correct)
        assertEquals(2, r.total)
    }

    @Test
    fun endNowFinishesTheRound() {
        val s = GameSession(total = 5)
        s.endNow()
        assertTrue(s.isOver)
    }

    @Test
    fun starsFollowShareOfCorrectAnswers() {
        assertEquals(3, starsFor(GameResult(correct = 9, total = 10, seconds = 0)))
        assertEquals(2, starsFor(GameResult(correct = 6, total = 10, seconds = 0)))
        assertEquals(1, starsFor(GameResult(correct = 1, total = 10, seconds = 0)))
        assertEquals(0, starsFor(GameResult(correct = 0, total = 10, seconds = 0)))
        assertEquals(0, starsFor(GameResult(correct = 0, total = 0, seconds = 0)))
    }

    @Test
    fun timeIsShownAsMinutesAndSeconds() {
        assertEquals("1:05", formatTime(65))
        assertEquals("0:09", formatTime(9))
    }
}
