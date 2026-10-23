package si.uckoquiz.app.domain

interface ContentRepository {
    suspend fun getSubjects(): List<Subject>
    suspend fun getChapters(subjectId: String): List<Chapter>
    suspend fun getLessons(chapterId: String): List<Lesson>
    suspend fun getLesson(lessonId: String): Lesson
    suspend fun getProgress(): Map<String, LessonProgress>
    suspend fun saveResult(lessonId: String, stars: Int, xp: Int, mistakes: List<String>)
}