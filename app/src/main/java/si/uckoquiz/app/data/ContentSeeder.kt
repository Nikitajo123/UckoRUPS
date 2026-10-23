package si.uckoquiz.app.data

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

object ContentSeeder {

    suspend fun seed(context: Context, db: FirebaseFirestore = FirebaseFirestore.getInstance()) {
        uploadArray(context, db, "subjects.json", "subjects")
        uploadArray(context, db, "chapters.json", "chapters")
        for (dir in listOf("mat", "ang", "nar")) {
            val files = context.assets.list(dir)?.filter { it.endsWith(".json") } ?: emptyList()
            for (file in files) {
                val lesson = JSONObject(read(context, "$dir/$file"))
                db.collection("lessons").document(lesson.getString("id"))
                    .set(lesson.toMap()).await()
            }
        }
    }

    private suspend fun uploadArray(context: Context, db: FirebaseFirestore, file: String, collection: String) {
        val array = JSONArray(read(context, file))
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            db.collection(collection).document(obj.getString("id")).set(obj.toMap()).await()
        }
    }

    private fun read(context: Context, path: String): String =
        context.assets.open(path).bufferedReader().use { it.readText() }

    private fun JSONObject.toMap(): Map<String, Any?> =
        keys().asSequence().associateWith { fromJson(get(it)) }

    private fun fromJson(value: Any?): Any? = when (value) {
        is JSONObject -> value.toMap()
        is JSONArray -> (0 until value.length()).map { fromJson(value.get(it)) }
        JSONObject.NULL -> null
        else -> value
    }
}