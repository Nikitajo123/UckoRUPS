package si.uckoquiz.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.LessonProgress
import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.settings.AppSettings
import si.uckoquiz.app.ui.settings.REMINDER_TIMES
import si.uckoquiz.app.ui.theme.UckoBlue
import si.uckoquiz.app.ui.theme.UckoGold
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme

private val HintGrey = Color(0xFF8A8F98)
private val CardBorder = Color(0xFFE6E6E6)
private val CheckGreen = Color(0xFF58A700)

/** Dnevni cilji, ki jih lahko izbere učenec (enako kot pri uvodu). */
val DAILY_GOAL_OPTIONS = listOf(5, 10, 15)

/* pravila (brez Androida, testirana) */

/** XP za en nivo: 250 XP = nov nivo (0–249 XP je nivo 1). */
const val XP_PER_LEVEL = 250

fun levelFor(xp: Int): Int = 1 + xp.coerceAtLeast(0) / XP_PER_LEVEL

/** Številke na profilu, izračunane iz napredka lekcij. */
data class ProfileStats(val lessonsDone: Int, val stars: Int)

fun profileStats(progress: Map<String, LessonProgress>): ProfileStats = ProfileStats(
    lessonsDone = progress.values.count { it.stars > 0 },
    stars = progress.values.sumOf { it.stars.coerceIn(0, 3) },
)

/* zasloni */

/** Zavihek Profil (Figma 14): naloži napredek in pokaže profil z nastavitvami. */
@Composable
fun ProfileRoute(
    profile: UserProfile,
    settings: AppSettings,
    repository: ContentRepository,
    onSettingsChange: (AppSettings) -> Unit,
    onProfileChange: (UserProfile) -> Unit,
) {
    val stats by produceState(initialValue = ProfileStats(0, 0), repository) {
        value = runCatching { profileStats(repository.getProgress()) }.getOrDefault(ProfileStats(0, 0))
    }
    ProfileScreen(profile, stats, settings, onSettingsChange, onProfileChange)
}

/** Profil in nastavitve na enem zaslonu, kot v Figmi 14_Profil_in_nastavitve. */
@Composable
fun ProfileScreen(
    profile: UserProfile,
    stats: ProfileStats,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onProfileChange: (UserProfile) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileAvatar(profile.avatar, Modifier.size(84.dp))
        Spacer(Modifier.height(10.dp))
        Text(profile.displayName, style = MaterialTheme.typography.titleLarge)
        Text("Raven ${levelFor(profile.xp)}", style = MaterialTheme.typography.bodyMedium, color = HintGrey)
        Spacer(Modifier.height(20.dp))

        // 2 x 2 kartice s številkami
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(UckoIconType.DROP, UckoOrange, "${profile.streak}", "Niz dni", Modifier.weight(1f))
            StatCard(UckoIconType.STAR, UckoGold, "${profile.xp}", "Skupni XP", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(UckoIconType.CHECK, CheckGreen, "${stats.lessonsDone}", "Lekcije", Modifier.weight(1f))
            StatCard(UckoIconType.STAR, UckoGold, "${stats.stars}", "Zvezdice", Modifier.weight(1f))
        }
        Spacer(Modifier.height(28.dp))

        SettingsSection(profile, settings, onSettingsChange, onProfileChange)
    }
}

/** Zavihek Nastavitve: samo nastavitve (isti seznam kot na profilu). */
@Composable
fun SettingsScreen(
    profile: UserProfile,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onProfileChange: (UserProfile) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text("Nastavitve", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        SettingsSection(profile, settings, onSettingsChange, onProfileChange)
    }
}

