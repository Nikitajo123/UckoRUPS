package si.uckoquiz.app.ui.sample

import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.DragItem
import si.uckoquiz.app.domain.Explanation
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.LessonProgress
import si.uckoquiz.app.domain.MatchPair
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

    /** Prva prava lekcija (enaka kot content/mat/mat_ulomki_01.json), da se zaslon lekcije lahko preizkusi. */
    private val ulomki01 = Lesson(
        id = "mat_ulomki_01", subjectId = "mat", chapterId = "mat_ulomki", order = 1, title = "Kaj je ulomek?",
        explanation = Explanation(
            title = "Ulomek je del celote",
            steps = listOf(
                "Pica ima 8 kosov.",
                "Če pojemo 3 kose, smo pojedli 3/8 pice.",
                "Spodnje število pove, na koliko delov je razdeljena celota.",
            ),
        ),
        tasks = listOf(
            Task(
                id = "mat_ulomki_01_t1", type = "choice",
                prompt = "Luka je pojedel 3 od 8 kosov pice. Kolikšen del pice je pojedel?",
                answers = listOf("3/8", "8/3", "5/8"), correct = listOf(0),
                explanation = "Pojedel je 3 od 8 kosov, torej 3/8.",
            ),
            Task(
                id = "mat_ulomki_01_t2", type = "match", prompt = "Poveži ulomek z besedo.",
                pairs = listOf(MatchPair("1/2", "polovica"), MatchPair("1/3", "tretjina"), MatchPair("1/4", "četrtina")),
                explanation = "Imenovalec pove, na koliko delov delimo celoto.",
            ),
            Task(
                id = "mat_ulomki_01_t3", type = "drag", prompt = "Razvrsti ulomke.",
                categories = listOf("Manj kot 1/2", "Več kot 1/2"),
                items = listOf(
                    DragItem("1/4", "Manj kot 1/2"), DragItem("1/3", "Manj kot 1/2"),
                    DragItem("2/3", "Več kot 1/2"), DragItem("3/4", "Več kot 1/2"),
                ),
                explanation = "Ulomek je manjši od 1/2, če je števec manj kot polovica imenovalca.",
                difficulty = 2,
            ),
            Task(
                id = "mat_ulomki_01_t4", type = "game", game = "ulomki_kocke", prompt = "Sestavi ulomek 3/8.",
                config = mapOf("numerator" to "3", "denominator" to "8"),
                explanation = "3 obarvane kocke od 8 so 3/8.", difficulty = 2,
            ),
        ),
    )

    /** Angliska lekcija s poslušanjem (Figma 11), da se lahko preizkusi TTS. */
    private val pozdravi = Lesson(
        id = "ang_family_01", subjectId = "ang", chapterId = "ang_family", order = 1, title = "Pozdravi",
        explanation = Explanation(
            title = "Kako pozdravimo po angleško?",
            steps = listOf(
                "Hello! pomeni Živijo!",
                "Good morning! rečemo zjutraj.",
                "Goodbye! pomeni Nasvidenje!",
            ),
        ),
        tasks = listOf(
            Task(
                id = "ang_family_01_t1", type = "listen", prompt = "Kaj slišiš?", audioText = "Good morning",
                answers = listOf("Good night", "Good morning", "Goodbye"), correct = listOf(1),
                explanation = "Good morning pomeni dobro jutro.",
            ),
            Task(
                id = "ang_family_01_t2", type = "choice", prompt = "Kako rečeš Nasvidenje po angleško?",
                answers = listOf("Hello", "Goodbye", "Thank you"), correct = listOf(1),
                explanation = "Goodbye pomeni nasvidenje.",
            ),
            Task(
                id = "ang_family_01_t3", type = "listen", prompt = "Kaj slišiš?", audioText = "my brother",
                answers = listOf("my sister", "my brother", "my mother"), correct = listOf(1),
                explanation = "Brother je brat.",
            ),
        ),
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
    }.map {
        when (it.id) {
            ulomki01.id -> ulomki01
            pozdravi.id -> pozdravi
            else -> it
        }
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
