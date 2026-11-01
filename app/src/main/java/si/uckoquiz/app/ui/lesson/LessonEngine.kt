package si.uckoquiz.app.ui.lesson

import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.Task
import kotlin.math.max

/** Srčki na začetku navadne lekcije in boss levela (načrt: boss ima 3 življenja). */
const val LESSON_HEARTS = 5
const val BOSS_HEARTS = 3

/** Vrste nalog, ki se preverijo z gumbom PREVERI (igra ima svoj konec). */
val CHECKABLE_TYPES = setOf("choice", "listen", "match", "drag")

/** Kaj pokaže spodnja plošča po preverjanju. */
data class LessonFeedback(val correct: Boolean, val title: String, val text: String)

/** Celotno stanje lekcije v enem nespremenljivem objektu (lažje testiranje, preživi obračanje zaslona v ViewModelu). */
data class LessonState(
    val pages: List<LessonPage>,
    val hearts: Int,
    val boss: Boolean = false,
    val startMillis: Long = 0,
    val index: Int = 0,
    val correct: Int = 0,
    val scored: Int = 0,
    val mistakes: List<String> = emptyList(),
    val answer: TaskAnswer = TaskAnswer.None,
    val feedback: LessonFeedback? = null,
    val outcome: LessonOutcome? = null,
) {
    val page: LessonPage? get() = pages.getOrNull(index)

    /** Vrstica napredka */
    val progress: Float
        get() = if (pages.isEmpty()) 0f else (index + if (feedback != null) 1 else 0) / pages.size.toFloat()

    /** PREVERI je omogočen, ko je odgovor popoln in še ni bil preverjen. */
    val canCheck: Boolean
        get() {
            val taskPage = page as? LessonPage.TaskPage ?: return false
            return feedback == null && taskPage.task.type in CHECKABLE_TYPES && isAnswerComplete(taskPage.task, answer)
        }
}

/**
 * Pravila lekcije (UQ-34), brez Androida, zato jih lahko testiramo:
 * - vrstni red: najprej razlaga, nato naloge po vrsti; napačno rešena naloga se enkrat ponovi na koncu
 * - srčki: napaka vzame srček; brez srčkov je lekcije konec (0 zvezdic)
 * - zvezdice: šteje samo prvi poskus vsake naloge (90 % = 3, 60 % = 2, vsaj en pravilen = 1)
 * - odklep: z vsaj 2 zvezdicama se odklene naslednje poglavje (LessonOutcome.unlocksNextChapter)
 * - boss level: 3 srčki, brez razlage, brez namigov in brez ponovitev
 */
object LessonEngine {

    fun start(lesson: Lesson, now: Long): LessonState = LessonState(
        pages = lessonPages(lesson),
        hearts = if (lesson.boss) BOSS_HEARTS else LESSON_HEARTS,
        boss = lesson.boss,
        startMillis = now,
    )

    /** Učenec spremeni odgovor (po preverjanju ni več mogoče). */
    fun answer(state: LessonState, answer: TaskAnswer): LessonState =
        if (state.feedback != null || state.outcome != null) state else state.copy(answer = answer)

    /** Gumb PREVERI. */
    fun check(state: LessonState): LessonState {
        if (!state.canCheck) return state
        val page = state.page as LessonPage.TaskPage
        val ok = isAnswerCorrect(page.task, state.answer)
        return score(state, page, ok, feedbackFor(page.task, ok, state.boss))
    }

    /** Mini igra je končana: velja za pravilno, če je vsaj 60 % pravilnih (2 zvezdici v igri). */
    fun gameFinished(state: LessonState, correct: Int, total: Int): LessonState {
        val page = state.page as? LessonPage.TaskPage ?: return state
        if (state.feedback != null || state.outcome != null) return state
        val ok = total > 0 && correct * 10 >= total * 6
        val hint = if (state.boss) "" else page.task.explanation
        val feedback = LessonFeedback(ok, if (ok) "Odlično!" else "Skoraj!", "$correct od $total pravilnih. $hint".trim())
        return score(state, page, ok, feedback)
    }

    /** Gumb NAPREJ (po razlagi ali povratni informaciji) ali PRESKOČI. Na koncu izračuna izid. */
    fun next(state: LessonState, now: Long): LessonState {
        if (state.outcome != null) return state
        val finished = state.hearts <= 0 || state.index >= state.pages.lastIndex
        return if (finished) {
            state.copy(outcome = outcome(state, now))
        } else {
            state.copy(index = state.index + 1, answer = TaskAnswer.None, feedback = null)
        }
    }

    /** Zvezdice za lekcijo (isto pravilo kot starsFor pri igrah): brez srčkov 0; brez nalog 3; sicer po deležu pravilnih prvih odgovorov. */
    fun lessonStars(correct: Int, total: Int, passed: Boolean): Int = when {
        !passed -> 0
        total == 0 -> 3
        correct * 10 >= total * 9 -> 3
        correct * 10 >= total * 6 -> 2
        correct > 0 -> 1
        else -> 0
    }

    /** XP: 10 za lekcijo (50 za boss level) in 2 za vsak pravilen odgovor; neuspela lekcija 0. */
    fun lessonXp(stars: Int, correct: Int, boss: Boolean): Int =
        if (stars == 0) 0 else (if (boss) 50 else 10) + 2 * correct

    private fun score(state: LessonState, page: LessonPage.TaskPage, ok: Boolean, feedback: LessonFeedback): LessonState {
        val firstTry = !page.retry
        val repeatLater = !ok && firstTry && !state.boss && page.task.type in CHECKABLE_TYPES
        return state.copy(
            pages = if (repeatLater) state.pages + page.copy(retry = true) else state.pages,
            correct = state.correct + if (ok && firstTry) 1 else 0,
            scored = state.scored + if (firstTry) 1 else 0,
            hearts = if (ok) state.hearts else state.hearts - 1,
            mistakes = if (!ok && firstTry) state.mistakes + page.task.id else state.mistakes,
            feedback = feedback,
        )
    }

    private fun feedbackFor(task: Task, ok: Boolean, boss: Boolean): LessonFeedback {
        val isChoice = task.type == "choice" || task.type == "listen"
        val title = when {
            ok -> "Odlično!"
            isChoice -> "Pravilni odgovor: ${correctAnswerText(task)}"
            else -> "Skoraj!"
        }
        val text = when {
            boss -> "" // boss level: brez namigov
            ok || isChoice -> task.explanation
            else -> "Pravilno: ${correctAnswerText(task)}. ${task.explanation}".trim()
        }
        return LessonFeedback(ok, title, text)
    }

    private fun outcome(state: LessonState, now: Long): LessonOutcome {
        val passed = state.hearts > 0
        val stars = lessonStars(state.correct, state.scored, passed)
        return LessonOutcome(
            correct = state.correct,
            total = state.scored,
            mistakes = state.mistakes,
            seconds = ((now - state.startMillis) / 1000).toInt(),
            heartsLeft = max(0, state.hearts),
            stars = stars,
            xp = lessonXp(stars, state.correct, state.boss),
            boss = state.boss,
        )
    }
}
