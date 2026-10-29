package si.uckoquiz.app.ui.lesson

import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.Task

/** Ena stran lekcije: korak Učkove razlage ali naloga. */
sealed interface LessonPage {
    data class Explanation(val title: String, val step: String, val index: Int, val count: Int) : LessonPage
    data class TaskPage(val task: Task, val number: Int, val count: Int) : LessonPage
}

/** Lekcija se začne z razlago (vsak korak svoja stran), sledijo naloge. */
fun lessonPages(lesson: Lesson): List<LessonPage> {
    val steps = lesson.explanation.steps
    val explanation = steps.mapIndexed { i, step ->
        LessonPage.Explanation(lesson.explanation.title, step, i, steps.size)
    }
    val tasks = lesson.tasks.mapIndexed { i, task -> LessonPage.TaskPage(task, i + 1, lesson.tasks.size) }
    return explanation + tasks
}

/** Odgovor je pravilen, če je izbranih natanko toliko in točno tisti odgovori, kot jih določa task.correct. */
fun isChoiceCorrect(task: Task, selected: Set<Int>): Boolean =
    selected.isNotEmpty() && selected == task.correct.toSet()

/** Besedilo pravilnega odgovora za rdečo ploščo (npr. "3/8"). */
fun correctAnswerText(task: Task): String =
    task.correct.mapNotNull { task.answers.getOrNull(it) }.joinToString(", ")

/** Napis nad nalogo glede na vrsto naloge (Figma 08 do 11). */
fun taskLabel(task: Task): String = when (task.type) {
    "choice" -> "Izberi pravilni odgovor"
    "listen" -> "Poslušaj in izberi"
    "match" -> "Poveži pare"
    "drag" -> "Povleci v pravo škatlo"
    "game" -> "Mini igra"
    else -> "Naloga"
}

/** Izid lekcije, ki gre v shranjevanje napredka in na zaslon rezultata (UQ-35). */
data class LessonOutcome(
    val correct: Int,
    val total: Int,
    val mistakes: List<String>,
    val seconds: Int,
    val heartsLeft: Int,
) {
    val passed: Boolean get() = heartsLeft > 0
}
