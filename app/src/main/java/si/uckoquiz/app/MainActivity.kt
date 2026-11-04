package si.uckoquiz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import si.uckoquiz.app.ui.UckoApp
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import si.uckoquiz.app.data.ContentSeeder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TEMP: uploads content/ to Firestore on every launch. Remove before merging to main.
        lifecycleScope.launch {
            try {
                ContentSeeder.seed(applicationContext)
                Log.d("UQ", "SEED DONE")
            } catch (e: Exception) {
                Log.e("UQ", "SEED FAILED", e)
            }
        }
        enableEdgeToEdge()
        setContent {
            UckoQuizTheme {
                UckoApp()
            }
        }
    }
}