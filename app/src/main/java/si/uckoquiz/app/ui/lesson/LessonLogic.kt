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

/** Učenčev odgovor na trenutno nalogo (odvisno od vrste naloge). */
sealed interface TaskAnswer {
    /** Še ni odgovora. */
    data object None : TaskAnswer

    /** Izbira odgovora: indeksi izbranih odgovorov v task.answers. */
    data class Choice(val selected: Set<Int>) : TaskAnswer

    /** Povezovanje: indeks levega pojma -> indeks para, katerega desni pojem je izbran. */
    data class Match(val pairs: Map<Int, Int>) : TaskAnswer

    /** Vleci in spusti: indeks predmeta v task.items -> izbrana škatla (kategorija). */
    data class Drag(val placed: Map<Int, String>) : TaskAnswer
}

/** Ali lahko učenec pritisne PREVERI: izbran je odgovor, povezani so vsi pari, razvrščeni so vsi predmeti. */
fun isAnswerComplete(task: Task, answer: TaskAnswer): Boolean = when (answer) {
    is TaskAnswer.Choice -> answer.selected.isNotEmpty()
    is TaskAnswer.Match -> task.pairs.isNotEmpty() && answer.pairs.size == task.pairs.size
    is TaskAnswer.Drag -> task.items.isNotEmpty() && answer.placed.size == task.items.size
    TaskAnswer.None -> false
}

/** Ali je odgovor pravilen. */
fun isAnswerCorrect(task: Task, answer: TaskAnswer): Boolean = when (answer) {
    is TaskAnswer.Choice -> isChoiceCorrect(task, answer.selected)
    is TaskAnswer.Match -> isAnswerComplete(task, answer) && answer.pairs.all { (left, right) -> left == right }
    is TaskAnswer.Drag -> isAnswerComplete(task, answer) &&
            answer.placed.all { (item, category) -> task.items.getOrNull(item)?.category == category }
    TaskAnswer.None -> false
}

/**
 * Vrstni red desnega stolpca pri povezovanju: premešan, a vedno enak za isto nalogo,
 * in nikoli enak levemu stolpcu (da ne e naloga prelesna)
 */
fun matchRightOrder(task: Task): List<Int> {
    val n = task.pairs.size
    if (n < 2) return List(n) { it }
    val shuffled = (0 until n).shuffled(kotlin.random.Random(task.id.hashCode()))
    return if (shuffled == (0 until n).toList()) shuffled.drop(1) + shuffled.first() else shuffled
}

/** Besedilo pravilnega odgovora za rdečo ploščo (npr. "3/8" ali "1/2 = polovica, 1/3 = tretjina"). */
fun correctAnswerText(task: Task): String = when (task.type) {
    "match" -> task.pairs.joinToString(", ") { "${it.left} = ${it.right}" }
    "drag" -> task.categories.joinToString("; ") { category ->
        "$category: " + task.items.filter { it.category == category }.joinToString(", ") { it.text }
    }
    else -> task.correct.mapNotNull { task.answers.getOrNull(it) }.joinToString(", ")
}

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
