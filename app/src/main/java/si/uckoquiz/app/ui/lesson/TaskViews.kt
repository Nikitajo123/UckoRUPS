package si.uckoquiz.app.ui.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import si.uckoquiz.app.domain.DragItem
import si.uckoquiz.app.domain.MatchPair
import si.uckoquiz.app.domain.Task
import si.uckoquiz.app.ui.components.CorrectGreen
import si.uckoquiz.app.ui.components.OptionCard
import si.uckoquiz.app.ui.components.OptionState
import si.uckoquiz.app.ui.theme.TextDark
import si.uckoquiz.app.ui.theme.UckoOrange
import si.uckoquiz.app.ui.theme.UckoQuizTheme
import si.uckoquiz.app.ui.theme.UckoRed
import kotlin.math.roundToInt

// Videz ploščic (Figma 09 in 10)
private enum class TileLook { DEFAULT, SELECTED, DONE, CORRECT, WRONG }

private val BorderGrey = Color(0xFFE0E0E0)
private val DoneGrey = Color(0xFFEDEDED)
private val DoneText = Color(0xFFB0B0B0)

/** Ploščica z besedilom, ki se uporablja pri povezovanju in pri vlečenju. */
@Composable
private fun Tile(
    text: String,
    look: TileLook,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    val (background, border, textColor) = when (look) {
        TileLook.DEFAULT -> Triple(Color.White, BorderGrey, TextDark)
        TileLook.SELECTED -> Triple(lerp(Color.White, color, 0.12f), color, TextDark)
        TileLook.DONE -> Triple(DoneGrey, DoneGrey, DoneText)
        TileLook.CORRECT -> Triple(Color(0xFFDDF7C8), CorrectGreen, Color(0xFF3A8A00))
        TileLook.WRONG -> Triple(Color(0xFFFFE0E0), UckoRed, UckoRed)
    }
    Box(
        modifier
            .background(background, shape)
            .border(2.dp, border, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

//izbira odgovora (Figma 08)
/** Odgovori kot kartice: izbran je v barvi predmeta; po preverjanju zelen ali rdeč. */
@Composable
fun ChoiceTask(
    task: Task,
    answer: TaskAnswer.Choice,
    checkedCorrect: Boolean?,
    color: Color,
    onAnswer: (TaskAnswer) -> Unit,
) {
    val selected = answer.selected
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        task.answers.forEachIndexed { i, text ->
            val state = when {
                i !in selected -> OptionState.DEFAULT
                checkedCorrect == null -> OptionState.SELECTED
                checkedCorrect -> OptionState.CORRECT
                else -> OptionState.WRONG
            }
            OptionCard(
                state = state,
                onClick = {
                    if (checkedCorrect == null) {
                        val next = if (task.correct.size > 1) {
                            if (i in selected) selected - i else selected + i
                        } else setOf(i)
                        onAnswer(TaskAnswer.Choice(next))
                    }
                },
                selectedColor = color,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// povezovanje (Figma 09)

/**
 * Dva stolpca: levo pojmi po vrsti, desno premešani. Učenec tapne enega levo in enega desno;
 * povezan par posivi. Dotik povezanega para ga razveže. Po preverjanju so pari zeleni ali rdeči.
 */
@Composable
fun MatchTask(
    task: Task,
    answer: TaskAnswer.Match,
    checked: Boolean,
    color: Color,
    onAnswer: (TaskAnswer) -> Unit,
) {
    val rightOrder = remember(task.id) { matchRightOrder(task) }
    var selectedLeft by remember(task.id) { mutableStateOf<Int?>(null) }
    var selectedRight by remember(task.id) { mutableStateOf<Int?>(null) }
    val pairs = answer.pairs
    val rightToLeft = pairs.entries.associate { (l, r) -> r to l }

    fun connect(left: Int, right: Int) {
        onAnswer(TaskAnswer.Match(pairs + (left to right)))
        selectedLeft = null
        selectedRight = null
    }

    fun lookFor(matchedLeft: Int?, matchedRight: Int?, isSelected: Boolean): TileLook = when {
        matchedLeft != null && matchedRight != null && checked ->
            if (matchedLeft == matchedRight) TileLook.CORRECT else TileLook.WRONG
        matchedLeft != null && matchedRight != null -> TileLook.DONE
        isSelected -> TileLook.SELECTED
        else -> TileLook.DEFAULT
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        task.pairs.indices.forEach { row ->
            val left = row
            val right = rightOrder[row]
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                val leftPartner = pairs[left]
                Tile(
                    text = task.pairs[left].left,
                    look = lookFor(left.takeIf { leftPartner != null }, leftPartner, selectedLeft == left),
                    color = color,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val pickedRight = selectedRight
                        if (!checked) {
                            when {
                                leftPartner != null -> onAnswer(TaskAnswer.Match(pairs - left))
                                pickedRight != null -> connect(left, pickedRight)
                                else -> selectedLeft = if (selectedLeft == left) null else left
                            }
                        }
                    },
                )
                val rightPartner = rightToLeft[right]
                Tile(
                    text = task.pairs[right].right,
                    look = lookFor(rightPartner, right.takeIf { rightPartner != null }, selectedRight == right),
                    color = color,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        val pickedLeft = selectedLeft
                        if (!checked) {
                            when {
                                rightPartner != null -> onAnswer(TaskAnswer.Match(pairs - rightPartner))
                                pickedLeft != null -> connect(pickedLeft, right)
                                else -> selectedRight = if (selectedRight == right) null else right
                            }
                        }
                    },
                )
            }
        }
    }
}

//vleci in spusti (Figma 10)

/**
 * Škatle s črtkanim robom in ploščice spodaj ("Povleci:"). Ploščico lahko povlečeš v škatlo
 * ali jo tapneš in nato tapneš škatlo. Dotik ploščice v škatli jo vrne nazaj.
 */
@Composable
fun DragTask(
    task: Task,
    answer: TaskAnswer.Drag,
    checked: Boolean,
    color: Color,
    onAnswer: (TaskAnswer) -> Unit,
) {
    val placed = answer.placed
    val boxBounds = remember(task.id) { mutableStateMapOf<String, Rect>() }
    var selectedItem by remember(task.id) { mutableStateOf<Int?>(null) }

    fun place(item: Int, category: String?) {
        onAnswer(TaskAnswer.Drag(if (category == null) placed - item else placed + (item to category)))
        selectedItem = null
    }

    fun lookFor(item: Int): TileLook = when {
        checked && placed[item] != null ->
            if (task.items[item].category == placed[item]) TileLook.CORRECT else TileLook.WRONG
        selectedItem == item -> TileLook.SELECTED
        else -> TileLook.DEFAULT
    }

    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            task.categories.forEach { category ->
                Column(
                    Modifier
                        .weight(1f)
                        .heightIn(min = 170.dp)
                        .onGloballyPositioned { boxBounds[category] = it.boundsInRoot() }
                        .background(lerp(Color.White, color, 0.08f), RoundedCornerShape(16.dp))
                        .drawBehind {
                            drawRoundRect(
                                color = color,
                                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f))),
                                cornerRadius = CornerRadius(16.dp.toPx()),
                            )
                        }
                        .clickable(enabled = !checked && selectedItem != null) { selectedItem?.let { place(it, category) } }
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(category, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    task.items.indices.filter { placed[it] == category }.forEach { item ->
                        DraggableTile(task.items[item], lookFor(item), color, enabled = !checked, boxBounds = boxBounds,
                            onTap = { place(item, null) }, onDrop = { target -> place(item, target) })
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        val pool = task.items.indices.filter { it !in placed }
        if (pool.isNotEmpty()) {
            Text("Povleci:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Spacer(Modifier.height(8.dp))
            pool.chunked(3).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                    row.forEach { item ->
                        DraggableTile(task.items[item], lookFor(item), color, enabled = !checked, boxBounds = boxBounds,
                            onTap = { selectedItem = if (selectedItem == item) null else item },
                            onDrop = { target -> if (target != null) place(item, target) })
                    }
                }
            }
        }
    }
}

/** Ploščica, ki jo lahko povlečeš; ob spustu pove, nad katero škatlo je (ali null). */
@Composable
private fun DraggableTile(
    item: DragItem,
    look: TileLook,
    color: Color,
    enabled: Boolean,
    boxBounds: Map<String, Rect>,
    onTap: () -> Unit,
    onDrop: (String?) -> Unit,
) {
    // Najnovejša onDrop (sicer bi gesta uporabila staro stanje odgovora).
    val currentOnDrop by rememberUpdatedState(onDrop)
    var origin by remember { mutableStateOf(Rect.Zero) }
    var drag by remember { mutableStateOf(Offset.Zero) }
    val dragging = drag != Offset.Zero
    Tile(
        text = item.text,
        look = look,
        color = color,
        onClick = if (enabled) onTap else null,
        modifier = Modifier
            .onGloballyPositioned { origin = it.boundsInRoot() }
            .zIndex(if (dragging) 1f else 0f)
            .offset { IntOffset(drag.x.roundToInt(), drag.y.roundToInt()) }
            .then(
                if (!enabled) Modifier else Modifier.pointerInput(item) {
                    detectDragGestures(
                        onDragEnd = {
                            val center = origin.center + drag
                            currentOnDrop(boxBounds.entries.firstOrNull { it.value.contains(center) }?.key)
                            drag = Offset.Zero
                        },
                        onDragCancel = { drag = Offset.Zero },
                    ) { change, amount ->
                        change.consume()
                        drag += amount
                    }
                }
            ),
    )
}


@Preview(showBackground = true)
@Composable
private fun TaskViewsPreview() {
    val match = Task(id = "m", type = "match", pairs = listOf(MatchPair("1/2", "polovica"), MatchPair("1/3", "tretjina"), MatchPair("1/4", "četrtina")))
    val drag = Task(
        id = "d", type = "drag", categories = listOf("Manj kot 1/2", "Več kot 1/2"),
        items = listOf(DragItem("1/4", "Manj kot 1/2"), DragItem("1/3", "Manj kot 1/2"), DragItem("2/3", "Več kot 1/2")),
    )
    UckoQuizTheme {
        Column(Modifier.padding(16.dp)) {
            MatchTask(match, TaskAnswer.Match(mapOf(0 to 0)), checked = false, color = UckoOrange, onAnswer = {})
            Spacer(Modifier.height(24.dp))
            DragTask(drag, TaskAnswer.Drag(mapOf(1 to "Manj kot 1/2")), checked = false, color = UckoOrange, onAnswer = {})
        }
    }
}
