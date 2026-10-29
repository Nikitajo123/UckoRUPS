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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.Task
import si.uckoquiz.app.games.MiniGame
import si.uckoquiz.app.games.starsFor
import si.uckoquiz.app.ui.components.OptionCard
import si.uckoquiz.app.ui.components.OptionState
import si.uckoquiz.app.ui.components.UckoButton
import si.uckoquiz.app.ui.components.UckoMascot
import si.uckoquiz.app.ui.sample.SampleContent
import si.uckoquiz.app.ui.theme.LockedGrey
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.subjectColor

private const val START_HEARTS = 5

/** Kaj pokaže spodnja plošča po preverjanju. */
private data class Feedback(val correct: Boolean, val title: String, val text: String)

/** Naloži lekcijo iz repozitorija in jo pokaže. */
@Composable
fun LessonRoute(
    lessonId: String,
    repository: ContentRepository,
    onClose: () -> Unit,
    onFinished: (LessonOutcome) -> Unit,
) {
    BackHandler(onBack = onClose)
    val lesson by produceState<Lesson?>(initialValue = null, lessonId) {
        value = runCatching { repository.getLesson(lessonId) }.getOrNull() ?: Lesson(id = lessonId)
    }
    val current = lesson
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (current == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LessonScreen(current, onClose, onFinished)
        }
    }
}

/**
 * Zaslon lekcije (Figma 07, 08, 12a, 12b): zgoraj X, napredek in srčki; na sredini razlaga ali naloga;
 * spodaj NAPREJ (razlaga), PREVERI (naloga) ali plošča s povratno informacijo.
 * Napačen odgovor vzame srček; ko srčkov zmanjka, se lekcija komča.
 * Zahtevnejša logika (zvezdice, boss level) pride z UQ-34, ostale vrste nalog z UQ-32 in UQ-33.
 */
@Composable
fun LessonScreen(lesson: Lesson, onClose: () -> Unit, onFinished: (LessonOutcome) -> Unit) {
    val pages = remember(lesson) { lessonPages(lesson) }
    val color = subjectColor(lesson.subjectId)

    var index by remember(lesson.id) { mutableIntStateOf(0) }
    var hearts by remember(lesson.id) { mutableIntStateOf(START_HEARTS) }
    var correctCount by remember(lesson.id) { mutableIntStateOf(0) }
    var scoredCount by remember(lesson.id) { mutableIntStateOf(0) }
    var mistakes by remember(lesson.id) { mutableStateOf(listOf<String>()) }
    var selected by remember(lesson.id) { mutableStateOf(setOf<Int>()) }
    var feedback by remember(lesson.id) { mutableStateOf<Feedback?>(null) }
    val startMillis = remember(lesson.id) { System.currentTimeMillis() }

    fun finish(heartsLeft: Int) {
        val seconds = ((System.currentTimeMillis() - startMillis) / 1000).toInt()
        onFinished(LessonOutcome(correctCount, scoredCount, mistakes, seconds, heartsLeft))
    }

    fun goNext() {
        if (hearts <= 0 || index >= pages.lastIndex) {
            finish(hearts)
        } else {
            index++
            selected = emptySet()
            feedback = null
        }
    }

    fun score(task: Task, ok: Boolean) {
        scoredCount++
        if (ok) correctCount++ else {
            hearts--
            mistakes = mistakes + task.id
        }
    }

    if (pages.isEmpty()) {
        EmptyLesson(onClose = onClose, onSkip = { onFinished(LessonOutcome(1, 1, emptyList(), 0, START_HEARTS)) })
        return
    }
    val page = pages[index]
    val progress = (index + if (feedback != null) 1 else 0) / pages.size.toFloat()

    Column(Modifier.fillMaxSize().safeDrawingPadding()) {
        LessonTopBar(progress = progress, hearts = hearts, color = color, onClose = onClose)

        // Sredina: razlaga, naloga ali mini igra
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (page) {
                is LessonPage.Explanation -> ScrollablePage {
                    ExplanationPage(page.title, page.step, page.index, page.count, color)
                }
                is LessonPage.TaskPage -> when (page.task.type) {
                    "choice", "listen" -> ScrollablePage {
                        TaskHeader(taskLabel(page.task), page.task.prompt, color)
                        if (page.task.type == "listen") {
                            Spacer(Modifier.height(8.dp))
                            Text("🔊 Poslušanje pride z UQ-33.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                        }
                        Spacer(Modifier.height(24.dp))
                        ChoiceAnswers(
                            task = page.task,
                            selected = selected,
                            checkedCorrect = feedback?.correct,
                            color = color,
                            onSelect = { i ->
                                selected = if (page.task.correct.size > 1) {
                                    if (i in selected) selected - i else selected + i
                                } else setOf(i)
                            },
                        )
                    }
                    "game" -> MiniGame(page.task) { result ->
                        val ok = starsFor(result) >= 2
                        score(page.task, ok)
                        feedback = Feedback(
                            correct = ok,
                            title = if (ok) "Odlično!" else "Skoraj!",
                            text = "${result.correct} od ${result.total} pravilnih. ${page.task.explanation}".trim(),
                        )
                    }
                    else -> ScrollablePage {
                        TaskHeader(taskLabel(page.task), page.task.prompt, color)
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Ta vrsta naloge (${page.task.type}) pride z UQ-32.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                        )
                    }
                }
            }
        }

        // Spodaj: povratna informacija ali gumb
        val currentFeedback = feedback
        if (currentFeedback != null) {
            FeedbackPanel(currentFeedback.correct, currentFeedback.title, currentFeedback.text, onContinue = { goNext() })
        } else {
            Box(Modifier.fillMaxWidth().padding(20.dp)) {
                when {
                    page is LessonPage.Explanation -> ContinueButton(onClick = { goNext() })
                    page is LessonPage.TaskPage && page.task.type in setOf("choice", "listen") ->
                        CheckButton(enabled = selected.isNotEmpty()) {
                            val ok = isChoiceCorrect(page.task, selected)
                            score(page.task, ok)
                            feedback = Feedback(
                                correct = ok,
                                title = if (ok) "Odlično!" else "Pravilni odgovor: ${correctAnswerText(page.task)}",
                                text = page.task.explanation,
                            )
                        }
                    page is LessonPage.TaskPage && page.task.type == "game" -> Unit // igra ima svoj konec
                    else -> UckoButton(text = "Preskoči", onClick = { goNext() }, color = LockedGrey)
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

/** Odgovori kot kartice (Figma 08): izbran je v barvi predmeta; po preverjanju zelen ali rdeč. */
@Composable
private fun ChoiceAnswers(
    task: Task,
    selected: Set<Int>,
    checkedCorrect: Boolean?,
    color: Color,
    onSelect: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        task.answers.forEachIndexed { i, answer ->
            val state = when {
                i !in selected -> OptionState.DEFAULT
                checkedCorrect == null -> OptionState.SELECTED
                checkedCorrect -> OptionState.CORRECT
                else -> OptionState.WRONG
            }
            OptionCard(
                state = state,
                onClick = { if (checkedCorrect == null) onSelect(i) },
                selectedColor = color,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(answer, fontWeight = FontWeight.Bold)
            }
        }
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LessonScreenPreview() {
    val lesson = SampleContent.lessons.first { it.id == "mat_ulomki_01" }
    UckoQuizTheme { LessonScreen(lesson, onClose = {}, onFinished = {}) }
}
