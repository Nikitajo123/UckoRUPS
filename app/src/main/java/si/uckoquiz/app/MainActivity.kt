package si.uckoquiz.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import si.uckoquiz.app.ui.UckoApp
import si.uckoquiz.app.ui.theme.UckoQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UckoQuizTheme {
                UckoApp()
            }
        }
    }
}