package si.uckoquiz.app.ui.home

import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.LessonProgress

enum class NodeKind { LESSON, GAME, BOSS }

enum class NodeState { LOCKED, CURRENT, DONE }

data class PathNode(
    val lessonId: String,
    val title: String,
    val kind: NodeKind,
    val state: NodeState,
    val stars: Int,
)

// Eno poglavje na poti- naslov poglavja in njegovi krogci
data class PathSection(val chapter: Chapter, val nodes: List<PathNode>)

// za prvata lekcija od novoto poglavje mora posledniot krogec od prethodnoto poglavje da ima vsaj 2 zvezdici
const val STARS_TO_UNLOCK_CHAPTER = 2

fun Lesson.isGame(): Boolean = tasks.isNotEmpty() && tasks.all { it.type == "game" }

/**
 * Iz poglavij, lekcij in napredka sestavi učno pot.
 * Pravila: prva lekcija je odklenjena; naslednja se odklene, ko ima prejšnja vsaj 1 zvezdico;
 * prva lekcija novega poglavja se odklene, ko ima zadnja lekcija (mini igra) prejšnjega poglavja vsaj 2 zvezdici.
 */
fun buildPath(
    chapters: List<Chapter>,
    lessonsByChapter: Map<String, List<Lesson>>,
    progress: Map<String, LessonProgress>,
): List<PathSection> {
    var previousStars: Int? = null // null = se ni bilo nobene lekcije
    return chapters.sortedBy { it.order }.map { chapter ->
        val lessons = lessonsByChapter[chapter.id].orEmpty().sortedBy { it.order }
        val nodes = lessons.mapIndexed { index, lesson ->
            val stars = progress[lesson.id]?.stars ?: 0
            val needed = if (index == 0) STARS_TO_UNLOCK_CHAPTER else 1
            val before = previousStars
            val unlocked = before == null || before >= needed
            previousStars = stars
            PathNode(
                lessonId = lesson.id,
                title = lesson.title,
                kind = when {
                    lesson.boss -> NodeKind.BOSS
                    lesson.isGame() -> NodeKind.GAME
                    else -> NodeKind.LESSON
                },
                state = when {
                    stars > 0 -> NodeState.DONE
                    unlocked -> NodeState.CURRENT
                    else -> NodeState.LOCKED
                },
                stars = stars.coerceIn(0, 3),
            )
        }
        PathSection(chapter, nodes)
    }
}
