package si.uckoquiz.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.ui.onboarding.LocalProfileStore
import si.uckoquiz.app.ui.onboarding.OnboardingScreen
import si.uckoquiz.app.ui.theme.UckoBlue
import si.uckoquiz.app.ui.theme.UckoQuizTheme

/** Zavihki v spodnji vrstici (po Figmi). Lestvica in Prijatelji iz načrta sta v nadaljnjem razvoju. */
enum class Tab(val label: String, val emoji: String) {
    HOME("Domov", "🏠"),
    SUBJECTS("Predmeti", "📚"),
    PROFILE("Profil", "👤"),
    SETTINGS("Nastavitve", "⚙️"),
}

@Composable
fun UckoApp() {
    val context = LocalContext.current
    val store = remember { LocalProfileStore(context) }
    var profile by remember { mutableStateOf(store.load()) }

    val current = profile
    if (current == null) {
        OnboardingScreen(onFinish = { newProfile ->
            store.save(newProfile)
            profile = newProfile
        })
    } else {
        MainTabs(current)
    }
}

/** Glavni del aplikacije: spodnja vrstica in current tab */
@Composable
fun MainTabs(profile: UserProfile) {
    var currentTab by rememberSaveable { mutableStateOf(Tab.HOME) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { Text(tab.emoji, fontSize = 22.sp) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedTextColor = UckoBlue,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = UckoBlue.copy(alpha = 0.15f),
                        ),
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (currentTab) {
                Tab.HOME -> PlaceholderScreen("Živijo, ${profile.displayName}!\nDomov: učna pot (UQ-29)")
                Tab.SUBJECTS -> PlaceholderScreen("Predmeti (UQ-30)")
                Tab.PROFILE -> PlaceholderScreen("${profile.avatar} ${profile.displayName}\nCilj: ${profile.dailyGoal} min/dan\nProfil (UQ-36)")
                Tab.SETTINGS -> PlaceholderScreen("Nastavitve (UQ-36)")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainTabsPreview() {
    UckoQuizTheme { MainTabs(UserProfile(displayName = "Ana", avatar = "🦊", dailyGoal = 10)) }
}