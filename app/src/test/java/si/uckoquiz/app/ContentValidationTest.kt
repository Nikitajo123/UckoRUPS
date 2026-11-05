package si.uckoquiz.app

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.fail
import org.junit.Test
import java.io.File

/**
 * UQ-14: checks every JSON file in content/ before it is uploaded to Firestore.
 * Errors fail the test. Warnings are only printed (see the test report).
 */
class ContentValidationTest {

    private val knownGames = setOf(
        "sentence_builder", "wordle_en", "plava_potone", "laboratorij", "decimalke_2048", "ulomki_kocke",
        "hitro_razvrsti", "spomin", "boss_fight",
    )
    private val taskTypes = setOf("choice", "match", "drag", "listen", "game")

    private fun contentDir(): File =
        listOf(File("../content"), File("content")).firstOrNull { it.isDirectory }
            ?: error("content/ folder not found. Working dir: ${File(".").absolutePath}")

    private fun readArray(file: File) = JSONArray(file.readText(Charsets.UTF_8))
    private fun readObject(file: File) = JSONObject(file.readText(Charsets.UTF_8))
    private fun JSONArray.objects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
    private fun JSONArray.strings(): List<String> = (0 until length()).map { getString(it) }
    private fun JSONArray.ints(): List<Int> = (0 until length()).map { getInt(it) }
    private fun JSONObject.array(key: String): JSONArray = optJSONArray(key) ?: JSONArray()
    private fun JSONObject.config(): Map<String, String> {
        val c = optJSONObject("config") ?: return emptyMap()
        return c.keys().asSequence().associateWith { c.getString(it) }
    }
    private fun parts(text: String): Int = text.split("|").size

    @Test
    fun contentIsValid() {
        val dir = contentDir()
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        val subjects = readArray(File(dir, "subjects.json")).objects()
        val subjectIds = subjects.map { it.getString("id") }.toSet()
        val colorRegex = Regex("#[0-9A-Fa-f]{6}")
        subjects.forEach { s ->
            if (!colorRegex.matches(s.getString("color"))) errors += "subjects.json: ${s.getString("id")} has an invalid color"
        }

        val chapters = readArray(File(dir, "chapters.json")).objects()
        val chapterSubject = chapters.associate { it.getString("id") to it.getString("subjectId") }
        if (chapterSubject.size != chapters.size) errors += "chapters.json: duplicate chapter id"
        chapters.forEach { c ->
            if (c.getString("subjectId") !in subjectIds) errors += "chapters.json: ${c.getString("id")} has an unknown subjectId"
        }

        val lessonIds = mutableSetOf<String>()
        val taskIds = mutableSetOf<String>()
        val orders = mutableSetOf<Pair<String, Int>>()
        val lessonsPerChapter = mutableMapOf<String, Int>()

        val folders = dir.listFiles()?.filter { it.isDirectory }?.sortedBy { it.name }.orEmpty()
        for (folder in folders) {
            val files = folder.listFiles()?.filter { it.name.endsWith(".json") }?.sortedBy { it.name }.orEmpty()
            for (file in files) {
                val lesson = try {
                    readObject(file)
                } catch (e: Exception) {
                    errors += "${folder.name}/${file.name}: not valid JSON (${e.message})"
                    continue
                }
                val lid = lesson.optString("id")
                fun err(msg: String) { errors += "$lid: $msg" }

                if (lid != file.nameWithoutExtension) err("id does not match the file name '${file.name}'")
                if (!lessonIds.add(lid)) err("duplicate lesson id")
                val subjectId = lesson.optString("subjectId")
                if (subjectId != folder.name) err("subjectId '$subjectId' does not match folder '${folder.name}'")
                if (subjectId !in subjectIds) err("unknown subjectId '$subjectId'")
                val chapterId = lesson.optString("chapterId")
                val chapterOwner = chapterSubject[chapterId]
                if (chapterOwner == null) err("unknown chapterId '$chapterId'")
                else if (chapterOwner != subjectId) err("chapter '$chapterId' belongs to another subject")
                val order = lesson.optInt("order", 0)
                if (order < 1) err("order must be 1 or more")
                if (!orders.add(chapterId to order)) err("another lesson in chapter '$chapterId' already has order $order")
                lessonsPerChapter[chapterId] = (lessonsPerChapter[chapterId] ?: 0) + 1
                if (lesson.optString("title").isBlank()) err("blank title")

                val tasks = lesson.array("tasks").objects()
                if (tasks.isEmpty()) err("no tasks")
                val boss = lesson.optBoolean("boss", false)
                val onlyGames = tasks.isNotEmpty() && tasks.all { it.optString("type") == "game" }
                val steps = lesson.optJSONObject("explanation")?.array("steps")?.length() ?: 0
                if (!boss && !onlyGames && steps == 0) err("a normal lesson needs explanation steps")
                tasks.forEach { checkTask(lid, it, taskIds, errors, warnings) }
            }
        }

        chapters.forEach { c ->
            if ((lessonsPerChapter[c.getString("id")] ?: 0) == 0) warnings += "chapter ${c.getString("id")} has no lessons"
        }

        warnings.forEach { println("WARNING: $it") }
        if (errors.isNotEmpty()) {
            fail("Found ${errors.size} problem(s) in content/:\n" + errors.joinToString("\n") { " - $it" })
        }
    }

