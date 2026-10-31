package si.uckoquiz.app.games

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task
import kotlin.math.abs
import kotlin.random.Random

/**
 * 2048 z decimalkami (game id "decimalke_2048", UQ-53).
 * Swipe the board; two equal tiles merge into their sum: 0,125 + 0,125 = 0,25 -> 0,5 -> 1,0.
 * Every 1,0 (a whole) counts as one correct answer and leaves the board.
 * A full board with no moves costs a heart and starts a new board.
 *
 * Content (in the lesson JSON, optional):
 *   "config.wholes": how many wholes (1,0) to build, default 3
 *
 * Tiles are stored as whole numbers of eighths (1 = 0,125, 2 = 0,25, 4 = 0,5, 8 = 1,0),
 * so the maths stays exact.
 */
@Composable
fun Decimal2048Game(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val wholes = remember(task.id) { task.config["wholes"]?.toIntOrNull()?.coerceIn(1, 10) ?: 3 }
    val session = rememberGameSession(total = wholes)
    var board by remember(task.id) { mutableStateOf(newBoard()) }
    var lastSum by remember(task.id) { mutableStateOf<String?>(null) }

    if (session.isOver) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    fun swipe(dir: Direction) {
        val move = moveBoard(board, dir)
        if (!move.moved) return
        lastSum = move.merged.lastOrNull()?.let { m -> "${eighths(m / 2)} + ${eighths(m / 2)} = ${eighths(m)}" }
        var next = move.board
        // A whole (8 eighths) scores and leaves the board.
        val made = next.count { it == WHOLE }
        if (made > 0) {
            repeat(made) { session.onCorrect() }
            next = next.map { if (it == WHOLE) 0 else it }
        }
        next = spawn(next)
        if (!hasMoves(next)) {
            session.onWrong()
            next = newBoard()
            lastSum = "Ni več potez. Nova plošča!"
        }
        board = next
    }

    // The swipe handler is set up once, so it always calls the latest swipe().
    val onSwipe by rememberUpdatedState<(Direction) -> Unit> { swipe(it) }

    Column(Modifier.fillMaxSize()) {
        GameHud(title = "2048 z decimalkami", session = session, color = color, onClose = { onFinish(session.result()) })
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Združuj enake ploščice do 1,0." }, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Celote: ${session.correct} / $wholes",
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(12.dp))

            val drag = remember { DragTotal() }
            Column(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFE8DCCB), RoundedCornerShape(16.dp))
                    .padding(8.dp)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { drag.value = Offset.Zero },
                            onDragEnd = {
                                val x = drag.value.x
                                val y = drag.value.y
                                if (maxOf(abs(x), abs(y)) > 40f) {
                                    onSwipe(
                                        if (abs(x) > abs(y)) {
                                            if (x > 0) Direction.Right else Direction.Left
                                        } else {
                                            if (y > 0) Direction.Down else Direction.Up
                                        },
                                    )
                                }
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                drag.value += amount
                            },
                        )
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (r in 0 until SIZE) {
                    Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (c in 0 until SIZE) {
                            val v = board[r * SIZE + c]
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxSize()
                                    .background(tileColor(v), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (v > 0) {
                                    Text(
                                        eighths(v),
                                        fontSize = if (v == 1) 18.sp else 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (v >= 4) Color.White else Color(0xFF5A3A12),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(lastSum ?: "Povleci s prstom: ← → ↑ ↓", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(12.dp))
            // Buttons too, so the game also works with a mouse in the emulator.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { swipe(Direction.Left) }) { Text("←") }
                OutlinedButton(onClick = { swipe(Direction.Up) }) { Text("↑") }
                OutlinedButton(onClick = { swipe(Direction.Down) }) { Text("↓") }
                OutlinedButton(onClick = { swipe(Direction.Right) }) { Text("→") }
            }
        }
    }
}

/** Plain holder for the finger movement during one swipe (no recomposition needed). */
private class DragTotal(var value: Offset = Offset.Zero)

internal const val SIZE = 4
internal const val WHOLE = 8

internal enum class Direction { Left, Right, Up, Down }

internal data class MoveResult(val board: List<Int>, val merged: List<Int>, val moved: Boolean)

/** Slides one line towards index 0 and merges equal neighbours once, like 2048. */
internal fun slideLine(line: List<Int>): Pair<List<Int>, List<Int>> {
    val tiles = line.filter { it != 0 }
    val out = mutableListOf<Int>()
    val merged = mutableListOf<Int>()
    var i = 0
    while (i < tiles.size) {
        if (i + 1 < tiles.size && tiles[i] == tiles[i + 1]) {
            out += tiles[i] * 2
            merged += tiles[i] * 2
            i += 2
        } else {
            out += tiles[i]
            i += 1
        }
    }
    while (out.size < line.size) out += 0
    return out to merged
}

/** Moves the whole 4x4 board (a list of 16 cells, row by row) in one direction. */
internal fun moveBoard(board: List<Int>, dir: Direction): MoveResult {
    val next = board.toMutableList()
    val merged = mutableListOf<Int>()
    for (k in 0 until SIZE) {
        val idx = (0 until SIZE).map { j ->
            when (dir) {
                Direction.Left -> k * SIZE + j
                Direction.Right -> k * SIZE + (SIZE - 1 - j)
                Direction.Up -> j * SIZE + k
                Direction.Down -> (SIZE - 1 - j) * SIZE + k
            }
        }
        val (line, m) = slideLine(idx.map { board[it] })
        idx.forEachIndexed { j, cell -> next[cell] = line[j] }
        merged += m
    }
    return MoveResult(next, merged, next != board)
}

internal fun hasMoves(board: List<Int>): Boolean =
    Direction.entries.any { moveBoard(board, it).moved }

/** Puts a new 0,125 (or sometimes 0,25) tile on a random empty cell. */
internal fun spawn(board: List<Int>, random: Random = Random): List<Int> {
    val empty = board.indices.filter { board[it] == 0 }
    if (empty.isEmpty()) return board
    val next = board.toMutableList()
    next[empty[random.nextInt(empty.size)]] = if (random.nextInt(10) < 8) 1 else 2
    return next
}

private fun newBoard(): List<Int> = spawn(spawn(List(SIZE * SIZE) { 0 }))

/** 1 -> "0,125", 2 -> "0,25", 4 -> "0,5", 8 -> "1,0" */
internal fun eighths(units: Int): String = when (units) {
    1 -> "0,125"
    2 -> "0,25"
    4 -> "0,5"
    8 -> "1,0"
    else -> (units / 8.0).toString().replace('.', ',')
}

private fun tileColor(v: Int): Color = when (v) {
    0 -> Color(0xFFF3EADD)
    1 -> Color(0xFFFDE3C8)
    2 -> Color(0xFFFBC995)
    4 -> GameColors.Mat
    else -> GameColors.Gold
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun Decimal2048Preview() {
    MaterialTheme {
        Surface {
            Decimal2048Game(task = Task(id = "mat_decimalna_03_t8", type = "game", game = "decimalke_2048"), onFinish = {})
        }
    }
}
