package si.uckoquiz.app.ui.onboarding

import android.content.Context
import androidx.core.content.edit
import si.uckoquiz.app.domain.UserProfile

/**
 * Začasno lokalno shranjevanje profila (SharedPreferences), da se uvod pokaže samo ob prvem zagonu.
 * kasneje shranjevanje profila v Firestore (UQ-15, Nikita M)
 */
class LocalProfileStore(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("ucko_profile", Context.MODE_PRIVATE)

    fun load(): UserProfile? {
        if (!prefs.getBoolean(KEY_DONE, false)) return null
        return UserProfile(
            displayName = prefs.getString(KEY_NAME, "") ?: "",
            avatar = prefs.getString(KEY_AVATAR, "") ?: "",
            dailyGoal = prefs.getInt(KEY_GOAL, 10),
            xp = prefs.getInt(KEY_XP, 0),
            streak = prefs.getInt(KEY_STREAK, 0),
            lastActiveDate = prefs.getString(KEY_LAST_ACTIVE, "") ?: "",
            hearts = prefs.getInt(KEY_HEARTS, 5),
        )
    }

    fun save(profile: UserProfile) {
        prefs.edit {
            putBoolean(KEY_DONE, true)
            putString(KEY_NAME, profile.displayName)
            putString(KEY_AVATAR, profile.avatar)
            putInt(KEY_GOAL, profile.dailyGoal)
            // UQ-38: napredek (niz, XP, srčki) se shrani skupaj s profilom
            putInt(KEY_XP, profile.xp)
            putInt(KEY_STREAK, profile.streak)
            putString(KEY_LAST_ACTIVE, profile.lastActiveDate)
            putInt(KEY_HEARTS, profile.hearts)
        }
    }

    private companion object {
        const val KEY_DONE = "onboarding_done"
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar"
        const val KEY_GOAL = "daily_goal"
        const val KEY_XP = "xp"
        const val KEY_STREAK = "streak"
        const val KEY_LAST_ACTIVE = "last_active_date"
        const val KEY_HEARTS = "hearts"
    }
}