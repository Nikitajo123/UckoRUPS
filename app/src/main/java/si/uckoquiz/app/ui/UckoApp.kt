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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.games.GameResult
import si.uckoquiz.app.games.starsFor
import si.uckoquiz.app.ui.home.HomeRoute
import si.uckoquiz.app.ui.lesson.LessonRoute
import si.uckoquiz.app.ui.onboarding.LocalProfileStore
import si.uckoquiz.app.ui.onboarding.OnboardingScreen
import si.uckoquiz.app.ui.sample.SampleContentRepository
import si.uckoquiz.app.ui.subjects.SubjectsRoute
import si.uckoquiz.app.ui.theme.UckoBlue
import si.uckoquiz.app.ui.theme.UckoQuizTheme

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

@Composable
fun MainTabs(profile: UserProfile) {
    var currentTab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var openLessonId by rememberSaveable { mutableStateOf<String?>(null) }
    // Izbrani predmet si delita Domov in Predmeti (dotik kartice odpre pot tega predmeta).
    var subjectId by rememberSaveable { mutableStateOf("mat") }

    // Začasno vzorčna vsebina v pomnilniku. Ko bo vsa vsebina v Firestoru (UQ-20),
    // to vrstico zamenjamo s FirestoreContentRepository().
    val repository: ContentRepository = remember { SampleContentRepository() }
    val scope = rememberCoroutineScope()

    val lessonId = openLessonId
    if (lessonId != null) {
        LessonRoute(
            lessonId = lessonId,
            repository = repository,
            onClose = { openLessonId = null },
            onFinished = { outcome ->
                scope.launch {
                    // Zvezdice po istem pravilu kot pri igrah; brez srčkov ni zvezdic. Zaslon rezultata pride z UQ-35.
                    val stars = if (outcome.passed) {
                        starsFor(GameResult(outcome.correct, outcome.total, outcome.seconds))
                    } else 0
                    repository.saveResult(lessonId, stars, xp = if (stars > 0) 10 + outcome.correct else 0, mistakes = outcome.mistakes)
                    openLessonId = null
                }
            },
        )
    } else {
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
                    Tab.HOME -> HomeRoute(
                        profile = profile,
                        repository = repository,
                        subjectId = subjectId,
                        onSelectSubject = { subjectId = it },
                        onOpenLesson = { openLessonId = it },
                    )
                    Tab.SUBJECTS -> SubjectsRoute(
                        repository = repository,
                        onOpenSubject = {
                            subjectId = it
                            currentTab = Tab.HOME
                        },
                    )
                    Tab.PROFILE -> PlaceholderScreen("${profile.displayName}\nCilj: ${profile.dailyGoal} min/dan\nProfil (UQ-36)")
                    Tab.SETTINGS -> PlaceholderScreen("Nastavitve (UQ-36)")
                }
            }
        }
    }
}

/** Začasni zaslon, dokler pravi zaslon ni narejen. */
@Composable
fun PlaceholderScreen(title: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainTabsPreview() {
    UckoQuizTheme { MainTabs(UserProfile(displayName = "Maja", avatar = "blue", dailyGoal = 10)) }
}
