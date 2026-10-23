package si.uckoquiz.app.domain

data class Subject(
    val id: String = "",
    val name: String = "",
    val order: Int = 0,
    val color: String = "#000000",
    val icon: String = ""
)

data class Chapter(
    val id: String = "",
    val subjectId: String = "",
    val order: Int = 0,
    val title: String = ""
)

data class Explanation(
    val title: String = "",
    val steps: List<String> = emptyList() // Učko says these one by one
)

data class MatchPair(val left: String = "", val right: String = "")
data class DragItem(val text: String = "", val category: String = "")

data class Task(
    val id: String = "",
    val type: String = "choice",                // choice | match | drag | listen | game
    val prompt: String = "",
    val answers: List<String> = emptyList(),    // choice, listen
    val correct: List<Int> = emptyList(),       // indexes into answers
    val pairs: List<MatchPair> = emptyList(),   // match
    val categories: List<String> = emptyList(), // drag
    val items: List<DragItem> = emptyList(),    // drag
    val audioText: String = "",                 // listen: text read by TTS
    val game: String = "",                      // game: sentence_builder | wordle | ...
    val config: Map<String, String> = emptyMap(), // game-specific settings
    val explanation: String = "",
    val difficulty: Int = 1
)

data class Lesson(
    val id: String = "",
    val subjectId: String = "",
    val chapterId: String = "",
    val order: Int = 0,
    val title: String = "",
    val explanation: Explanation = Explanation(),
    val tasks: List<Task> = emptyList(),
    val boss: Boolean = false
)

data class UserProfile(
    val displayName: String = "",
    val avatar: String = "",
    val xp: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String = "", // yyyy-MM-dd
    val hearts: Int = 5,
    val dailyGoal: Int = 1
)

data class LessonProgress(
    val stars: Int = 0,
    val attempts: Int = 0,
    val bestScore: Int = 0,
    val mistakes: List<String> = emptyList(), // task ids
    val completedAt: Long = 0
)