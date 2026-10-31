package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MathGamesTest {

    // ---- 2048 z decimalkami (tiles in eighths: 1 = 0,125 ... 8 = 1,0) ----

    @Test
    fun slideMergesEqualTilesOnce() {
        val (line, merged) = slideLine(listOf(2, 2, 2, 2))
        assertEquals(listOf(4, 4, 0, 0), line)
        assertEquals(listOf(4, 4), merged)
    }

    @Test
    fun slideMovesTilesWithoutMerging() {
        val (line, merged) = slideLine(listOf(0, 1, 0, 2))
        assertEquals(listOf(1, 2, 0, 0), line)
        assertTrue(merged.isEmpty())
    }

    @Test
    fun twoHalvesMakeAWhole() {
        val board = listOf(
            4, 4, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
            0, 0, 0, 0,
        )
        val move = moveBoard(board, Direction.Right)
        assertEquals(WHOLE, move.board[3])
        assertEquals(listOf(WHOLE), move.merged)
        assertTrue(move.moved)
    }

    @Test
    fun moveUpUsesColumns() {
        val board = listOf(
            0, 0, 0, 0,
            1, 0, 0, 0,
            0, 0, 0, 0,
            1, 0, 0, 0,
        )
        assertEquals(2, moveBoard(board, Direction.Up).board[0])
    }

    @Test
    fun fullBoardWithoutPairsHasNoMoves() {
        val board = listOf(
            1, 2, 1, 2,
            2, 1, 2, 1,
            1, 2, 1, 2,
            2, 1, 2, 1,
        )
        assertFalse(hasMoves(board))
    }

    @Test
    fun tilesShowAsDecimals() {
        assertEquals("0,125", eighths(1))
        assertEquals("0,25", eighths(2))
        assertEquals("0,5", eighths(4))
        assertEquals("1,0", eighths(8))
    }

    // ---- Ulomki s kockami ----

    @Test
    fun parsesSeveralFractions() {
        assertEquals(listOf(3 to 8, 2 to 4), parseFractions(mapOf("fractions" to "3/8| 2/4")))
    }

    @Test
    fun parsesSingleFractionFromNumeratorAndDenominator() {
        assertEquals(listOf(3 to 8), parseFractions(mapOf("numerator" to "3", "denominator" to "8")))
    }

    @Test
    fun skipsBadFractions() {
        assertEquals(listOf(1 to 2), parseFractions(mapOf("fractions" to "1/2|9/4|x/3|5/20")))
    }
}
