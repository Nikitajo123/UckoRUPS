package si.uckoquiz.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Chapter
import si.uckoquiz.app.domain.ContentRepository
import si.uckoquiz.app.domain.Subject
import si.uckoquiz.app.domain.UserProfile
import si.uckoquiz.app.ui.components.UckoIcon
import si.uckoquiz.app.ui.components.UckoIconType
import si.uckoquiz.app.ui.components.UckoMascot
import si.uckoquiz.app.ui.sample.SampleContent
import si.uckoquiz.app.ui.theme.LockedGrey
import si.uckoquiz.app.ui.theme.UckoGold
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.UckoRed
import si.uckoquiz.app.ui.theme.subjectColor

//cik-cak prikaz na krugovi
private val ZIGZAG = listOf(0, 40, 70, 40, 0, -40, -70, -40)

private val StarText = Color(0xFFD99A00)
private val PillGrey = Color(0xFFF0F0F0)
private val NodeSize = 64.dp
private val BossSize = 80.dp
private val SideSlot = 64.dp

@Composable
fun HomeRoute(
    profile: UserProfile,
    repository: ContentRepository,
    onOpenLesson: (String) -> Unit,
) {
    var subjectId by rememberSaveable { mutableStateOf("mat") }

    val subjects by produceState(initialValue = emptyList<Subject>(), repository) {
        value = runCatching { repository.getSubjects() }.getOrDefault(emptyList())
    }
    val sections by produceState<List<PathSection>?>(initialValue = null, repository, subjectId) {
        value = null
        value = runCatching {
            val chapters = repository.getChapters(subjectId)
            val lessons = chapters.associate { it.id to repository.getLessons(it.id) }
            buildPath(chapters, lessons, repository.getProgress())
        }.getOrDefault(emptyList())
    }

    HomeScreen(
        profile = profile,
        subjects = subjects,
        selectedSubjectId = subjectId,
        onSelectSubject = { subjectId = it },
        sections = sections,
        onOpenLesson = onOpenLesson,
    )
}

@Composable
fun HomeScreen(
    profile: UserProfile,
    subjects: List<Subject>,
    selectedSubjectId: String,
    onSelectSubject: (String) -> Unit,
    sections: List<PathSection>?,
    onOpenLesson: (String) -> Unit,
) {
    val color = subjectColor(selectedSubjectId)
    Column(Modifier.fillMaxSize()) {
        StatsBar(profile)
        SubjectPicker(subjects, selectedSubjectId, onSelectSubject)
        when {
            sections == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = color)
            }
            sections.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Za ta predmet še ni lekcij.", style = MaterialTheme.typography.bodyLarge)
            }
            else -> LearningPathList(sections, color, onOpenLesson)
        }
    }
}

@Composable
private fun StatsBar(profile: UserProfile) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Stat(UckoIconType.DROP, UckoOrange, UckoOrange, profile.streak.toString(), "Dnevni niz")
        Stat(UckoIconType.STAR, UckoGold, StarText, profile.xp.toString(), "Točke XP")
        Stat(UckoIconType.HEART, UckoRed, UckoRed, profile.hearts.toString(), "Srčki")
    }
}

@Composable
private fun Stat(icon: UckoIconType, iconColor: Color, textColor: Color, value: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = "$description: $value" },
    ) {
        UckoIcon(icon, Modifier.size(22.dp), color = iconColor)
        Spacer(Modifier.width(4.dp))
        Text(value, color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
private fun SubjectPicker(subjects: List<Subject>, selectedId: String, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = subjects.firstOrNull { it.id == selectedId }?.name ?: ""
    Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(PillGrey)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(subjectColor(selectedId)))
            Spacer(Modifier.width(8.dp))
            Text(selectedName, fontWeight = FontWeight.Bold, color = Color(0xFF1F2933))
            Text("  ▾", color = Color(0xFF1F2933))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject.name) },
                    leadingIcon = { Box(Modifier.size(10.dp).clip(CircleShape).background(subjectColor(subject.id))) },
                    onClick = {
                        expanded = false
                        onSelect(subject.id)
                    },
                )
            }
        }
    }
}

private sealed interface PathRow {
    data class Header(val number: Int, val chapter: Chapter) : PathRow
    data class Node(val node: PathNode, val position: Int) : PathRow
}

