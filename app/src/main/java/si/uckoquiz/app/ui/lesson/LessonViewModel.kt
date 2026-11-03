package si.uckoquiz.app.ui.lesson

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.games.GameResult

/**
 * ViewModel lekcije (MVVM): hrani LessonState in ga spreminja samo prek LessonEngine.
 * Zaslon (LessonScreen) samo prikazuje stanje in kliče te funkcije. Stanje preživi obračanje zaslona.
 */
class LessonViewModel(
    lesson: Lesson,
    startHearts: Int = LESSON_HEARTS,
    private val clock: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    var state by mutableStateOf(LessonEngine.start(lesson, clock(), startHearts))
        private set

    fun onAnswer(answer: TaskAnswer) {
        state = LessonEngine.answer(state, answer)
    }

    fun onCheck() {
        state = LessonEngine.check(state)
    }

    fun onGameFinished(result: GameResult) {
        state = LessonEngine.gameFinished(state, result.correct, result.total)
    }

    fun onContinue() {
        state = LessonEngine.next(state, clock())
    }

    companion object {
        fun factory(lesson: Lesson, startHearts: Int = LESSON_HEARTS): ViewModelProvider.Factory = viewModelFactory {
            initializer { LessonViewModel(lesson, startHearts) }
        }
    }
}
