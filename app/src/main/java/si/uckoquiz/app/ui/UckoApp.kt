package si.uckoquiz.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import si.uckoquiz.app.ui.home.HomeRoute
import si.uckoquiz.app.ui.lesson.LessonOutcome
import si.uckoquiz.app.ui.lesson.LessonRoute
import si.uckoquiz.app.ui.onboarding.LocalProfileStore
import si.uckoquiz.app.ui.onboarding.OnboardingScreen
import si.uckoquiz.app.ui.profile.ProfileRoute
import si.uckoquiz.app.ui.profile.SettingsScreen
import si.uckoquiz.app.ui.progress.NoHeartsScreen
import si.uckoquiz.app.ui.progress.ProgressRules
import si.uckoquiz.app.ui.result.ResultScreen
import si.uckoquiz.app.ui.sample.SampleContentRepository
import si.uckoquiz.app.ui.settings.AppSettings
import si.uckoquiz.app.ui.settings.LocalAppSettings
import si.uckoquiz.app.ui.settings.SettingsStore
import si.uckoquiz.app.ui.subjects.SubjectsRoute
import si.uckoquiz.app.ui.theme.UckoBlue
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.data.FirestoreContentRepository
import si.uckoquiz.app.ui.settings.ReminderEffect
import java.time.LocalDate


enum class Tab(val label: String, val emoji: String) {
    HOME("Domov", "🏠"),
    SUBJECTS("Predmeti", "📚"),
    PROFILE("Profil", "👤"),
    SETTINGS("Nastavitve", "⚙️"),
}

// Vstop v aplikacijo: ob prvem zagonu uvod, nato zavihki. Nastavitve (temni način, zvok) veljajo za vse zaslone.
@Composable
fun UckoApp() {
    val context = LocalContext.current
    val store = remember { LocalProfileStore(context) }
    val settingsStore = remember { SettingsStore(context) }
    // Ob odprtju: nov dan napolni srčke, niz se prekine, če včeraj ni bilo učenja (UQ-38).
    var profile by remember {
        mutableStateOf(store.load()?.let { ProgressRules.refreshForToday(it, LocalDate.now()) }?.also { store.save(it) })
    }
    var settings by remember { mutableStateOf(settingsStore.load()) }
    ReminderEffect(profileReady = profile != null, settings = settings)

    UckoQuizTheme(darkTheme = settings.darkMode) {
        CompositionLocalProvider(LocalAppSettings provides settings) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val current = profile
                if (current == null) {
                    OnboardingScreen(onFinish = { newProfile ->
                        store.save(newProfile)
                        profile = newProfile
                    })
                } else {
                    MainTabs(
                        profile = current,
                        settings = settings,
                        onSettingsChange = {
                            settingsStore.save(it)
                            settings = it
                        },
                        onProfileChange = {
                            store.save(it)
                            profile = it
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun MainTabs(
    profile: UserProfile,
    settings: AppSettings = AppSettings(),
    onSettingsChange: (AppSettings) -> Unit = {},
    onProfileChange: (UserProfile) -> Unit = {},
) {
    var currentTab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var openLessonId by rememberSaveable { mutableStateOf<String?>(null) }
    // Izbrani predmet si delita Domov in Predmeti (dotik kartice odpre pot tega predmeta).
    var subjectId by rememberSaveable { mutableStateOf("mat") }

    val repository: ContentRepository = remember { FirestoreContentRepository() }
    val scope = rememberCoroutineScope()

    // Izid zadnje lekcije (id lekcije in izid); dokler ni null, se kaže zaslon rezultata.
    var result by remember { mutableStateOf<Pair<String, LessonOutcome>?>(null) }
    // Vaja = ponavljanje že opravljene lekcije (srčki iz profila se ne porabijo).
    var practice by rememberSaveable { mutableStateOf(false) }
    var noHearts by remember { mutableStateOf(false) }

    // Odpre lekcijo, če ima učenec srčke ali če lekcijo samo ponavlja.
    val openLesson: (String) -> Unit = { id ->
        scope.launch {
            val done = (repository.getProgress()[id]?.stars ?: 0) > 0
            if (ProgressRules.canStart(profile, lessonAlreadyDone = done)) {
                practice = done
                openLessonId = id
            } else {
                noHearts = true
            }
        }
    }

    val lessonId = openLessonId
    val finished = result
    if (noHearts) {
        NoHeartsScreen(onBack = { noHearts = false })
    } else if (finished != null) {
        ResultScreen(
            outcome = finished.second,
            streak = profile.streak,
            onContinue = { result = null },
            onRetry = {
                result = null
                openLesson(finished.first)
            },
        )
    } else if (lessonId != null) {
        LessonRoute(
            lessonId = lessonId,
            repository = repository,
            startHearts = ProgressRules.startHearts(profile, practice),
            onClose = { openLessonId = null },
            onFinished = { outcome ->
                scope.launch {
                    // Zvezdice in XP izračuna LessonEngine (UQ-34); nato zaslon rezultata (UQ-35).
                    repository.saveResult(lessonId, outcome.stars, outcome.xp, outcome.mistakes, outcome.perfect)
                    // UQ-38: XP, niz in srčki se shranijo v profil, zato jih Domov, Rezultat in Profil pokažejo.
                    onProfileChange(ProgressRules.afterLesson(profile, outcome, LocalDate.now(), practice))
                    result = lessonId to outcome
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
                        onOpenLesson = openLesson,
                    )
                    Tab.SUBJECTS -> SubjectsRoute(
                        repository = repository,
                        onOpenSubject = {
                            subjectId = it
                            currentTab = Tab.HOME
                        },
                    )
                    Tab.PROFILE -> ProfileRoute(profile, settings, repository, onSettingsChange, onProfileChange)
                    Tab.SETTINGS -> SettingsScreen(profile, settings, onSettingsChange, onProfileChange)
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
    var settings by remember { mutableStateOf(AppSettings()) }
    var profile by remember { mutableStateOf(UserProfile(displayName = "Maja", avatar = "blue", dailyGoal = 10)) }
    UckoQuizTheme(darkTheme = settings.darkMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MainTabs(
                profile = profile,
                settings = settings,
                onSettingsChange = { settings = it },
                onProfileChange = { profile = it },
            )
        }
    }
}