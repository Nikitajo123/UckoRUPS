package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathGamesTest {

    // ---- Deljivostni stroj ----

    @Test
    fun divisorsFollowTheRules() {
        assertEquals(setOf(2, 3, 5, 10), divisorsOf(120))
        assertEquals(setOf(3, 5, 9), divisorsOf(135))
        assertEquals(setOf(2, 3, 5, 9, 10), divisorsOf(3600))
        assertEquals(emptySet<Int>(), divisorsOf(457))
    }

    @Test
    fun bigNumbersAreWrittenWithSpaces() {
        assertEquals("1 000 000", formatNumber(1_000_000))
        assertEquals("4 518", formatNumber(4518))
        assertEquals("74", formatNumber(74))
    }

    @Test
    fun divisibilityNumbersOutsideRangeAreSkipped() {
        assertEquals(listOf(5, 1_000_000), parseDivisibilityNumbers(mapOf("numbers" to "5|0|abc|1000000|1000001")))
    }

    // ---- Lovilec kotov ----

    @Test
    fun angleKinds() {
        assertEquals(AngleKind.OSTRI, angleKind(89))
        assertEquals(AngleKind.PRAVI, angleKind(90))
        assertEquals(AngleKind.TOPI, angleKind(91))
        assertEquals(AngleKind.TOPI, angleKind(179))
        assertEquals(AngleKind.IZTEGNJENI, angleKind(180))
    }

    @Test
    fun anglesOutsideRangeAreSkipped() {
        assertEquals(listOf(35, 180), parseAngles(mapOf("angles" to "35|0|181|180")))
    }

    // ---- Gradbenik ----

    @Test
    fun rectangleQuestionHasRightAnswerAndFourDifferentOptions() {
        repeat(20) { seed ->
            val perimeter = rectQuestion(5, 3, askArea = false, random = Random(seed))
            assertEquals(16, perimeter.answer)
            assertEquals(4, perimeter.options.toSet().size)
            assertTrue(perimeter.answer in perimeter.options)
            assertTrue(15 in perimeter.options) // the area is the typical mistake

            val area = rectQuestion(5, 3, askArea = true, random = Random(seed))
            assertEquals(15, area.answer)
            assertEquals(4, area.options.toSet().size)
        }
    }

    @Test
    fun parsesRectangles() {
        assertEquals(listOf(5 to 3, 10 to 1), parseRects(mapOf("rects" to "5x3|11x2|10×1|x")))
    }
}