/** Seznam nastavitev: zvok, dnevni opomnik (s časom), temni način, dnevni cilj. */
@Composable
private fun SettingsSection(
    profile: UserProfile,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onProfileChange: (UserProfile) -> Unit,
) {
    Column(Modifier.fillMaxWidth()) {
        Text("NASTAVITVE", style = MaterialTheme.typography.labelSmall, color = HintGrey, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        SwitchRow("Zvok", settings.sound) { onSettingsChange(settings.copy(sound = it)) }
        HorizontalDivider(color = CardBorder)

        // Opomnik: stikalo in čas (dotik časa odpre seznam)
        var timeMenu by remember { mutableStateOf(false) }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Dnevni opomnik", style = MaterialTheme.typography.bodyLarge)
            Text(" · ", color = HintGrey)
            Box {
                Text(
                    settings.reminderTime,
                    color = if (settings.reminders) UckoBlue else HintGrey,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = settings.reminders) { timeMenu = true }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .semantics { contentDescription = "Čas opomnika ${settings.reminderTime}, spremeni" },
                )
                DropdownMenu(expanded = timeMenu, onDismissRequest = { timeMenu = false }) {
                    REMINDER_TIMES.forEach { time ->
                        DropdownMenuItem(text = { Text(time) }, onClick = {
                            timeMenu = false
                            onSettingsChange(settings.copy(reminderTime = time))
                        })
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            UckoSwitch(settings.reminders, "Dnevni opomnik") { onSettingsChange(settings.copy(reminders = it)) }
        }
        HorizontalDivider(color = CardBorder)

        SwitchRow("Temni način", settings.darkMode) { onSettingsChange(settings.copy(darkMode = it)) }
        HorizontalDivider(color = CardBorder)

        // Dnevni cilj (shranjen v profilu)
        var goalMenu by remember { mutableStateOf(false) }
        Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Dnevni cilj", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Box {
                Text(
                    "${profile.dailyGoal} min ›",
                    color = UckoBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { goalMenu = true }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .semantics { contentDescription = "Dnevni cilj ${profile.dailyGoal} minut, spremeni" },
                )
                DropdownMenu(expanded = goalMenu, onDismissRequest = { goalMenu = false }) {
                    DAILY_GOAL_OPTIONS.forEach { minutes ->
                        DropdownMenuItem(text = { Text("$minutes min") }, onClick = {
                            goalMenu = false
                            onProfileChange(profile.copy(dailyGoal = minutes))
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        UckoSwitch(checked, label, onChange)
    }
}

/** Stikalo v modri barvi (Figma). */
@Composable
private fun UckoSwitch(checked: Boolean, label: String, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(checkedTrackColor = UckoBlue, checkedThumbColor = Color.White),
        modifier = Modifier.semantics { contentDescription = label },
    )
}

/** Kartica s številko (Niz dni, Skupni XP, Lekcije, Zvezdice). */
@Composable
private fun StatCard(icon: UckoIconType, color: Color, value: String, label: String, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .border(2.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
            .semantics(mergeDescendants = true) { contentDescription = "$label: $value" },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UckoIcon(icon, Modifier.size(24.dp), color = color)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(label, style = MaterialTheme.typography.labelSmall, color = HintGrey)
        }
    }
}

/**
 * Avatar: barvni krog z ikono osebe (id barve iz Figme, npr. "blue")
 * ali emoji živali iz trenutnega uvoda; prazno = moder krog.
 */
@Composable
private fun ProfileAvatar(avatar: String, modifier: Modifier = Modifier) {
    val colorById = mapOf(
        "orange" to Color(0xFFF7A04F), "blue" to UckoBlue, "green" to Color(0xFF4FBF7A),
        "purple" to Color(0xFFA779E0), "pink" to Color(0xFFF48FB1), "teal" to Color(0xFF7FD3C6),
    )
    val circleColor = colorById[avatar]
    val isEmoji = circleColor == null && avatar.isNotBlank()
    Box(
        modifier
            .clip(CircleShape)
            .background(circleColor ?: if (isEmoji) Color(0xFFFFF3E6) else UckoBlue)
            .semantics { contentDescription = "Avatar" },
        contentAlignment = Alignment.Center,
    ) {
        if (isEmoji) {
            Text(avatar, fontSize = 44.sp)
        } else {
            UckoIcon(UckoIconType.PERSON, Modifier.size(52.dp), color = Color.White)
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ProfilePreview() {
    var settings by remember { mutableStateOf(AppSettings()) }
    var profile by remember { mutableStateOf(UserProfile(displayName = "Maja", avatar = "blue", xp = 560, streak = 4, dailyGoal = 10)) }
    UckoQuizTheme(darkTheme = settings.darkMode) {
        ProfileScreen(profile, ProfileStats(lessonsDone = 12, stars = 27), settings, { settings = it }, { profile = it })
    }
}
