package si.uckoquiz.app.ui.subjects

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.ui.home.buildPath
import si.uckoquiz.app.ui.sample.SampleContent
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.subjectColor

/** Predmeti iz načrta, ki jih v tem projektu še ne izdelamo. */
private const val COMING_SOON = "Kmalu: Zgodovina, Geografija, Glasba"
private val SoonGrey = Color(0xFF8A8F98)

/** Naloži napredek vseh predmetov iz repozitorija in pokaže zaslon Predmeti. */
@Composable
fun SubjectsRoute(repository: ContentRepository, onOpenSubject: (String) -> Unit) {
    val summaries by produceState<List<SubjectSummary>?>(initialValue = null, repository) {
        value = runCatching {
            val progress = repository.getProgress()
            repository.getSubjects().map { subject ->
                val chapters = repository.getChapters(subject.id)
                val lessons = chapters.associate { it.id to repository.getLessons(it.id) }
                summarize(subject, buildPath(chapters, lessons, progress))
            }
        }.getOrDefault(emptyList())
    }
    SubjectsScreen(summaries, onOpenSubject)
}

/** Zaslon Predmeti (Figma 06_Predmeti): kartica za vsak predmet z napredkom. */
@Composable
fun SubjectsScreen(summaries: List<SubjectSummary>?, onOpenSubject: (String) -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Predmeti", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        if (summaries == null) {
            Box(Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            summaries.forEach { summary ->
                SubjectCard(summary, onClick = { onOpenSubject(summary.subject.id) })
                Spacer(Modifier.height(14.dp))
            }
            ComingSoonCard()
        }
    }
}

@Composable
private fun SubjectCard(summary: SubjectSummary, onClick: () -> Unit) {
    val color = subjectColor(summary.subject.id)
    val shape = RoundedCornerShape(16.dp)
    val chapterText = if (summary.chapterCount > 0) "Poglavje ${summary.currentChapter} od ${summary.chapterCount}" else "Še brez lekcij"
    // Temnejši spodnji rob kot v Figmi (kartica je videti "dvignjena").
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = "${summary.subject.name}, $chapterText, ${summary.percent} odstotkov opravljeno"
            }
    ) {
        Box(Modifier.matchParentSize().offset(y = 4.dp).clip(shape).background(color.darker()))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(color)
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(summary.subject.name.take(1), color = color, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(summary.subject.name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text(chapterText, color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                LinearProgressIndicator(
                    progress = { summary.percent / 100f },
                    modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(50)),
                    color = Color.White,
                    trackColor = color.darker(),
                )
                Spacer(Modifier.width(12.dp))
                Text("${summary.percent} %", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

/** Siva kartica s črtkanim robom za predmete, ki pridejo kasneje. */
@Composable
private fun ComingSoonCard() {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFC8C8C8),
                    style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                    cornerRadius = CornerRadius(16.dp.toPx()),
                )
            }
            .padding(vertical = 22.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(COMING_SOON, color = SoonGrey, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
    }
}

private fun Color.darker(factor: Float = 0.8f) = Color(red * factor, green * factor, blue * factor, alpha)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SubjectsScreenPreview() {
    val lessons = SampleContent.lessons.groupBy { it.chapterId }
    val summaries = SampleContent.subjects.map { subject ->
        val chapters = SampleContent.chapters.filter { it.subjectId == subject.id }
        summarize(subject, buildPath(chapters, lessons, SampleContent.demoProgress))
    }
    UckoQuizTheme { SubjectsScreen(summaries, onOpenSubject = {}) }
}
