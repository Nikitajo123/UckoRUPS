package si.uckoquiz.app.ui.lesson

import org.junit.Assert.assertEquals
import org.junit.Test
import si.uckoquiz.app.domain.Task

class ListenTextTest {
    @Test
    fun usesAudioTextWhenGiven() {
        val task = Task(type = "listen", audioText = "my brother", answers = listOf("my sister", "my brother"), correct = listOf(1))
        assertEquals("my brother", listenText(task))
    }

    @Test
    fun fallsBackToCorrectAnswer() {
        val task = Task(type = "listen", answers = listOf("Good night", "Good morning"), correct = listOf(1))
        assertEquals("Good morning", listenText(task))
    }
}
