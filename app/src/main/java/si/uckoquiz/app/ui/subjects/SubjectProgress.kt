package si.uckoquiz.app.ui.subjects

import si.uckoquiz.app.domain.Subject
import si.uckoquiz.app.ui.home.NodeState
import si.uckoquiz.app.ui.home.PathSection

/** Povzetek napredka pri enem predmetu za kartico na zaslonu Predmeti. */
data class SubjectSummary(
    val subject: Subject,
    val doneLessons: Int,
    val totalLessons: Int,
    val currentChapter: Int,
    val chapterCount: Int,
) {
    /** Delež opravljenih lekcij (vključno z mini igrami in boss levelom) v odstotkih. */
    val percent: Int get() = if (totalLessons == 0) 0 else doneLessons * 100 / totalLessons
}

/**
 * Iz učne poti predmeta izračuna, koliko je opravljenega in v katerem poglavju je učenec.
 * Trenutno poglavje je tisto z lekcijo "na vrsti"; če takšne ni, zadnje poglavje z opravljeno lekcijo.
 */
fun summarize(subject: Subject, sections: List<PathSection>): SubjectSummary {
    val nodes = sections.flatMap { it.nodes }
    val currentIndex = sections.indexOfFirst { section -> section.nodes.any { it.state == NodeState.CURRENT } }
    val lastDoneIndex = sections.indexOfLast { section -> section.nodes.any { it.state == NodeState.DONE } }
    val currentChapter = when {
        sections.isEmpty() -> 0
        currentIndex >= 0 -> currentIndex + 1
        lastDoneIndex >= 0 -> lastDoneIndex + 1
        else -> 1
    }
    return SubjectSummary(
        subject = subject,
        doneLessons = nodes.count { it.state == NodeState.DONE },
        totalLessons = nodes.size,
        currentChapter = currentChapter,
        chapterCount = sections.size,
    )
}
