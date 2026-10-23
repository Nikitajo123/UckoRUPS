package si.uckoquiz.app.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.Lesson
import si.uckoquiz.app.domain.LessonProgress
import si.uckoquiz.app.domain.Subject

class FirestoreContentRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ContentRepository {

    override suspend fun getSubjects(): List<Subject> =
        db.collection("subjects").get().await()
            .toObjects(Subject::class.java)
            .sortedBy { it.order }

    override suspend fun getChapters(subjectId: String): List<Chapter> =
        db.collection("chapters").whereEqualTo("subjectId", subjectId).get().await()
            .toObjects(Chapter::class.java)
            .sortedBy { it.order }

    override suspend fun getLessons(chapterId: String): List<Lesson> =
        db.collection("lessons").whereEqualTo("chapterId", chapterId).get().await()
            .toObjects(Lesson::class.java)
            .sortedBy { it.order }

    override suspend fun getLesson(lessonId: String): Lesson =
        db.collection("lessons").document(lessonId).get().await()
            .toObject(Lesson::class.java)
            ?: error("Lesson not found: $lessonId")

    override suspend fun getProgress(): Map<String, LessonProgress> {
        val uid = auth.currentUser?.uid ?: return emptyMap()
        val docs = db.collection("users").document(uid).collection("progress").get().await()
        return docs.documents.associate { doc ->
            doc.id to (doc.toObject(LessonProgress::class.java) ?: LessonProgress())
        }
    }

    override suspend fun saveResult(lessonId: String, stars: Int, xp: Int, mistakes: List<String>) {
        TODO("UQ-16")
    }
}