package si.uckoquiz.app.ui.settings

import android.content.Context
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.core.content.edit

/** Časi, ki jih lahko izbere učenec za dnevni opomnik. */
val REMINDER_TIMES = listOf("16:00", "17:00", "18:00", "19:00", "20:00")

/** Nastavitve aplikacije (Figma 14: zvok, dnevni opomnik, temni način). */
data class AppSettings(
    val sound: Boolean = true,
    val reminders: Boolean = true,
    val reminderTime: String = "18:00",
    val darkMode: Boolean = false,
)

/**
 * Trenutne nastavitve za vse zaslone. Igre lahko preverijo zvok z LocalAppSettings.current.sound,
 * opomniki (UQ-17, WorkManager) pa preberejo reminders in reminderTime iz SettingsStore.
 */
val LocalAppSettings = staticCompositionLocalOf { AppSettings() }

/** Shrani nastavitve na telefonu (SharedPreferences), da ostanejo po zaprtju aplikacije. */
class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ucko_settings", Context.MODE_PRIVATE)

    fun load(): AppSettings = AppSettings(
        sound = prefs.getBoolean(KEY_SOUND, true),
        reminders = prefs.getBoolean(KEY_REMINDERS, true),
        reminderTime = prefs.getString(KEY_REMINDER_TIME, "18:00") ?: "18:00",
        darkMode = prefs.getBoolean(KEY_DARK, false),
    )

    fun save(settings: AppSettings) {
        prefs.edit {
            putBoolean(KEY_SOUND, settings.sound)
            putBoolean(KEY_REMINDERS, settings.reminders)
            putString(KEY_REMINDER_TIME, settings.reminderTime)
            putBoolean(KEY_DARK, settings.darkMode)
        }
    }

    private companion object {
        const val KEY_SOUND = "sound"
        const val KEY_REMINDERS = "reminders"
        const val KEY_REMINDER_TIME = "reminder_time"
        const val KEY_DARK = "dark_mode"
    }
}
