package si.uckoquiz.app.games

import org.junit.Assert.assertEquals
import org.junit.Test

class SentenceBuilderTest {

    @Test
    fun splitsSentenceIntoWordsWithoutFullStop() {
        assertEquals(listOf("My", "sister", "is", "twelve", "years", "old"), splitWords("My sister is twelve years old."))
    }

    @Test
    fun removesQuestionMarkAndExtraSpaces() {
        assertEquals(listOf("What", "is", "your", "name"), splitWords("  What  is your name? "))
    }

    @Test
    fun keepsRepeatedWords() {
        assertEquals(listOf("This", "is", "is", "it"), splitWords("This is is it!"))
    }
}
