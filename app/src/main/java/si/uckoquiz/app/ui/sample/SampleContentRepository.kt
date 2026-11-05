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
        Chapter(id = "mat_deljivost", subjectId = "mat", order = 1, title = "Deljivost"),
        Chapter(id = "mat_koti", subjectId = "mat", order = 2, title = "Koti"),
        Chapter(id = "mat_obseg", subjectId = "mat", order = 3, title = "Obseg in ploščina"),
        Chapter(id = "ang_family", subjectId = "ang", order = 1, title = "Hello & Family"),
        Chapter(id = "ang_school", subjectId = "ang", order = 2, title = "School & Daily Routine"),
        Chapter(id = "ang_home", subjectId = "ang", order = 3, title = "Food, Animals & Home"),
        Chapter(id = "nar_snovi", subjectId = "nar", order = 1, title = "Lastnosti snovi"),
        Chapter(id = "nar_zmesi", subjectId = "nar", order = 2, title = "Zmesi in ločevanje"),
        Chapter(id = "nar_raziskujemo", subjectId = "nar", order = 3, title = "Raziskujemo snovi"),
    )

    /** Prva prava lekcija (enaka kot content/mat/mat_deljivost_01.json), da se zaslon lekcije lahko preizkusi. */
    private val deljivost01 = Lesson(
        id = "mat_deljivost_01", subjectId = "mat", chapterId = "mat_deljivost", order = 1, title = "Večkratniki in delitelji",
        explanation = Explanation(
            title = "Večkratniki in delitelji",
            steps = listOf(
                "Večkratnike števila 4 dobimo, če 4 množimo z 1, 2, 3 ...: 4, 8, 12, 16 ...",
                "Delitelj števila 12 je vsako število, s katerim 12 delimo brez ostanka: 1, 2, 3, 4, 6, 12.",
                "Vsako število je deljivo z 1 in s samim seboj.",
            ),
        ),
        tasks = listOf(
            Task(
                id = "mat_deljivost_01_t1",
                type = "choice",
                prompt = "Kateri je večkratnik števila 6?",
                answers = listOf("16", "18", "20"),
                correct = listOf(1),
                explanation = "6 · 3 = 18.",
            ),
            Task(
                id = "mat_deljivost_01_t2",
                type = "choice",
                prompt = "Katero število NI delitelj števila 20?",
                answers = listOf("4", "5", "6"),
                correct = listOf(2),
                explanation = "20 : 6 = 3, ostanek 2.",
            ),
            Task(
                id = "mat_deljivost_01_t3",
                type = "drag",
                prompt = "Razvrsti števila.",
                categories = listOf("Večkratnik števila 3", "Ni večkratnik števila 3"),
                items = listOf(DragItem("9", "Večkratnik števila 3"), DragItem("14", "Ni večkratnik števila 3"), DragItem("21", "Večkratnik števila 3"), DragItem("25", "Ni večkratnik števila 3"), DragItem("30", "Večkratnik števila 3"), DragItem("8", "Ni večkratnik števila 3")),
                explanation = "Večkratniki števila 3 so 3, 6, 9, 12, 15, 18, 21, 24, 27, 30 ...",
                difficulty = 2,
            ),
            Task(
                id = "mat_deljivost_01_t4",
                type = "match",
                prompt = "Poveži število z vsemi njegovimi delitelji.",
                pairs = listOf(MatchPair("6", "1, 2, 3, 6"), MatchPair("9", "1, 3, 9"), MatchPair("10", "1, 2, 5, 10"), MatchPair("7", "1, 7")),
                explanation = "Delitelji delijo število brez ostanka.",
            ),
            Task(
                id = "mat_deljivost_01_t5",
                type = "choice",
                prompt = "Koliko deliteljev ima število 12?",
                answers = listOf("4", "5", "6"),
                correct = listOf(2),
                explanation = "Delitelji števila 12 so 1, 2, 3, 4, 6 in 12, torej 6.",
                difficulty = 2,
            ),
            Task(
                id = "mat_deljivost_01_t6",
                type = "choice",
                prompt = "Maja ima 24 bonbonov. Vsak prijatelj naj dobi enako in noben bonbon ne sme ostati. Med koliko prijateljev jih NE more razdeliti?",
                answers = listOf("3", "5", "8"),
                correct = listOf(1),
                explanation = "24 ni deljivo s 5 (24 : 5 = 4, ostanek 4).",
                difficulty = 2,
            ),
            Task(
                id = "mat_deljivost_01_t7",
                type = "choice",
                prompt = "Katero število je večkratnik števila 4 in hkrati števila 6?",
                answers = listOf("10", "12", "16"),
                correct = listOf(1),
                explanation = "Večkratniki 4: 4, 8, 12 ... Večkratniki 6: 6, 12 ... Skupni je 12.",
                difficulty = 2,
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

    /** Boss level matematike (enak kot content/mat/mat_obseg_boss.json): mešane naloge iz vseh treh poglavij, 3 srčki, brez namigov. */
    private val mathBoss = Lesson(
        id = "mat_obseg_boss", subjectId = "mat", chapterId = "mat_obseg", order = 7, title = "Geometrijski grad", boss = true,
        tasks = listOf(
            Task(
                id = "mat_obseg_boss_t1",
                type = "choice",
                prompt = "Katero število je deljivo s 3 in z 9?",
                answers = listOf("123", "243", "124"),
                correct = listOf(1),
                difficulty = 2,
            ),
            Task(
                id = "mat_obseg_boss_t2",
                type = "choice",
                prompt = "Katero število je praštevilo?",
                answers = listOf("21", "31", "33"),
                correct = listOf(1),
            ),
            Task(
                id = "mat_obseg_boss_t3",
                type = "drag",
                prompt = "Razvrsti števila.",
                categories = listOf("Deljivo z 2", "Ni deljivo z 2"),
                items = listOf(DragItem("14", "Deljivo z 2"), DragItem("33", "Ni deljivo z 2"), DragItem("50", "Deljivo z 2"), DragItem("71", "Ni deljivo z 2"), DragItem("98", "Deljivo z 2"), DragItem("15", "Ni deljivo z 2")),
                difficulty = 2,
            ),
            Task(
                id = "mat_obseg_boss_t4",
                type = "match",
                prompt = "Poveži kot z njegovo vrsto.",
                pairs = listOf(MatchPair("45°", "ostri kot"), MatchPair("90°", "pravi kot"), MatchPair("150°", "topi kot"), MatchPair("180°", "iztegnjeni kot")),
            ),
            Task(
                id = "mat_obseg_boss_t5",
                type = "choice",
                prompt = "Kakšen kot oklepata urna kazalca ob 5.00?",
                answers = listOf("ostri", "topi", "pravi"),
                correct = listOf(1),
            ),
            Task(
                id = "mat_obseg_boss_t6",
                type = "choice",
                prompt = "Pravokotnik meri 8 cm × 5 cm. Koliko meri njegov obseg?",
                answers = listOf("26 cm", "40 cm", "13 cm"),
                correct = listOf(0),
            ),
            Task(
                id = "mat_obseg_boss_t7",
                type = "choice",
                prompt = "Kvadrat ima ploščino 49 cm². Koliko meri njegova stranica?",
                answers = listOf("7 cm", "12 cm", "24 cm"),
                correct = listOf(0),
                difficulty = 2,
            ),
            Task(
                id = "mat_obseg_boss_t8",
                type = "choice",
                prompt = "Koliko m² je 3 a?",
                answers = listOf("30 m²", "300 m²", "3000 m²"),
                correct = listOf(1),
            ),
        ),
    )

    val lessons: List<Lesson> = buildList {
        addChapter("mat", "mat_deljivost", listOf("Večkratniki in delitelji", "Deljivost z 2, 5 in 10", "Deljivost s 3 in 9", "Praštevila", "Skupni delitelji in večkratniki"),
            "deljivi_baloni", "Deljivi baloni")
        addChapter("mat", "mat_koti", listOf("Vrste kotov", "Vzporednice in pravokotnice", "Merjenje kotov", "Računanje s koti", "Koti na uri in kompasu"),
            "lovec_na_kote", "Lovec na kote")
        addChapter("mat", "mat_obseg", listOf("Obseg", "Ploščina", "Enote za ploščino", "Sestavljeni liki", "Obseg in ploščina okrog nas"),
            "vrtnar", "Vrtnar", boss = "Geometrijski grad")
        addChapter("ang", "ang_family", listOf("Pozdravi", "to be", "Družina", "my / your / his / her"),
        addChapter("mat", "mat_ulomki", listOf("Del celote", "Ulomki s kockami", "Primerjanje", "Enakovredni ulomki"),
            "ulomki_kocke", "Ulomki s kockami")
        addChapter("mat", "mat_decimalna", listOf("Zapis", "Primerjanje", "Seštevanje in odštevanje"),
            "decimalke_2048", "2048 z decimalkami", boss = "2048 – ulomki")
        addChapter("ang", "ang_family", listOf("Pozdravi", "to be", "Družina", "my / your / his / her", "Have got"),
            "sentence_builder", "Sentence Builder")
        addChapter("ang", "ang_school", listOf("Šolske potrebščine", "Ura in dnevi", "Present simple", "Moj dan", "Šolski predmeti"),
            "wordle_en", "Wordle EN")
        addChapter("ang", "ang_home", listOf("Hrana in pijača", "Živali", "Can / can't", "Moj dom", "In, on, under"),
            "spomin", "Spomin", boss = "Veliki izziv")
        addChapter("nar", "nar_snovi", listOf("Kaj je snov", "Trdno, tekoče, plinasto", "Gostota", "Prevodnost", "Magnetnost"),
            "plava_potone", "Plava ali potone")
        addChapter("nar", "nar_zmesi", listOf("Čiste snovi in zmesi", "Raztopine", "Ločevanje zmesi", "Usedanje in odlivanje", "Zmesi okoli nas"),
            "laboratorij", "Laboratorij")
        addChapter("nar", "nar_raziskujemo", listOf("Topne in netopne snovi", "Hitrost raztapljanja", "Homogene in heterogene zmesi", "Spremembe stanj", "Gostota tekočin"),
            "hitro_razvrsti", "Hitro razvrsti", boss = "Skrivnostni laboratorij")
    }.map {
        when (it.id) {
            deljivost01.id -> deljivost01
            pozdravi.id -> pozdravi
            mathBoss.id -> mathBoss
            else -> it
        }
    }

    // Napredek za prikaz: nekaj opravljenih lekcij, da se vidijo vsa stanja krogcev
    val demoProgress = mapOf(
        "mat_deljivost_01" to LessonProgress(stars = 3),
        "mat_deljivost_02" to LessonProgress(stars = 2),
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

    override suspend fun saveResult(lessonId: String, stars: Int, xp: Int, mistakes: List<String>, perfect: Boolean) {
        val old = progress[lessonId] ?: LessonProgress()
        progress[lessonId] = old.copy(
            stars = maxOf(old.stars, stars),
            attempts = old.attempts + 1,
            mistakes = mistakes,
            completedAt = System.currentTimeMillis(),
            perfect = old.perfect || perfect,
        )
    }
}
