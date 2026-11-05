package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BossFightTest {
    private val config = mapOf(
        "questions" to "Q1|Q2|Q3",
        "options" to "a;b;c|x;y|bad",
        "correct" to "1|0|0",
        "why" to "w1|w2|w3",
    )

    @Test
    fun readsQuestionsAndSkipsBrokenOnes() {
        val rounds = parseBossRounds(config)
        assertEquals(2, rounds.size) // Q3 has only one option
        assertEquals(BossRound("Q1", listOf("a", "b", "c"), 1, "w1"), rounds[0])
        assertEquals("x", rounds[1].options[rounds[1].correct])
    }

    @Test
    fun shufflingKeepsTheRightAnswer() {
        val round = BossRound("Q", listOf("a", "b", "c", "d"), 2)
        repeat(20) { seed ->
            val s = round.shuffled(Random(seed))
            assertEquals("c", s.options[s.correct])
            assertEquals(round.options.toSet(), s.options.toSet())
        }
    }

    @Test
    fun bossNeedsSixtyPercentToFall() {
        assertEquals(8, bossHitsToWin(12))
        assertEquals(3, bossHitsToWin(5))
        assertTrue(bossHitsToWin(1) >= 1)
    }
}
