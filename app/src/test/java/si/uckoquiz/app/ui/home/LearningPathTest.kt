package si.uckoquiz.app.ui.home

import org.junit.Assert.assertEquals
import org.junit.Test
import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.LessonProgress
import si.uckoquiz.app.domain.Task
import si.uckoquiz.app.ui.home.NodeState.CURRENT
import si.uckoquiz.app.ui.home.NodeState.DONE
import si.uckoquiz.app.ui.home.NodeState.LOCKED

class LearningPathTest {
    private val chapters = listOf(Chapter(id = "c2", order = 2), Chapter(id = "c1", order = 1))
    private val lessons = mapOf(
        "c1" to listOf(
            Lesson(id = "b", order = 2, tasks = listOf(Task(type = "game"))),
            Lesson(id = "a", order = 1),
        ),
        "c2" to listOf(Lesson(id = "c", order = 1, boss = true)),
    )

    private fun states(stars: Map<String, Int>): List<NodeState> =
        buildPath(chapters, lessons, stars.mapValues { LessonProgress(stars = it.value) })
            .flatMap { section -> section.nodes.map { it.state } }

    @Test
    fun onlyFirstLessonIsOpenWithoutProgress() {
        assertEquals(listOf(CURRENT, LOCKED, LOCKED), states(emptyMap()))
    }

    @Test
    fun oneStarUnlocksNextLesson() {
        assertEquals(listOf(DONE, CURRENT, LOCKED), states(mapOf("a" to 1)))
    }

    @Test
    fun nextChapterNeedsTwoStars() {
        assertEquals(listOf(DONE, DONE, LOCKED), states(mapOf("a" to 3, "b" to 1)))
        assertEquals(listOf(DONE, DONE, CURRENT), states(mapOf("a" to 3, "b" to 2)))
    }

    @Test
    fun kindsAndOrderAreCorrect() {
        val nodes = buildPath(chapters, lessons, emptyMap()).flatMap { it.nodes }
        assertEquals(listOf("a", "b", "c"), nodes.map { it.lessonId })
        assertEquals(listOf(NodeKind.LESSON, NodeKind.GAME, NodeKind.BOSS), nodes.map { it.kind })
    }
}
