package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LabRoundsTest {

    @Test
    fun parsesRoundsWithSeveralSteps() {
        val rounds = parseLabRounds(
            mapOf(
                "mixtures" to "Pesek + voda|Sol + pesek",
                "steps" to "filtriranje|raztapljanje v vodi, filtriranje, izhlapevanje",
                "why" to "Pesek ostane na filtru.|",
            ),
        )
        assertEquals(2, rounds.size)
        assertEquals(listOf("filtriranje"), rounds[0].steps)
        assertEquals("Pesek ostane na filtru.", rounds[0].why)
        assertEquals(listOf("raztapljanje v vodi", "filtriranje", "izhlapevanje"), rounds[1].steps)
        assertEquals("", rounds[1].why)
    }

    @Test
    fun skipsRoundsWithoutSteps() {
        val rounds = parseLabRounds(mapOf("mixtures" to "Pesek + voda|Sol + voda", "steps" to "filtriranje"))
        assertEquals(1, rounds.size)
    }

    @Test
    fun emptyConfigGivesNoRounds() {
        assertTrue(parseLabRounds(emptyMap()).isEmpty())
    }
}
