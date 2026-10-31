package si.uckoquiz.app.games

import org.junit.Assert.assertTrue
import org.junit.Test

class FloatOrSinkTest {

    @Test
    fun defaultObjectsUseOnlyTheTwoCategories() {
        assertTrue(DEFAULT_OBJECTS.all { it.category == FLOATS || it.category == SINKS })
    }

    @Test
    fun defaultObjectsHaveBothKinds() {
        assertTrue(DEFAULT_OBJECTS.any { it.category == FLOATS })
        assertTrue(DEFAULT_OBJECTS.any { it.category == SINKS })
    }

    @Test
    fun reasonTalksAboutDensity() {
        assertTrue(defaultReason(floats = true).contains("manjšo gostoto"))
        assertTrue(defaultReason(floats = false).contains("večjo gostoto"))
    }
}
