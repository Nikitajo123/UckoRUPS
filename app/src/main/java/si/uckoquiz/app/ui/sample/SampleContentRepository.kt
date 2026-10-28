package si.uckoquiz.app.ui.sample

import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.LessonProgress
import si.uckoquiz.app.domain.Subject
import si.uckoquiz.app.domain.Task

/**
 * Vzorčna vsebina (poglavja in lekcije iz načrta), dokler vsa vsebina ni v Firestoru.
 * Lekcije se nimajo nalog; mini igre in boss leveli so označeni, da se pot pravilno nariše.
 */
object SampleContent {
    val subjects = listOf(
        Subject(id = "mat", name = "Matematika", order = 1, color = "#F7A04F", icon = "calculate"),
        Subject(id = "ang", name = "Angleščina", order = 2, color = "#4F8EF7", icon = "translate"),
        Subject(id = "nar", name = "Naravoslovje", order = 3, color = "#4FBF7A", icon = "science"),
    )

    val chapters = listOf(
        Chapter(id = "mat_ulomki", subjectId = "mat", order = 1, title = "Ulomki"),
        Chapter(id = "mat_decimalna", subjectId = "mat", order = 2, title = "Decimalna števila"),
        Chapter(id = "ang_family", subjectId = "ang", order = 1, title = "Hello & Family"),
        Chapter(id = "ang_school", subjectId = "ang", order = 2, title = "School & Daily Routine"),
        Chapter(id = "nar_snovi", subjectId = "nar", order = 1, title = "Lastnosti snovi"),
        Chapter(id = "nar_zmesi", subjectId = "nar", order = 2, title = "Zmesi in ločevanje"),
    )

    val lessons: List<Lesson> = buildList {
        addChapter("mat", "mat_ulomki", listOf("Del celote", "Ulomki s kockami", "Primerjanje", "Enakovredni ulomki"),
            "ulomki_kocke", "Ulomki s kockami")
        addChapter("mat", "mat_decimalna", listOf("Zapis", "Primerjanje", "Seštevanje in odštevanje"),
            "decimalke_2048", "2048 z decimalkami", boss = "2048 – ulomki")
        addChapter("ang", "ang_family", listOf("Pozdravi", "to be", "Družina", "my / your / his / her"),
            "sentence_builder", "Sentence Builder")
        addChapter("ang", "ang_school", listOf("Šolske potrebščine", "Ura in dnevi", "Present simple", "Moj dan"),
            "wordle_en", "Wordle EN", boss = "A Day in London")
        addChapter("nar", "nar_snovi", listOf("Kaj je snov", "Trdno, tekoče, plinasto", "Gostota", "Prevodnost", "Magnetnost"),
            "plava_potone", "Plava ali potone")
        addChapter("nar", "nar_zmesi", listOf("Čiste snovi in zmesi", "Raztopine", "Ločevanje zmesi"),
            "laboratorij", "Laboratorij", boss = "Skrivnostni laboratorij")
    }

    // Napredek za prikaz: nekaj opravljenih lekcij, da se vidijo vsa stanja krogcev
    val demoProgress = mapOf(
        "mat_ulomki_01" to LessonProgress(stars = 3),
        "mat_ulomki_02" to LessonProgress(stars = 2),
        "ang_family_01" to LessonProgress(stars = 1),
    )

    private fun MutableList<Lesson>.addChapter(
        subjectId: String,
        chapterId: String,
        titles: List<String>,
        gameId: String,
        gameTitle: String,
        boss: String? = null,
    ) {
        titles.forEachIndexed { i, title ->
            add(Lesson(id = "${chapterId}_${(i + 1).toString().padStart(2, '0')}", subjectId = subjectId, chapterId = chapterId,
                order = i + 1, title = title))
        }
        add(Lesson(id = "${chapterId}_igra", subjectId = subjectId, chapterId = chapterId, order = titles.size + 1,
            title = gameTitle, tasks = listOf(Task(id = "${chapterId}_igra_t1", type = "game", game = gameId))))
        if (boss != null) {
            add(Lesson(id = "${chapterId}_boss", subjectId = subjectId, chapterId = chapterId, order = titles.size + 2,
                title = boss, boss = true))
        }
    }
}

/**
 * ContentRepository z vzorčno vsebino v pomnilniku. Ko bo vsebina v Firestoru (UQ-20),
 * ga v UckoApp zamenjamo s FirestoreContentRepository(). saveResult si napredek zapomni do zaprtja aplikacije.
 */
class SampleContentRepository(
    initialProgress: Map<String, LessonProgress> = SampleContent.demoProgress,
) : ContentRepository {
    private val progress = initialProgress.toMutableMap()

    override suspend fun getSubjects(): List<Subject> = SampleContent.subjects

    override suspend fun getChapters(subjectId: String): List<Chapter> =
        SampleContent.chapters.filter { it.subjectId == subjectId }.sortedBy { it.order }

    override suspend fun getLessons(chapterId: String): List<Lesson> =
        SampleContent.lessons.filter { it.chapterId == chapterId }.sortedBy { it.order }

    override suspend fun getLesson(lessonId: String): Lesson =
        SampleContent.lessons.first { it.id == lessonId }

    override suspend fun getProgress(): Map<String, LessonProgress> = progress.toMap()

    override suspend fun saveResult(lessonId: String, stars: Int, xp: Int, mistakes: List<String>) {
        val old = progress[lessonId] ?: LessonProgress()
        progress[lessonId] = old.copy(
            stars = maxOf(old.stars, stars),
            attempts = old.attempts + 1,
            mistakes = mistakes,
            completedAt = System.currentTimeMillis(),
        )
    }
}