    private fun checkTask(
        lid: String,
        t: JSONObject,
        taskIds: MutableSet<String>,
        errors: MutableList<String>,
        warnings: MutableList<String>,
    ) {
        val tid = t.optString("id")
        fun err(msg: String) { errors += "$lid / $tid: $msg" }
        fun warn(msg: String) { warnings += "$lid / $tid: $msg" }

        if (!taskIds.add(tid)) err("duplicate task id")
        if (!tid.startsWith(lid + "_t")) warn("task id should start with ${lid}_t")
        val type = t.optString("type")
        if (type !in taskTypes) {
            err("unknown type '$type'")
            return
        }
        if (type != "game" && t.optString("prompt").isBlank()) err("blank prompt")

        when (type) {
            "choice", "listen" -> {
                val answers = t.array("answers").strings()
                val correct = t.array("correct").ints()
                if (answers.size < 2) err("needs at least 2 answers")
                if (correct.isEmpty() || correct.any { it < 0 || it >= answers.size }) err("'correct' is empty or points outside 'answers'")
                if (answers.toSet().size != answers.size) err("duplicate answers")
                if (type == "listen" && t.optString("audioText").isBlank()) err("listen task without audioText")
            }
            "match" -> {
                val pairs = t.array("pairs").objects()
                if (pairs.size < 2) err("needs at least 2 pairs")
                // The same word twice on one side (we = are, they = are) is fine: the app compares the text.
            }
            "drag" -> {
                val cats = t.array("categories").strings()
                val items = t.array("items").objects()
                if (cats.size < 2 || items.size < 2) err("needs at least 2 categories and 2 items")
                if (items.any { it.getString("category") !in cats }) err("an item has a category that is not in 'categories'")
                if (cats.any { c -> items.none { it.getString("category") == c } }) warn("a category has no items")
            }
            "game" -> {
                val game = t.optString("game")
                val cfg = t.config()
                if (game !in knownGames) {
                    err("unknown game id '$game'")
                    return
                }
                when (game) {
                    "plava_potone" -> {
                        val items = t.array("items").objects()
                        if (items.size < 2) err("needs at least 2 items")
                        if (items.any { it.getString("category").trim().lowercase() !in setOf("plava", "potone") }) {
                            err("item category must be 'plava' or 'potone'")
                        }
                        cfg["why"]?.let { if (parts(it) != items.size) err("config.why has ${parts(it)} parts but there are ${items.size} items") }
                    }
                    "laboratorij" -> {
                        val mixtures = cfg["mixtures"]
                        val steps = cfg["steps"]
                        if (mixtures != null || steps != null) {
                            if (mixtures == null || steps == null) {
                                err("config.mixtures and config.steps must both exist")
                            } else {
                                if (parts(mixtures) != parts(steps)) err("config.mixtures and config.steps need the same number of parts")
                                cfg["why"]?.let { if (parts(it) != parts(mixtures)) err("config.why needs one part per mixture") }
                            }
                        }
                    }
                    "wordle_en" -> {
                        val words = t.array("answers").strings()
                        if (words.isEmpty()) err("needs answers (the words)")
                        if (words.any { it.trim().length != 5 }) err("every word must have exactly 5 letters")
                        cfg["hints"]?.let { if (parts(it) != words.size) err("config.hints needs one part per word") }
                    }
                    "sentence_builder" -> {
                        val sentences = t.array("answers").strings()
                        if (sentences.isEmpty()) err("needs answers (the sentences)")
                        cfg["hints"]?.let { if (parts(it) != sentences.size) err("config.hints needs one part per sentence") }
                    }
                    "boss_fight" -> {
                        val questions = cfg["questions"]?.split("|")
                        val options = cfg["options"]?.split("|")
                        val correct = cfg["correct"]?.split("|")
                        if (questions == null || options == null || correct == null) {
                            err("needs config.questions, config.options and config.correct")
                        } else {
                            if (options.size != questions.size || correct.size != questions.size) {
                                err("config.questions, options and correct need the same number of parts")
                            }
                            questions.indices.forEach { i ->
                                val opts = options.getOrNull(i)?.split(";")?.map { it.trim() }.orEmpty()
                                val c = correct.getOrNull(i)?.trim()?.toIntOrNull()
                                if (opts.size < 2) err("question ${i + 1} needs at least 2 options")
                                if (opts.toSet().size != opts.size) err("question ${i + 1} has duplicate options")
                                if (c == null || c !in opts.indices) err("question ${i + 1}: correct index is missing or outside the options")
                            }
                            cfg["why"]?.let { if (parts(it) != questions.size) err("config.why needs one part per question") }
                        }
                        cfg["seconds"]?.let { if (it.toIntOrNull() == null) err("config.seconds must be a whole number") }
                    }
                    "hitro_razvrsti" -> {
                        val cats = t.array("categories").strings()
                        val items = t.array("items").objects()
                        if (cats.size != 2) err("needs exactly 2 categories")
                        if (items.size < 4) err("needs at least 4 items")
                        if (items.any { it.getString("category") !in cats }) err("an item has a category that is not in 'categories'")
                        cfg["seconds"]?.let { if (it.toIntOrNull() == null) err("config.seconds must be a whole number") }
                    }
                    "spomin" -> {
                        val pairs = t.array("pairs").objects()
                        if (pairs.size !in 2..8) err("needs 2 to 8 pairs")
                        val words = pairs.flatMap { listOf(it.getString("left"), it.getString("right")) }
                        if (words.toSet().size != words.size) err("every card text must be different")
                        cfg["seconds"]?.let { if (it.toIntOrNull() == null) err("config.seconds must be a whole number") }
                    }
                    "decimalke_2048" -> {
                        cfg["wholes"]?.let { if (it.toIntOrNull() == null) err("config.wholes must be a whole number") }
                    }
                    "ulomki_kocke" -> {
                        val fractions = cfg["fractions"]
                        if (fractions != null) {
                            if (!fractions.split("|").all { Regex("\\d+/\\d+").matches(it.trim()) }) err("config.fractions must look like 1/2|3/8")
                        } else if (cfg["numerator"]?.toIntOrNull() == null || cfg["denominator"]?.toIntOrNull() == null) {
                            err("needs config.numerator and config.denominator, or config.fractions")
                        }
                    }
                }
            }
        }
    }
}