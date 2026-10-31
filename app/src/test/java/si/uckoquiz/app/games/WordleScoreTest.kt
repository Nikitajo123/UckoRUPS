package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Test
import si.uckoquiz.app.games.LetterState.Absent
import si.uckoquiz.app.games.LetterState.Correct
import si.uckoquiz.app.games.LetterState.Present

class WordleScoreTest {

    @Test
    fun allCorrect() {
        assertEquals(List(5) { Correct }, scoreGuess("RULER", "RULER"))
    }

    @Test
    fun correctAndAbsent() {
        assertEquals(listOf(Absent, Absent, Absent, Correct, Correct), scoreGuess("PAPER", "RULER"))
    }

    @Test
    fun presentLetters() {
        // R and E are in RULER, but not in these places
        assertEquals(listOf(Present, Present, Absent, Absent, Absent), scoreGuess("ERASM", "RULER"))
    }

    @Test
    fun doubleLetterOnlyCountsOnce() {
        // PAPER has one A, already matched in place 2, so the second A is grey
        assertEquals(listOf(Absent, Correct, Absent, Absent, Absent), scoreGuess("BANAL", "PAPER"))
    }
}
