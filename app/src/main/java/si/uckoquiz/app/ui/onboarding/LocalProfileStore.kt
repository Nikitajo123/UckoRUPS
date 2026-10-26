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
        )
    }

    fun save(profile: UserProfile) {
        prefs.edit {
            putBoolean(KEY_DONE, true)
            putString(KEY_NAME, profile.displayName)
            putString(KEY_AVATAR, profile.avatar)
            putInt(KEY_GOAL, profile.dailyGoal)
        }
    }

    private companion object {
        const val KEY_DONE = "onboarding_done"
        const val KEY_NAME = "name"
        const val KEY_AVATAR = "avatar"
        const val KEY_GOAL = "daily_goal"
    }
}