@Composable
private fun LearningPathList(sections: List<PathSection>, color: Color, onOpenLesson: (String) -> Unit) {
    val rows = remember(sections) {
        buildList<PathRow> {
            var position = 0
            sections.forEachIndexed { index, section ->
                add(PathRow.Header(index + 1, section.chapter))
                section.nodes.forEach { add(PathRow.Node(it, position++)) }
            }
        }
    }
    val listState = rememberLazyListState()
    // Ob odprtju se pot pomakne do lekcije ki je na vrsti.
    LaunchedEffect(rows) {
        val current = rows.indexOfFirst { it is PathRow.Node && it.node.state == NodeState.CURRENT }
        if (current > 0) listState.scrollToItem(current - 1)
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 12.dp),
    ) {
        items(rows, key = { row ->
            when (row) {
                is PathRow.Header -> "h_${row.chapter.id}"
                is PathRow.Node -> row.node.lessonId
            }
        }) { row ->
            when (row) {
                is PathRow.Header -> ChapterHeader(row.number, row.chapter.title, color)
                is PathRow.Node -> PathNodeItem(
                    node = row.node,
                    offsetDp = ZIGZAG[row.position % ZIGZAG.size],
                    color = color,
                    onClick = { onOpenLesson(row.node.lessonId) },
                )
            }
        }
    }
}

@Composable
private fun ChapterHeader(number: Int, title: String, color: Color) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text("POGLAVJE $number", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)
        Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun PathNodeItem(node: PathNode, offsetDp: Int, color: Color, onClick: () -> Unit) {
    val fill = when (node.state) {
        NodeState.LOCKED -> LockedGrey
        NodeState.CURRENT -> color
        NodeState.DONE -> UckoGold
    }
    val icon = when {
        node.kind == NodeKind.BOSS -> UckoIconType.CASTLE
        node.kind == NodeKind.GAME -> UckoIconType.GAMEPAD
        node.state == NodeState.LOCKED -> UckoIconType.LOCK
        node.state == NodeState.CURRENT -> UckoIconType.STAR
        else -> UckoIconType.CHECK
    }
    val size = if (node.kind == NodeKind.BOSS) BossSize else NodeSize
    val stateText = when (node.state) {
        NodeState.LOCKED -> "zaklenjeno"
        NodeState.CURRENT -> "na vrsti"
        NodeState.DONE -> "opravljeno, ${node.stars} od 3 zvezdic"
    }
    val isCurrent = node.state == NodeState.CURRENT
    // "ZAČNI" je na strani, ki je bližje sredini; Učko pa na nasprotni strani zaslona.
    val bubbleOnRight = offsetDp >= 0

    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
        if (isCurrent) {
            UckoMascot(
                Modifier
                    .align(if (bubbleOnRight) Alignment.CenterStart else Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
                    .size(72.dp)
            )
        }
        Row(
            Modifier
                .offset(x = offsetDp.dp)
                .semantics(mergeDescendants = true) { contentDescription = "${node.title}, $stateText" },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(SideSlot), contentAlignment = Alignment.CenterEnd) {
                if (isCurrent && !bubbleOnRight) StartBubble(color)
            }
            Spacer(Modifier.width(6.dp))
            if (isCurrent) {
                Box(Modifier.border(3.dp, color.copy(alpha = 0.45f), CircleShape).padding(5.dp)) {
                    NodeCircle(fill, icon, size, enabled = true, onClick = onClick)
                }
            } else {
                NodeCircle(fill, icon, size, enabled = node.state != NodeState.LOCKED, onClick = onClick)
            }
            Spacer(Modifier.width(6.dp))
            Box(Modifier.width(SideSlot), contentAlignment = Alignment.CenterStart) {
                if (isCurrent && bubbleOnRight) StartBubble(color)
            }
        }
    }
}

@Composable
private fun StartBubble(color: Color) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text("ZAČNI", color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun NodeCircle(fill: Color, icon: UckoIconType, size: Dp, enabled: Boolean, onClick: () -> Unit) {
    Box(Modifier.size(width = size, height = size + 6.dp)) {
        Box(
            Modifier
                .size(size)
                .offset(y = 6.dp)
                .clip(CircleShape)
                .background(fill.darker())
        )
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(fill)
                .clickable(enabled = enabled, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            UckoIcon(icon, Modifier.size(size * 0.5f), color = Color.White, cutout = fill)
        }
    }
}

private fun Color.darker(factor: Float = 0.8f) = Color(red * factor, green * factor, blue * factor, alpha)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    val chapters = SampleContent.chapters.filter { it.subjectId == "mat" }
    val lessons = SampleContent.lessons.groupBy { it.chapterId }
    UckoQuizTheme {
        HomeScreen(
            profile = UserProfile(displayName = "Maja", avatar = "blue", xp = 120, streak = 4, hearts = 5),
            subjects = SampleContent.subjects,
            selectedSubjectId = "mat",
            onSelectSubject = {},
            sections = buildPath(chapters, lessons, SampleContent.demoProgress),
            onOpenLesson = {},
        )
    }
}
