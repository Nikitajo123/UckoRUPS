package si.uckoquiz.app.ui.lesson

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.games.GameResult
import si.uckoquiz.app.games.MiniGame
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.components.UckoMascot
import si.uckoquiz.app.ui.sample.SampleContent
import si.uckoquiz.app.ui.theme.LockedGrey
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.subjectColor

/** Naloži lekcijo iz repozitorija in jo pokaže. */
@Composable
fun LessonRoute(
    lessonId: String,
    repository: ContentRepository,
    onClose: () -> Unit,
    onFinished: (LessonOutcome) -> Unit,
    startHearts: Int = LESSON_HEARTS,
) {
    BackHandler(onBack = onClose)
    // Nov ključ ob vsakem odprtju lekcije = nov ViewModel (ponovno reševanje začne od začetka),
    // ob obračanju zaslona pa ostane isti, zato se stanje ohrani.
    val sessionKey = rememberSaveable(lessonId) { "$lessonId-${System.nanoTime()}" }
    val lesson by produceState<Lesson?>(initialValue = null, lessonId) {
        value = runCatching { repository.getLesson(lessonId) }.getOrNull() ?: Lesson(id = lessonId)
    }
    val current = lesson
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (current == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LessonSession(current, sessionKey, startHearts, onClose, onFinished)
        }
    }
}

/** Poveže zaslon z LessonViewModel; ko je lekcije konec, pošlje izid naprej. */
@Composable
private fun LessonSession(
    lesson: Lesson,
    sessionKey: String,
    startHearts: Int,
    onClose: () -> Unit,
    onFinished: (LessonOutcome) -> Unit,
) {
    val viewModel: LessonViewModel = viewModel(key = sessionKey, factory = LessonViewModel.factory(lesson, startHearts))
    val state = viewModel.state
    val currentOnFinished by rememberUpdatedState(onFinished)
    LaunchedEffect(state.outcome) {
        state.outcome?.let { currentOnFinished(it) }
    }
    LessonScreen(
        lesson = lesson,
        state = state,
        onClose = onClose,
        onAnswer = viewModel::onAnswer,
        onCheck = viewModel::onCheck,
        onGameFinished = viewModel::onGameFinished,
        onContinue = viewModel::onContinue,
        onSkipEmpty = { onFinished(LessonOutcome(1, 1, emptyList(), 0, LESSON_HEARTS, stars = 3, xp = 10)) },
    )
}

/**
 * Zaslon lekcije (Figma 07, 08, 12a, 12b): zgoraj X, napredek in srčki; na sredini razlaga ali naloga;
 * spodaj NAPREJ (razlaga), PREVERI (naloga) ali plošča s povratno informacijo.
 * Zaslon samo prikazuje [state]; vsa pravila so v LessonEngine (prek LessonViewModel).
 */
@Composable
fun LessonScreen(
    lesson: Lesson,
    state: LessonState,
    onClose: () -> Unit,
    onAnswer: (TaskAnswer) -> Unit,
    onCheck: () -> Unit,
    onGameFinished: (GameResult) -> Unit,
    onContinue: () -> Unit,
    onSkipEmpty: () -> Unit,
) {
    val color = subjectColor(lesson.subjectId)
    val page = state.page
    if (page == null) {
        EmptyLesson(onClose = onClose, onSkip = onSkipEmpty)
        return
    }

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        LessonTopBar(progress = state.progress, hearts = state.hearts, color = color, onClose = onClose)

        // Sredina: razlaga, naloga ali mini igra
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (page) {
                is LessonPage.Explanation -> ScrollablePage {
                    ExplanationPage(page.title, page.step, page.index, page.count, color)
                }
                is LessonPage.TaskPage -> when (page.task.type) {
                    in CHECKABLE_TYPES -> ScrollablePage {
                        TaskHeader(
                            if (page.retry) "Poskusi še enkrat" else taskLabel(page.task),
                            page.task.prompt,
                            color,
                        )
                        if (page.task.type == "listen") {
                            Spacer(Modifier.height(16.dp))
                            ListenPlayer(page.task, color)
                        }
                        Spacer(Modifier.height(24.dp))
                        val checked = state.feedback != null
                        val answer = state.answer
                        when (page.task.type) {
                            "match" -> MatchTask(page.task, answer as? TaskAnswer.Match ?: TaskAnswer.Match(emptyMap()), checked, color, onAnswer)
                            "drag" -> DragTask(page.task, answer as? TaskAnswer.Drag ?: TaskAnswer.Drag(emptyMap()), checked, color, onAnswer)
                            else -> ChoiceTask(
                                page.task,
                                answer as? TaskAnswer.Choice ?: TaskAnswer.Choice(emptySet()),
                                checkedCorrect = state.feedback?.correct,
                                color = color,
                                onAnswer = onAnswer,
                            )
                        }
                    }
                    "game" -> MiniGame(page.task, onGameFinished)
                    else -> ScrollablePage {
                        TaskHeader(taskLabel(page.task), page.task.prompt, color)
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Te vrste naloge (${page.task.type}) aplikacija še ne pozna.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }

        // Spodaj: povratna informacija ali gumb
        val feedback = state.feedback
        if (feedback != null) {
            FeedbackPanel(feedback.correct, feedback.title, feedback.text, onContinue = onContinue)
        } else {
            Box(Modifier.fillMaxWidth().padding(20.dp)) {
                when {
                    page is LessonPage.Explanation -> ContinueButton(onClick = onContinue)
                    page is LessonPage.TaskPage && page.task.type in CHECKABLE_TYPES ->
                        CheckButton(enabled = state.canCheck, onClick = onCheck)
                    page is LessonPage.TaskPage && page.task.type == "game" -> Unit // igra ima svoj konec
                    else -> UckoButton(text = "Preskoči", onClick = onContinue, color = LockedGrey)
                }
            }
        }
    }
}

@Composable
private fun ScrollablePage(content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        content()
    }
}

/** Lekcija še nima vsebine (vzorčne lekcije). Gumb za test jo označi kot opravljeno. */
@Composable
private fun EmptyLesson(onClose: () -> Unit, onSkip: () -> Unit) {
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        UckoMascot(Modifier.padding(bottom = 16.dp).fillMaxWidth(0.4f))
        Text("Ta lekcija še nima vsebine.", style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        UckoButton(text = "Označi kot opravljeno (test)", onClick = onSkip, color = UckoOrange)
        Spacer(Modifier.height(12.dp))
        UckoButton(text = "Nazaj", onClick = onClose, color = LockedGrey)
    }
}

/** Predogled brez ViewModela: stanje je kar v predogledu, pravila pa so ista (LessonEngine). */
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LessonScreenPreview() {
    val lesson = SampleContent.lessons.first { it.id == "mat_ulomki_01" }
    var state by remember { mutableStateOf(LessonEngine.start(lesson, now = 0L)) }
    UckoQuizTheme {
        LessonScreen(
            lesson = lesson,
            state = state,
            onClose = {},
            onAnswer = { state = LessonEngine.answer(state, it) },
            onCheck = { state = LessonEngine.check(state) },
            onGameFinished = { state = LessonEngine.gameFinished(state, it.correct, it.total) },
            onContinue = { state = LessonEngine.next(state, now = 0L) },
            onSkipEmpty = {},
        )
    }
}
